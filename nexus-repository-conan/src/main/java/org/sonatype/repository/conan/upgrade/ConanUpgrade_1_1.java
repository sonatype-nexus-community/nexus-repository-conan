/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.conan.upgrade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.upgrade.DependsOn;
import org.sonatype.nexus.common.upgrade.Upgrades;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseInstanceNames;
import org.sonatype.nexus.orient.DatabaseUpgradeSupport;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.ConanFormat;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

/**
 * @since 1.0.0
 */
@Named
@Singleton
@Upgrades(model = ConanModel.NAME, from = "1.0", to = "1.1")
@DependsOn(model = DatabaseInstanceNames.COMPONENT, version = "1.14", checkpoint = true)
@DependsOn(model = DatabaseInstanceNames.CONFIG, version = "1.8", checkpoint = true)
public class ConanUpgrade_1_1
    extends DatabaseUpgradeSupport
{
  private static final String P_REPOSITORY_NAME = "repository_name";

  private static final String I_REPOSITORY_NAME = new OIndexNameBuilder()
      .type("bucket")
      .property(P_REPOSITORY_NAME)
      .build();

  private static final String ASSET_CLASS_NAME = "asset";

  private final Provider<DatabaseInstance> configDatabaseInstance;

  private final Provider<DatabaseInstance> componentDatabaseInstance;

  @Inject
  public ConanUpgrade_1_1(
      @Named(DatabaseInstanceNames.CONFIG) final Provider<DatabaseInstance> configDatabaseInstance,
      @Named(DatabaseInstanceNames.COMPONENT) final Provider<DatabaseInstance> componentDatabaseInstance)
  {
    this.configDatabaseInstance = checkNotNull(configDatabaseInstance);
    this.componentDatabaseInstance = checkNotNull(componentDatabaseInstance);
  }

  @Override
  public void apply() {
    if (hasSchemaClass(configDatabaseInstance, "repository") &&
        hasSchemaClass(componentDatabaseInstance, ASSET_CLASS_NAME)) {
      List<String> repositoryNames = findConanRepositoryNames();
      updateAssetPath(repositoryNames);
      removeAttributesFromConanManifest(repositoryNames);
    }
  }

  private List<String> findConanRepositoryNames() {
    try (ODatabaseDocumentTx oDatabaseDocumentTx = configDatabaseInstance.get().connect()) {
      final List<ODocument> documents = oDatabaseDocumentTx.query(
          new OSQLSynchQuery<ODocument>("select from repository where recipe_name in ['conan-proxy', 'conan-hosted']"));
      return documents
          .stream()
          .map(entries -> (String) entries.field(P_REPOSITORY_NAME))
          .collect(Collectors.toList());
    }
  }

  private void removeAttributesFromConanManifest(final List<String> repositoryNames) {
    DatabaseUpgradeSupport.withDatabaseAndClass(componentDatabaseInstance, ASSET_CLASS_NAME,
        (db, type) -> repositoryNames
            .stream()
            .flatMap(repositoryName -> {
              OIndex<?> bucketIdx = db.getMetadata().getIndexManager().getIndex(I_REPOSITORY_NAME);
              OIdentifiable bucket = (OIdentifiable) bucketIdx.get(repositoryName);
              List<ODocument> conanManifests = db.query(new OSQLSynchQuery<ODocument>(
                      "select from asset where bucket = ? and attributes.conan.asset_kind = 'CONAN_MANIFEST'"),
                  bucket.getIdentity());
              return conanManifests.stream();
            })
            .forEach(oDocument -> {

              Map<String, Object> attributes = oDocument.field("attributes");
              // remove all attributes, except asset_kind from conan "bucket"
              attributes
                  .put(ConanFormat.NAME, Collections.singletonMap(P_ASSET_KIND, AssetKind.CONAN_MANIFEST.name()));
              oDocument.field("attributes", attributes);

              oDocument.save();
            })
    );
  }

  private void updateAssetPath(final List<String> repositoryNames) {
    DatabaseUpgradeSupport.withDatabaseAndClass(componentDatabaseInstance, ASSET_CLASS_NAME,
        (db, type) -> repositoryNames
            .stream()
            .flatMap(repositoryName -> {
              OIndex<?> bucketIdx = db.getMetadata().getIndexManager().getIndex(I_REPOSITORY_NAME);
              OIdentifiable bucket = (OIdentifiable) bucketIdx.get(repositoryName);
              List<ODocument> assets =
                  db.query(new OSQLSynchQuery<ODocument>("select from asset where bucket = ?"), bucket.getIdentity());
              return assets.stream();
            })
            .forEach(oDocument -> {
              String name = oDocument.field("name");
              String nextName = null;

              if (name.startsWith("/v1/conans/v1/conans/")) { // broken HOSTED
                nextName = "conans/" + name.substring(21);
              }
              else if (name.startsWith("v1/conans/")) { // broken PROXY
                nextName = "conans/" + name.substring(10);
              }
              else if (name.startsWith("/v1/conans/")) { // broken HOSTED
                nextName = "conans/" + name.substring(11);
              }
              else if (!name.startsWith("conans/")) { // latest master changes. PROXY
                nextName = "conans/" + name;
              }

              if (nextName != null) {
                oDocument.field("name", nextName);
                oDocument.save();
              }
            })
    );
  }
}
