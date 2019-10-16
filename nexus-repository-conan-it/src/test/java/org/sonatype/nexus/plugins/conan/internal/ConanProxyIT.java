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
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.testsuite.testsupport.FormatClientSupport;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.goodies.httpfixture.server.fluent.Behaviours.error;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class ConanProxyIT
    extends ConanITSupport
{
  private static final String VERSION = "0.1.0.0";

  private static final String NAME_PACKAGE = "AlgoRhythm";

  private static final String EXTENSION_TAR_GZ = ".tar.gz";

  private static final String EXTENSION_CABAL = ".conan";

  private static final String EXTENSION_JSON = ".json";

  private static final String NAME_INCREMENTAL_INDEX = "01-index";

  private static final String NAME_TIMESTAMP = "timestamp";

  private static final String NAME_SNAPSHOT = "snapshot";

  private static final String NAME_MIRRORS = "mirrors";

  private static final String NAME_ROOT = "root";

  private static final String FILE_INCREMENTAL_INDEX = NAME_INCREMENTAL_INDEX + EXTENSION_TAR_GZ;

  private static final String FILE_TIMESTAMP = NAME_TIMESTAMP + EXTENSION_JSON;

  private static final String FILE_SNAPSHOT = NAME_SNAPSHOT + EXTENSION_JSON;

  private static final String FILE_MIRRORS = NAME_MIRRORS + EXTENSION_JSON;

  private static final String FILE_ROOT = NAME_ROOT + EXTENSION_JSON;

  private static final String FILE_TAR_GZ_PACKAGE = NAME_PACKAGE + "-" + VERSION + EXTENSION_TAR_GZ;

  private static final String FILE_CABAL_PACKAGE = NAME_PACKAGE + EXTENSION_CABAL;
  
  private static final String DIRECTORY_PACKAGE = "package/" + NAME_PACKAGE + "-" + VERSION + "/";
  
  private static final String DIRECTORY_INVALID = "this/is/a/bad/path/";

  private static final String PATH_TAR_GZ_PACKAGE = DIRECTORY_PACKAGE + FILE_TAR_GZ_PACKAGE;

  private static final String PATH_CABAL_PACKAGE = DIRECTORY_PACKAGE + FILE_CABAL_PACKAGE;

  private static final String PATH_INVALID = DIRECTORY_INVALID + FILE_TAR_GZ_PACKAGE;

  public static final String MIME_GZIP = "application/x-gzip";

  public static final String MIME_TEXT = "text/plain";

  public static final String MIME_JSON = "application/json";

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
        .serve("/" + PATH_TAR_GZ_PACKAGE)
        .withBehaviours(Behaviours.file(testData.resolveFile(FILE_TAR_GZ_PACKAGE)))
        .start();

    proxyRepo = repos.createConanProxy("conan-test-proxy-online", server.getUrl().toExternalForm());
    proxyClient = conanClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    Server serverUnresponsive = Server.withPort(0).serve("/*")
        .withBehaviours(error(HttpStatus.NOT_FOUND))
        .start();
    try {
      Repository proxyRepoUnresponsive =
          repos.createConanProxy("conan-test-proxy-notfound", serverUnresponsive.getUrl().toExternalForm());
      ConanClient proxyClientUnresponsive = conanClient(proxyRepoUnresponsive);
      MatcherAssert.assertThat(FormatClientSupport.status(proxyClientUnresponsive.get(PATH_TAR_GZ_PACKAGE)), is(
          HttpStatus.NOT_FOUND));
    }
    finally {
      serverUnresponsive.stop();
    }
  }

  @Test
  public void invalidPathsReturn404() throws Exception {
    assertThat(status(proxyClient.get(PATH_INVALID)), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrievePackageWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_TAR_GZ_PACKAGE)), is(HttpStatus.OK));

    final Component component = findComponent(proxyRepo, NAME_PACKAGE);
    assertThat(component.version(), is(VERSION));
    assertThat(component.name(), is (NAME_PACKAGE));

    final Asset asset = findAsset(proxyRepo, PATH_TAR_GZ_PACKAGE);
    assertThat(asset.format(), is("conan"));
    assertThat(asset.name(), is(PATH_TAR_GZ_PACKAGE));
    assertThat(asset.contentType(), is(MIME_GZIP));
  }

  @Test
  public void retrieveConanWhenRemoteOffline() throws Exception {
    try {
      proxyRepo = repos.createConanProxy("conan-test-proxy-offline", server.getUrl().toExternalForm());
      proxyClient.get(PATH_TAR_GZ_PACKAGE);
    }
    finally {
      server.stop();
    }
    assertThat(status(proxyClient.get(PATH_TAR_GZ_PACKAGE)), is(HttpStatus.OK));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
