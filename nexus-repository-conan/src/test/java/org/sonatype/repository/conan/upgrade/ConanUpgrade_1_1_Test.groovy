/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype
 * .com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License
 * Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are
 * trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.conan.upgrade

import org.sonatype.goodies.testsupport.TestSupport
import org.sonatype.nexus.orient.OClassNameBuilder
import org.sonatype.nexus.orient.OIndexNameBuilder
import org.sonatype.nexus.orient.testsupport.DatabaseInstanceRule
import org.sonatype.repository.conan.internal.AssetKind
import org.sonatype.repository.conan.internal.ConanFormat

import com.google.common.collect.ImmutableMap
import com.orientechnologies.orient.core.collate.OCaseInsensitiveCollate
import com.orientechnologies.orient.core.db.record.OIdentifiable
import com.orientechnologies.orient.core.index.OIndex
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE
import com.orientechnologies.orient.core.metadata.schema.OSchema
import com.orientechnologies.orient.core.metadata.schema.OType
import com.orientechnologies.orient.core.record.impl.ODocument
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.notNullValue

class ConanUpgrade_1_1_Test
    extends TestSupport
{
  static final String REPOSITORY_CLASS = new OClassNameBuilder()
      .type("repository")
      .build()

  static final String I_REPOSITORY_REPOSITORY_NAME = new OIndexNameBuilder()
      .type(REPOSITORY_CLASS)
      .property(P_REPOSITORY_NAME)
      .build()

  static final String BUCKET_CLASS = new OClassNameBuilder()
      .type("bucket")
      .build()

  static final String I_BUCKET_REPOSITORY_NAME = new OIndexNameBuilder()
      .type(BUCKET_CLASS)
      .property(P_REPOSITORY_NAME)
      .build()

  static final String ASSET_CLASS = new OClassNameBuilder()
      .type("asset")
      .build()

  static final String I_ASSET_NAME = new OIndexNameBuilder()
      .type(ASSET_CLASS)
      .property(P_NAME)
      .build()

  static final List<String> ACTUAL_NAMES = Collections.unmodifiableList(Arrays.asList(
      // ZLIB proxy base on v1/api-refactor testing
      "v1/conans/zlib/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "v1/conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "v1/conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "v1/conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "v1/conans/zlib/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz",
      "v1/conans/conan/zlib/1.2.11/stable/conanfile.py",
      "v1/conans/conan/zlib/1.2.11/stable/conanmanifest.txt",
      "v1/conans/zlib/1.2.11/conan/stable/download_urls",
      // ZLIB proxy base on v1/api-refactor testing

      // Poco proxy base on latest master testing
      "Poco/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "Poco/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "conan/Poco/1.2.11/stable/conan_export.tgz",
      "conan/Poco/1.2.11/stable/conanfile.py",
      "conan/Poco/1.2.11/stable/conanmanifest.txt",
      "Poco/1.2.11/conan/stable/download_urls",
      // Poco proxy base on latest master testing

      // nodejs hosted base on latest master testing
      "/v1/conans/nodejs/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "/v1/conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "/v1/conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "/v1/conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "/v1/conans/nodejs/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "/v1/conans/conan/nodejs/1.2.11/stable/conan_export.tgz",
      "/v1/conans/conan/nodejs/1.2.11/stable/conanfile.py",
      "/v1/conans/conan/nodejs/1.2.11/stable/conanmanifest.txt",
      "/v1/conans/nodejs/1.2.11/conan/stable/download_urls",
      // nodejs hosted base on latest master testing

      // react hosted base on v1/api-refactor testing
      "/v1/conans/v1/conans/react/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package" +
          ".tgz",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest" +
          ".txt",
      "/v1/conans/v1/conans/react/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/conan_export.tgz",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/conanfile.py",
      "/v1/conans/v1/conans/conan/react/1.2.11/stable/conanmanifest.txt",
      "/v1/conans/v1/conans/react/1.2.11/conan/stable/download_urls"
      // react hosted base on v1/api-refactor testing
  ))

  static final List<String> EXPECTED_NAMES = Collections.unmodifiableList(Arrays.asList(
      // ZLIB proxy v1/api-refactor testing
      "conans/zlib/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "conans/conan/zlib/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "conans/zlib/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "conans/conan/zlib/1.2.11/stable/conan_export.tgz",
      "conans/conan/zlib/1.2.11/stable/conanfile.py",
      "conans/conan/zlib/1.2.11/stable/conanmanifest.txt",
      "conans/zlib/1.2.11/conan/stable/download_urls",
      // ZLIB proxy v1/api-refactor testing

      // Poco proxy base on latest master testing
      "conans/Poco/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "conans/conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "conans/conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "conans/conan/Poco/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "conans/Poco/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "conans/conan/Poco/1.2.11/stable/conan_export.tgz",
      "conans/conan/Poco/1.2.11/stable/conanfile.py",
      "conans/conan/Poco/1.2.11/stable/conanmanifest.txt",
      "conans/Poco/1.2.11/conan/stable/download_urls",
      // Poco proxy base on latest master testing

      // nodejs hosted base on latest master testing
      "conans/nodejs/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "conans/conan/nodejs/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "conans/nodejs/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "conans/conan/nodejs/1.2.11/stable/conan_export.tgz",
      "conans/conan/nodejs/1.2.11/stable/conanfile.py",
      "conans/conan/nodejs/1.2.11/stable/conanmanifest.txt",
      "conans/nodejs/1.2.11/conan/stable/download_urls",
      // nodejs hosted base on latest master testing

      // react hosted base on v1/api-refactor testing
      "conans/react/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55",
      "conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conan_package.tgz",
      "conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conaninfo.txt",
      "conans/conan/react/1.2.11/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/conanmanifest.txt",
      "conans/react/1.2.11/conan/stable/packages/534dcc368c999e07e81f146b3466b8f656ef1f55/download_urls",
      "conans/conan/react/1.2.11/stable/conan_export.tgz",
      "conans/conan/react/1.2.11/stable/conanfile.py",
      "conans/conan/react/1.2.11/stable/conanmanifest.txt",
      "conans/react/1.2.11/conan/stable/download_urls"
      // react hosted base on v1/api-refactor testing
  ))

  private static final String P_NAME = "name"

  private static final String P_FORMAT = "format"

  private static final String P_ATTRIBUTES = "attributes"

  private static final String P_BUCKET = "bucket"

  private static final String P_REPOSITORY_NAME = "repository_name"

  private static final String P_RECIPE_NAME = "recipe_name"

  @Rule
  public DatabaseInstanceRule configDatabase = DatabaseInstanceRule.inMemory("test_config")

  @Rule
  public DatabaseInstanceRule componentDatabase = DatabaseInstanceRule.inMemory("test_component")

  ConanUpgrade_1_1 underTest

  @Before
  void setUp() {
    configDatabase.instance.connect().withCloseable { db ->
      OSchema schema = db.getMetadata().getSchema()

      // repository
      def repositoryType = schema.createClass(REPOSITORY_CLASS)
      repositoryType.createProperty(P_REPOSITORY_NAME, OType.STRING)
          .setCollate(new OCaseInsensitiveCollate())
          .setMandatory(true)
          .setNotNull(true)
      repositoryType.createProperty(P_RECIPE_NAME, OType.STRING)
          .setMandatory(true)
          .setNotNull(true)
      repositoryType.createIndex(I_REPOSITORY_REPOSITORY_NAME, INDEX_TYPE.UNIQUE, P_REPOSITORY_NAME)

      repository('conan-proxy', 'conan-proxy')
    }

    componentDatabase.instance.connect().withCloseable { db ->
      OSchema schema = db.getMetadata().getSchema()

      // bucket
      def bucketType = schema.createClass(BUCKET_CLASS)
      bucketType.createProperty(P_REPOSITORY_NAME, OType.STRING)
          .setMandatory(true)
          .setNotNull(true)
      bucketType.createIndex(I_BUCKET_REPOSITORY_NAME, INDEX_TYPE.UNIQUE, P_REPOSITORY_NAME)

      bucket('conan-proxy')

      // asset
      def assetType = schema.createClass(ASSET_CLASS)

      assetType.createProperty(P_NAME, OType.STRING)
          .setMandatory(true)
          .setNotNull(true)
      assetType.createProperty(P_FORMAT, OType.STRING)
          .setMandatory(true)
          .setNotNull(true)
      assetType.createProperty(P_ATTRIBUTES, OType.EMBEDDEDMAP)
      assetType.createIndex(I_ASSET_NAME, INDEX_TYPE.UNIQUE, P_NAME)

      // create some test data
      OIndex<?> bucketIdx = db.getMetadata().getIndexManager().getIndex(I_BUCKET_REPOSITORY_NAME)
      ACTUAL_NAMES.each { key -> asset(bucketIdx, 'conan-proxy', key, attributes(key)) }
    }

    underTest = new ConanUpgrade_1_1(configDatabase.getInstanceProvider(),
        componentDatabase.getInstanceProvider())
  }

  private Map<String, Object> attributes(final String path) {
    if (path.endsWith(AssetKind.CONAN_MANIFEST.filename)) {
      return Collections.singletonMap(ConanFormat.NAME, ImmutableMap.of(
          "export_source/CMakeLists.txt", "b45a8f19ed120e922c1cb720c0e4a7c7",
          "asset_kind", AssetKind.CONAN_MANIFEST.name()
      ))
    }
    return Collections.singletonMap("another_attributes", "another_attributes_value")
  }

  @Test
  void 'upgrade step updates asset_name'() {
    underTest.apply()
    componentDatabase.instance.connect().withCloseable { db ->
      OIndex<?> idx = db.getMetadata().getIndexManager().getIndex(I_ASSET_NAME)

      EXPECTED_NAMES.each { value ->
        OIdentifiable idf = idx.get(value) as OIdentifiable
        assertThat(idf, notNullValue())
        ODocument asset = idf.record
        assertThat(asset, notNullValue())
      }
    }
  }

  @Test
  void 'upgrade conan manifest'() {
    underTest.apply()
    componentDatabase.instance.connect().withCloseable { db ->
      OIndex<?> idx = db.getMetadata().getIndexManager().getIndex(I_ASSET_NAME)
      EXPECTED_NAMES.each { value ->
        OIdentifiable idf = idx.get(value) as OIdentifiable
        ODocument asset = idf.record

        Map<String, Object> attributes = asset.field("attributes")

        if (value.endsWith(AssetKind.CONAN_MANIFEST.filename)) {
          Map<String, Object> conan = attributes.conan as Map<String, Object>
          assertThat(conan.size(), Matchers.is(1))
          String assetKind = conan.asset_kind
          assertThat(assetKind, Matchers.is(AssetKind.CONAN_MANIFEST.name()))
        }
        else {
          String anotherAttributes = attributes.another_attributes
          assertThat(anotherAttributes, Matchers.is("another_attributes_value"))
        }
      }
    }
  }

  private static repository(final String name, final String recipe) {
    ODocument repository = new ODocument(REPOSITORY_CLASS)
    repository.field(P_REPOSITORY_NAME, name)
    repository.field(P_RECIPE_NAME, recipe)
    repository.save()
  }

  private static bucket(final String repositoryName) {
    ODocument bucket = new ODocument(BUCKET_CLASS)
    bucket.field(P_REPOSITORY_NAME, repositoryName)
    bucket.save()
  }

  private static asset(final OIndex bucketIdx, final String repositoryName, final String name,
                       Map<String, Object> attributes = Collections.emptyMap())
  {
    OIdentifiable idf = bucketIdx.get(repositoryName) as OIdentifiable
    ODocument asset = new ODocument(ASSET_CLASS)
    asset.field(P_BUCKET, idf)
    asset.field(P_NAME, name)
    asset.field(P_FORMAT, ConanFormat.NAME)
    if (!attributes.isEmpty()) {
      asset.field("attributes", attributes)
    }
    asset.save()
  }
}
