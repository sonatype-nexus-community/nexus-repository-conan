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
package org.sonatype.nexus.plugins.conan.internal;

import java.io.IOException;

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.plugins.conan.ConanITConfig.configureConanBase;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class ConanProxyIT
    extends ConanITSupport
{
  private static final int CONAN_REMOTE_PORT = 57777;

  private static final String DIRECTORY_PACKAGE =
      "conans/vthiery/jsonformoderncpp/3.7.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String DIRECTORY_DOWNLOAD_URLS =
      "conans/jsonformoderncpp/3.7.0/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String DIRECTORY_INVALID = "this/is/a/bad/path/";

  private static final String EXTENSION_TGZ = ".tgz";

  private static final String EXTENSION_TXT = ".txt";

  private static final String FILE_DOWNLOAD_URLS = "download_urls";

  private static final String FILE_DIGEST = "digest";

  private static final String FILE_DOWNLOAD_URLS_NON_PACKAGE = "download_urls_non_package";

  private static final String NAME_PACKAGE = "conan_package";

  private static final String NAME_INFO = "conaninfo";

  private static final String NAME_MANIFEST = "conanmanifest";

  private static final String FILE_PACKAGE = NAME_PACKAGE + EXTENSION_TGZ;

  private static final String FILE_INFO = NAME_INFO + EXTENSION_TXT;

  private static final String FILE_MANIFEST = NAME_MANIFEST + EXTENSION_TXT;

  private static final String LIBRARY_NAME = "jsonformoderncpp";

  private static final String LIBRARY_VENDOR = "vthiery";

  private static final String LIBRARY_VERSION = "3.7.0-stable";

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_DIRECTORY =
      "conans/lib/1.0.0/some_vendor/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_PACKAGE_DIRECTORY =
      "conans/some_vendor/lib/1.0.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_CONANMANIFEST_FILE_NAME = "conanmanifest_wrong_hash.txt";

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_CONANMANIFEST_PATH =
      LIB_WITH_WRONG_CONANINFO_HASH_PACKAGE_DIRECTORY + FILE_MANIFEST;

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_CONANINFO_PATH =
      LIB_WITH_WRONG_CONANINFO_HASH_PACKAGE_DIRECTORY + FILE_INFO;

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_FILE_NAME = "download_urls_lib";

  private static final String LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_PATH =
      LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_DIRECTORY + FILE_DOWNLOAD_URLS;

  private static final String NXRM_CONAN_PROXY_REPO_NAME = "conan-test-proxy-online";

  private static final String NXRM_CONAN_PROXY_REPO_PATH = "http://localhost:10000/repository/conan-test-proxy-online/";

  private static final String PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES =
      "conans/jsonformoderncpp/3.7.0/vthiery/stable/download_urls";

  private static final String PATH_TGZ_PACKAGE = DIRECTORY_PACKAGE + FILE_PACKAGE;

  private static final String PATH_INFO = DIRECTORY_PACKAGE + FILE_INFO;

  private static final String PATH_MANIFEST = DIRECTORY_PACKAGE + FILE_MANIFEST;

  private static final String PATH_INVALID = DIRECTORY_INVALID + FILE_PACKAGE;

  private static final String PATH_DOWNLOAD_URLS = DIRECTORY_DOWNLOAD_URLS + FILE_DOWNLOAD_URLS;

  private static final String PATH_DIGEST = DIRECTORY_DOWNLOAD_URLS + FILE_DIGEST;

  private ConanClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  @Configuration
  public static Option[] configureNexus() {
    return configureConanBase();
  }

  @Before
  public void setup() throws Exception {
    BaseUrlHolder.set(this.nexusUrl.toString());

    server = Server.withPort(CONAN_REMOTE_PORT)
        .serve("/" + PATH_DOWNLOAD_URLS)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DOWNLOAD_URLS)))
        .serve("/" + PATH_DIGEST)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DIGEST)))
        .serve("/" + PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DOWNLOAD_URLS_NON_PACKAGE)))
        .serve("/" + LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_PATH)
        .withBehaviours(Behaviours.file(testData.resolveFile(LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_FILE_NAME)))
        .serve("/" + PATH_TGZ_PACKAGE)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_PACKAGE)))
        .serve("/" + PATH_INFO)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_INFO)))
        .serve("/" + LIB_WITH_WRONG_CONANINFO_HASH_CONANINFO_PATH)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_INFO)))
        .serve("/" + PATH_MANIFEST)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_MANIFEST)))
        .serve("/" + LIB_WITH_WRONG_CONANINFO_HASH_CONANMANIFEST_PATH)
        .withBehaviours(Behaviours.file(testData.resolveFile(LIB_WITH_WRONG_CONANINFO_HASH_CONANMANIFEST_FILE_NAME)))
        .start();

    proxyRepo = repos.createConanProxy(NXRM_CONAN_PROXY_REPO_NAME, server.getUrl().toExternalForm());
    proxyClient = conanClient(proxyRepo);
    proxyClient.getHttpResponse(PATH_DOWNLOAD_URLS);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void invalidPathsReturn404() throws Exception {
    assertThat(status(proxyClient.getHttpResponse(PATH_INVALID)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveDownloadUrls() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_DOWNLOAD_URLS);
    assertThat(status(response), is(HttpStatus.OK));
    HttpEntity entity = response.getEntity();
    String download_urls = EntityUtils.toString(entity);
    JsonObject obj = new JsonParser().parse(download_urls).getAsJsonObject();

    assertThat(obj.get(FILE_INFO).getAsString(), is(NXRM_CONAN_PROXY_REPO_PATH + PATH_INFO));
    assertThat(obj.get(FILE_PACKAGE).getAsString(), is(NXRM_CONAN_PROXY_REPO_PATH + PATH_TGZ_PACKAGE));
    assertThat(obj.get(FILE_MANIFEST).getAsString(), is(NXRM_CONAN_PROXY_REPO_PATH + PATH_MANIFEST));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_DOWNLOAD_URLS), is(true));
  }

  @Test
  public void retrieveDigest() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_DIGEST);
    assertThat(status(response), is(HttpStatus.OK));
    HttpEntity entity = response.getEntity();
    String digest = EntityUtils.toString(entity);
    JsonObject obj = new JsonParser().parse(digest).getAsJsonObject();
    assertThat(obj.get(FILE_MANIFEST).getAsString(), is(NXRM_CONAN_PROXY_REPO_PATH + PATH_MANIFEST));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_DIGEST), is(true));
  }

  @Test
  public void retrieveDownloadUrlsFromNonPackageRoute() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES);
    assertThat(status(response), is(HttpStatus.OK));

    HttpEntity entity = response.getEntity();
    String download_urls = EntityUtils.toString(entity);
    JsonObject obj = new JsonParser().parse(download_urls).getAsJsonObject();

    assertThat(obj.get(FILE_MANIFEST).getAsString(),
        is("http://localhost:10000/repository/conan-test-proxy-online/conans/vthiery/jsonformoderncpp/3.7.0/stable/conanmanifest.txt"));
    assertThat(obj.get("conanfile.py").getAsString(),
        is("http://localhost:10000/repository/conan-test-proxy-online/conans/vthiery/jsonformoderncpp/3.7.0/stable/conanfile.py"));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES), is(true));
  }

  @Test
  public void retrievePackageWhenRemoteOnline() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_TGZ_PACKAGE);
    assertThat(status(response), is(HttpStatus.OK));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_TGZ_PACKAGE), is(true));
  }

  @Test
  public void retrieveInfoWhenRemoteOnline() throws Exception {
    // Conan client gets conanmanifest.txt file to examine conanifo.txt file hash
    HttpResponse response = proxyClient.getHttpResponse(PATH_MANIFEST);
    assertThat(status(response), is(HttpStatus.OK));
    // Conan client gets conanifo.txt file
    assertThat(status(proxyClient.getHttpResponse(PATH_INFO)), is(HttpStatus.OK));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_INFO), is(true));
  }

  @Test
  public void retrieveManifestWhenRemoteOnline() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_MANIFEST);

    assertThat(status(response), is(HttpStatus.OK));

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, PATH_MANIFEST), is(true));
  }

  @Test
  public void checkComponentRemovedWhenAssetRemoved() throws IOException {
    HttpResponse response = proxyClient.getHttpResponse(PATH_MANIFEST);
    assertThat(status(response), is(HttpStatus.OK));

    String assetPath = PATH_MANIFEST;

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, assetPath), is(true));
    assertThat(componentAssetTestHelper.componentExists(proxyRepo, LIBRARY_NAME, LIBRARY_VERSION), is(true));

    componentAssetTestHelper.removeAsset(proxyRepo, assetPath);

    assertThat(componentAssetTestHelper.assetExists(proxyRepo, assetPath), is(false));
    assertThat(componentAssetTestHelper.componentExists(proxyRepo, LIBRARY_NAME, LIBRARY_VERSION), is(false));
  }

  @Test
  public void failRetrieveConaninfoWhenWrongHashInConanmanifest() throws Exception {
    // Conan client gets download_urls.txt file to discover links of all dependency artifacts.
    HttpResponse response =
        proxyClient.getHttpResponse(LIB_WITH_WRONG_CONANINFO_HASH_DOWNLOAD_URLS_PATH);
    assertThat(status(response), is(HttpStatus.OK));

    response = proxyClient.getHttpResponse(LIB_WITH_WRONG_CONANINFO_HASH_CONANMANIFEST_PATH);
    assertThat(status(response), is(HttpStatus.OK));
    // Conan client gets conanifo.txt file. Its hash is not equal to hash from conanmanifest.tx file
    // therefore it will be ignored.

    assertThat(status(proxyClient.getHttpResponse(LIB_WITH_WRONG_CONANINFO_HASH_CONANINFO_PATH)),
        is(HttpStatus.NOT_FOUND));
    assertThat(componentAssetTestHelper.assetExists(proxyRepo, LIB_WITH_WRONG_CONANINFO_HASH_CONANINFO_PATH),
        is(false));
  }
}
