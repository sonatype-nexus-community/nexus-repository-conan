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

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class ConanProxyIT
    extends ConanITSupport
{
  private static final String NAME_PACKAGE = "conan_package";

  private static final String NAME_INFO = "conaninfo";

  private static final String NAME_MANIFEST = "conanmanifest";

  private static final String EXTENSION_TGZ = ".tgz";

  private static final String EXTENSION_TXT = ".txt";

  private static final String FILE_DOWNLOAD_URLS = "download_urls";

  private static final String FILE_DOWNLOAD_URLS_NON_PACKAGE = "download_urls_non_package";

  private static final String FILE_PACKAGE = NAME_PACKAGE + EXTENSION_TGZ;

  private static final String FILE_INFO = NAME_INFO + EXTENSION_TXT;

  private static final String FILE_MANIFEST = NAME_MANIFEST + EXTENSION_TXT;
  
  private static final String DIRECTORY_PACKAGE = "v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String DIRECTORY_DOWNLOAD_URLS = "v1/conans/jsonformoderncpp/3.7.0/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/";

  private static final String PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES = "v1/conans/jsonformoderncpp/3.7.0/vthiery/stable/download_urls";
  
  private static final String DIRECTORY_INVALID = "this/is/a/bad/path/";

  private static final String PATH_TGZ_PACKAGE = DIRECTORY_PACKAGE + FILE_PACKAGE;

  private static final String PATH_INFO = DIRECTORY_PACKAGE + FILE_INFO;

  private static final String PATH_MANIFEST = DIRECTORY_PACKAGE + FILE_MANIFEST;

  private static final String PATH_INVALID = DIRECTORY_INVALID + FILE_PACKAGE;

  private static final String MIME_GZIP = "application/gzip";

  private static final String MIME_TEXT = "text/plain";

  private static final String PATH_DOWNLOAD_URLS = DIRECTORY_DOWNLOAD_URLS + FILE_DOWNLOAD_URLS;

  private ConanClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conan")
    );
  }

  @Before
  public void setup() throws Exception {
    server = Server.withPort(0)
        .serve("/" + PATH_DOWNLOAD_URLS)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DOWNLOAD_URLS)))
        .serve("/" + PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DOWNLOAD_URLS_NON_PACKAGE)))
        .serve("/" + PATH_TGZ_PACKAGE)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_PACKAGE)))
        .serve("/" + PATH_INFO)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_INFO)))
        .serve("/" + PATH_MANIFEST)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_MANIFEST)))
        .start();

    proxyRepo = repos.createConanProxy("conan-test-proxy-online", server.getUrl().toExternalForm());
    proxyClient = conanClient(proxyRepo);
    proxyClient.get(PATH_DOWNLOAD_URLS);
  }

  //TODO: current behavior is that a downloads_url file must be present when getting from the server otherwise a 500 status will be thrown
  //@Test
  //public void unresponsiveRemoteProduces404() throws Exception {
  //  Server serverUnresponsive = Server.withPort(0)
  //      .serve("/*")
  //      .withBehaviours(error(HttpStatus.NOT_FOUND))
  //      .start();
  //  try {
  //    Repository proxyRepoUnresponsive =
  //        repos.createConanProxy("conan-test-proxy-notfound", serverUnresponsive.getUrl().toExternalForm());
  //    ConanClient proxyClientUnresponsive = conanClient(proxyRepoUnresponsive);
  //    MatcherAssert.assertThat(FormatClientSupport.status(proxyClientUnresponsive.get(PATH_MANIFEST)), is(
  //        HttpStatus.NOT_FOUND));
  //  }
  //  finally {
  //    serverUnresponsive.stop();
  //  }
  //}

  @Test
  public void invalidPathsReturn404() throws Exception {
    assertThat(status(proxyClient.get(PATH_INVALID)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveDownloadUrls() throws Exception {
    try(CloseableHttpResponse response = proxyClient.get(PATH_DOWNLOAD_URLS)){
      assertThat(status(response), is(HttpStatus.OK));
      HttpEntity entity = response.getEntity();
      String download_urls = EntityUtils.toString(entity);
      JsonObject obj = new JsonParser().parse(download_urls).getAsJsonObject();

      assertThat(obj.get("conaninfo.txt").getAsString(), is("http://localhost:10000/repository/conan-test-proxy-online/v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conaninfo.txt"));
      assertThat(obj.get("conan_package.tgz").getAsString(), is("http://localhost:10000/repository/conan-test-proxy-online/v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conan_package.tgz"));
      assertThat(obj.get("conanmanifest.txt").getAsString(), is("http://localhost:10000/repository/conan-test-proxy-online/v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conanmanifest.txt"));

      final Asset asset = findAsset(proxyRepo, PATH_DOWNLOAD_URLS);
      assertThat(asset.format(), is("conan"));
      assertThat(asset.name(), is(PATH_DOWNLOAD_URLS));
      assertThat(asset.contentType(), is(MIME_TEXT));
    }
  }

  @Test
  public void retrieveDownloadUrlsFromNonPackageRoute() throws Exception {
    try(CloseableHttpResponse response = proxyClient.get(PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES)){
      assertThat(status(response), is(HttpStatus.OK));

      HttpEntity entity = response.getEntity();
      String download_urls = EntityUtils.toString(entity);
      JsonObject obj = new JsonParser().parse(download_urls).getAsJsonObject();

      assertThat(obj.get("conanmanifest.txt").getAsString(), is("http://localhost:10000/repository/conan-test-proxy-online/v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/conanmanifest.txt"));
      assertThat(obj.get("conanfile.py").getAsString(), is("http://localhost:10000/repository/conan-test-proxy-online/v1/conans/vthiery/jsonformoderncpp/3.7.0/stable/conanfile.py"));

      final Asset asset = findAsset(proxyRepo, PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES);
      assertThat(asset.format(), is("conan"));
      assertThat(asset.name(), is(PATH_DOWNLOAD_URLS_WITHOUT_PACKAGES));
      assertThat(asset.contentType(), is(MIME_TEXT));
    }
  }

  @Test
  public void retrievePackageWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_TGZ_PACKAGE)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_TGZ_PACKAGE);
    assertThat(asset.format(), is("conan"));
    assertThat(asset.name(), is(PATH_TGZ_PACKAGE));
    assertThat(asset.contentType(), is(MIME_GZIP));
  }

  @Test
  public void retrieveInfoWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_INFO)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_INFO);
    assertThat(asset.format(), is("conan"));
    assertThat(asset.name(), is(PATH_INFO));
    assertThat(asset.contentType(), is(MIME_TEXT));
  }

  @Test
  public void retrieveManifestWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_MANIFEST)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_MANIFEST);
    assertThat(asset.format(), is("conan"));
    assertThat(asset.name(), is(PATH_MANIFEST));
    assertThat(asset.contentType(), is(MIME_TEXT));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
