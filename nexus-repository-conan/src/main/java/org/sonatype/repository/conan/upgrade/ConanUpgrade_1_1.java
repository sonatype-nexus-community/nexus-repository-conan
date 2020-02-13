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
import java.util.stream.Stream;

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
@DependsOn(model = DatabaseInstanceNames.CONFIG, version = "1.8")
public class ConanUpgrade_1_1
    extends DatabaseUpgradeSupport
{
  private static final String P_REPOSITORY_NAME = "repository_name";

  private static final String P_ASSET_NAME = "name";

  private static final String P_ATTRIBUTES = "attributes";

  private static final String ASSET_CLASS_NAME = "asset";

  private static final String REPOSITORY_CLASS_NAME = "repository";

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
    if (hasSchemaClass(configDatabaseInstance, REPOSITORY_CLASS_NAME) &&
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
        (db, type) -> {
          String selectAssetQuery = String.format("select from asset where bucket = ? and attributes.conan.asset_kind = '%s'",
              AssetKind.CONAN_MANIFEST.name());
          findAssets(db, repositoryNames, selectAssetQuery)
              .forEach(oDocument -> {

                Map<String, Object> attributes = oDocument.field(P_ATTRIBUTES);
                // remove all attributes, except asset_kind from conan "bucket"
                attributes
                    .put(ConanFormat.NAME, Collections.singletonMap(P_ASSET_KIND, AssetKind.CONAN_MANIFEST.name()));
                oDocument.field(P_ATTRIBUTES, attributes);

                oDocument.save();
              });
        }
    );
  }

  private void updateAssetPath(final List<String> repositoryNames) {
    DatabaseUpgradeSupport.withDatabaseAndClass(componentDatabaseInstance, ASSET_CLASS_NAME,
        (db, type) -> findAssets(db, repositoryNames, "select from asset where bucket = ?")
            .forEach(oDocument -> {
              String name = oDocument.field(P_ASSET_NAME);
              String nextName = null;

              String strategy1 = "/v1/conans/v1/conans/";
              String strategy2 = "v1/conans/";
              String strategy3 = "/v1/conans/";
              String conans = "conans/";

              if (name.startsWith(strategy1)) { // broken based on refactor-v1-api HOSTED
                nextName = conans + name.substring(strategy1.length());
              }
              else if (name.startsWith(strategy2)) { // based on refactor-v1-api PROXY
                nextName = conans + name.substring(strategy2.length());
              }
              else if (name.startsWith(strategy3)) { // latest(master) HOSTED
                nextName = conans + name.substring(strategy3.length());
              }
              else if (!name.startsWith(conans)) { // latest(master) PROXY
                nextName = conans + name;
              }

              if (nextName != null) {
                oDocument.field(P_ASSET_NAME, nextName);
                oDocument.save();
              }
            })
    );
  }

  private Stream<ODocument> findAssets(
      final ODatabaseDocumentTx db,
      final List<String> repositoryNames,
      final String SQL)
  {
    return repositoryNames
        .stream()
        .flatMap(repositoryName -> {
          OIndex<?> bucketIdx = db.getMetadata().getIndexManager().getIndex(new OIndexNameBuilder()
              .type("bucket")
              .property(P_REPOSITORY_NAME)
              .build());
          OIdentifiable bucket = (OIdentifiable) bucketIdx.get(repositoryName);
          if (bucket == null) {
            log.debug("Unable to find bucket for {}", repositoryName);
            return Stream.empty();
          }
          List<ODocument> assets =
              db.query(new OSQLSynchQuery<ODocument>(SQL), bucket.getIdentity());
          return assets.stream();
        });
  }
}
