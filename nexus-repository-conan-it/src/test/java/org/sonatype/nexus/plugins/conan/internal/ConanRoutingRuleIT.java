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

import javax.inject.Inject;

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.conan.internal.fixtures.RepositoryRuleConan;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.routing.RoutingRule;
import org.sonatype.nexus.repository.routing.RoutingRuleStore;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;
import org.sonatype.nexus.testsuite.testsupport.fixtures.RoutingRuleRule;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.nexus.repository.http.HttpStatus.OK;
import static org.sonatype.nexus.repository.http.HttpStatus.FORBIDDEN;

public class ConanRoutingRuleIT
    extends ConanITSupport
{
  private ConanClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  private static final String DOWNLOAD_URLS_FILE_NAME = "download_urls";

  private static final String JSON_FOR_MODERN_CPP_URL =
      "v1/conans/jsonformoderncpp/3.7.0/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/" +
          DOWNLOAD_URLS_FILE_NAME;

  private static final String JSON_FOR_MODERN_CPP_BLOCKED_URL =
      "v1/conans/jsonformoderncpp/3.7.0/blockedvendor/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/" +
          DOWNLOAD_URLS_FILE_NAME;

  private static final String BLOCKED_VENDOR_REGEX = ".*/blockedvendor/.*";

  @Inject
  private RoutingRuleStore ruleStore;

  @Rule
  public RepositoryRuleConan repos = new RepositoryRuleConan(() -> repositoryManager);

  @Rule
  public RoutingRuleRule routingRules = new RoutingRuleRule(() -> ruleStore);

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conan")
    );
  }

  @Before
  public void setup() throws Exception {
    server = Server.withPort(0).serve("/" + JSON_FOR_MODERN_CPP_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(DOWNLOAD_URLS_FILE_NAME)))
        .serve("/" + JSON_FOR_MODERN_CPP_BLOCKED_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(DOWNLOAD_URLS_FILE_NAME)))
        .start();

    proxyRepo = repos.createConanProxy(testName.getMethodName(), server.getUrl().toExternalForm());
    proxyClient = conanClient(proxyRepo);
    EntityId routingRuleId = createBlockedRoutingRule("conan-rule", BLOCKED_VENDOR_REGEX);
    attachRuleToRepository(proxyRepo, routingRuleId);
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }

  private EntityId createBlockedRoutingRule(final String name, final String matcher) {
    RoutingRule rule = routingRules.create(name, matcher);
    return rule.id();
  }

  private void attachRuleToRepository(final Repository repository, final EntityId routingRuleId) throws Exception {
    org.sonatype.nexus.repository.config.Configuration configuration = repository.getConfiguration();
    configuration.setRoutingRuleId(routingRuleId);
    repositoryManager.update(configuration);
  }

  private void assertGetResponseStatus(
      final ConanClient client,
      final Repository repository,
      final String path,
      final int responseCode) throws IOException
  {
    try (CloseableHttpResponse response = client.get(path)) {
      StatusLine statusLine = response.getStatusLine();
      assertThat("Repository:" + repository.getName() + " Path:" + path, statusLine.getStatusCode(), is(responseCode));
    }
  }

  @Test
  public void testRoutingRule() throws Exception {
    assertGetResponseStatus(proxyClient, proxyRepo, JSON_FOR_MODERN_CPP_URL, OK);
  }

  @Test
  public void testBlockRoute() throws Exception {
    assertGetResponseStatus(proxyClient, proxyRepo, JSON_FOR_MODERN_CPP_BLOCKED_URL, FORBIDDEN);
  }
}
