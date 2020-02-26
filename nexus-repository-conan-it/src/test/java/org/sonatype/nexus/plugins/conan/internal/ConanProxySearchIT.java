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
import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;
import org.sonatype.repository.conan.internal.ConanFormat;

import org.apache.http.Header;
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
import static org.hamcrest.Matchers.nullValue;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class ConanProxySearchIT
    extends ConanITSupport
{
  private static final int CONAN_REMOTE_PORT = 57777;

  private static final String LIBRARY_NAME = "zlib";

  private static final String LIBRARY_VENDOR = "conan";

  private static final String LIBRARY_VERSION = "1.2.11";

  private static final String NXRM_CONAN_PROXY_REPO_NAME = "conan-test-proxy-online";

  private static final String PATH_PATTERN_SEARCH =
      "conans/search";

  private static final String PATH_SEARCH =
      "conans/" + LIBRARY_NAME + "/" + LIBRARY_VERSION + "/" + LIBRARY_VENDOR + "/stable/search";

  private static final String MOCK_PATTERN_SEARCH_REMOTE_RESPONSE = "MOCK_PATTERN_SEARCH_REMOTE_RESPONSE";

  private static final String MOCK_SEARCH_REMOTE_RESPONSE = "MOCK_SEARCH_REMOTE_RESPONSE";

  private static final String PATH_DIGEST =
      "conans/" + LIBRARY_NAME + "/" + LIBRARY_VERSION + "/" + LIBRARY_VENDOR + "/stable/digest";

  private static final String PATH_DOWNLOAD_URLS =
      "conans/" + LIBRARY_NAME + "/" + LIBRARY_VERSION + "/" + LIBRARY_VENDOR + "/stable/download_urls";

  private static final String PATH_MANIFEST =
      "conans/" + LIBRARY_VENDOR + "/" + LIBRARY_NAME + "/" + LIBRARY_VERSION + "/stable/conanmanifest.txt";

  private static final String FILE_DIGEST = "digest_non_package_search";

  private static final String FILE_MANIFEST = "conanmanifest.txt";

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
    BaseUrlHolder.set(this.nexusUrl.toString());

    server = Server
        .withPort(CONAN_REMOTE_PORT)

        // we should make sure that proxy will not use download_urls
        .serve("/" + PATH_DOWNLOAD_URLS)
        .withBehaviours(Behaviours.error(501))

        .serve("/" + PATH_PATTERN_SEARCH)
        .withBehaviours(Behaviours.content(MOCK_PATTERN_SEARCH_REMOTE_RESPONSE, ContentTypes.APPLICATION_JSON))

        .serve("/" + PATH_SEARCH)
        .withBehaviours(Behaviours.content(MOCK_SEARCH_REMOTE_RESPONSE, ContentTypes.APPLICATION_JSON))

        .serve("/" + PATH_DIGEST)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_DIGEST)))

        .serve("/" + PATH_MANIFEST)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_MANIFEST)))

        .start();

    proxyRepo = repos.createConanProxy(NXRM_CONAN_PROXY_REPO_NAME, server.getUrl().toExternalForm());
    proxyClient = conanClient(proxyRepo);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  /*
    pattern search functionality should redirect requests to remote storage and response return back
  */
  @Test
  public void patternSearch() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_PATTERN_SEARCH + "?q=zlib");
    assertThat(status(response), is(HttpStatus.OK));

    HttpEntity entity = response.getEntity();
    String actualJson = EntityUtils.toString(entity);
    assertThat(actualJson, is(MOCK_PATTERN_SEARCH_REMOTE_RESPONSE));

    Header contentType = response.getEntity().getContentType();
    String mimeType = contentType.getValue();
    assertThat(mimeType, is(ContentTypes.APPLICATION_JSON));

    Asset conanManifestAsset = findAsset(proxyRepo, PATH_MANIFEST);
    assertThat(conanManifestAsset, nullValue());

    Asset downloadUrlsAsset = findAsset(proxyRepo, PATH_DOWNLOAD_URLS);
    assertThat(downloadUrlsAsset, nullValue());

    Asset digestAsset = findAsset(proxyRepo, PATH_DIGEST);
    assertThat(digestAsset, nullValue());
  }

  /*
    search functionality should redirect requests to remote storage and response return back
   */
  @Test
  public void search() throws Exception {
    HttpResponse response = proxyClient.getHttpResponse(PATH_SEARCH);
    assertThat(status(response), is(HttpStatus.OK));

    HttpEntity entity = response.getEntity();
    String actualJson = EntityUtils.toString(entity);
    assertThat(actualJson, is(MOCK_SEARCH_REMOTE_RESPONSE));

    Header contentType = response.getEntity().getContentType();
    String mimeType = contentType.getValue();
    assertThat(mimeType, is(ContentTypes.APPLICATION_JSON));

    Asset conanManifestAsset = findAsset(proxyRepo, PATH_MANIFEST);
    assertThat(conanManifestAsset, nullValue());

    Asset downloadUrlsAsset = findAsset(proxyRepo, PATH_DOWNLOAD_URLS);
    assertThat(downloadUrlsAsset, nullValue());

    Asset digestAsset = findAsset(proxyRepo, PATH_DIGEST);
    assertThat(digestAsset, nullValue());
  }

  @Test
  public void conanManifestUrlDownloadUrlIsNotExistButDigestIsExist() throws Exception {
    // init digest begin
    HttpResponse initDigestResponse = proxyClient.getHttpResponse(PATH_DIGEST);
    assertThat(status(initDigestResponse), is(HttpStatus.OK));
    // init digest end
    // do not test digest. It should be covered by proxy integration test

    HttpResponse response = proxyClient.getHttpResponse(PATH_MANIFEST);
    assertThat(status(response), is(HttpStatus.OK));

    // we should make sure that downloadUrls is not exist
    Asset downloadUrlsAsset = findAsset(proxyRepo, PATH_DOWNLOAD_URLS);
    assertThat(downloadUrlsAsset, nullValue());

    Asset conanManifestAsset = findAsset(proxyRepo, PATH_MANIFEST);
    assertThat(conanManifestAsset.format(), is(ConanFormat.NAME));
    assertThat(conanManifestAsset.name(), is(PATH_MANIFEST));
    assertThat(conanManifestAsset.contentType(), is(ContentTypes.TEXT_PLAIN));
  }
}
