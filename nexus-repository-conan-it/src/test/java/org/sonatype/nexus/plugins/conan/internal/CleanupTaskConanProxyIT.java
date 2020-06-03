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
import java.net.URL;

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.conan.internal.fixtures.RepositoryRuleConan;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.testsupport.cleanup.CleanupITSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.sonatype.nexus.plugins.conan.ConanITConfig.configureConanBase;

public class CleanupTaskConanProxyIT
    extends CleanupITSupport
{
  private ConanClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  private static final String DOWNLOAD_URLS_FILE_NAME = "download_urls";

  private static final String CLEANUP_REGEX = ".*jsonformoderncpp.*";

  private static final String JSON_FOR_MODERN_CPP_URL =
      "v1/conans/jsonformoderncpp/3.7.0/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/" +
          DOWNLOAD_URLS_FILE_NAME;

  private static final String LIB_DOWNLOAD_URLS_FILE_NAME = "download_urls_lib";

  private static final String LIB_URL =
      "v1/conans/lib/1.0.0/some_vendor/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/" +
          DOWNLOAD_URLS_FILE_NAME;

  @Rule
  public RepositoryRuleConan repos = new RepositoryRuleConan(() -> repositoryManager);

  private int fecthArtifacts() {
    try {
      proxyClient.get(LIB_URL);
    }
    catch (IOException e) {
      return 0;
    }
    return 1;
  }

  @Configuration
  public static Option[] configureNexus() {
    return configureConanBase();
  }

  @Before
  public void setup() throws Exception {
    BaseUrlHolder.set(this.nexusUrl.toString());
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/conan"));
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/test-classes/conan"));

    server = Server.withPort(0)
        .serve("/" + JSON_FOR_MODERN_CPP_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(DOWNLOAD_URLS_FILE_NAME)))
        .serve("/" + LIB_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(LIB_DOWNLOAD_URLS_FILE_NAME)))
        .start();

    proxyRepo = repos.createConanProxy(testName.getMethodName(), server.getUrl().toExternalForm());
    URL repositoryUrl = repositoryBaseUrl(proxyRepo);
    proxyClient = new ConanClient(
        clientBuilder(repositoryUrl).build(),
        clientContext(),
        repositoryUrl.toURI()
    );
    proxyClient.get(JSON_FOR_MODERN_CPP_URL);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  @Test
  public void cleanupByLastBlobUpdated() throws Exception {
    assertLastBlobUpdatedComponentsCleanedUp(proxyRepo, 1L,
        () -> fecthArtifacts(), 1L);
  }

  @Test
  public void cleanupByLastDownloaded() throws Exception {
    assertLastDownloadedComponentsCleanedUp(proxyRepo, 1L,
        () -> fecthArtifacts(), 1L);
  }

  @Test
  public void cleanupByRegex() throws Exception {
    assertCleanupByRegex(proxyRepo, 1L, CLEANUP_REGEX,
        () -> fecthArtifacts(), 1L);
  }
}
