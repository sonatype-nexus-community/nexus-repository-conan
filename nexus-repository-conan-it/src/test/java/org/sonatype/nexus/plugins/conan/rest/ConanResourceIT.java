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
package org.sonatype.nexus.plugins.conan.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.rest.api.model.AbstractRepositoryApiRequest;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.repository.conan.internal.ConanFormat;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConanResourceIT
    extends ConanResourceITSupport
{
  @Configuration
  public static Option[] configureNexus() {
    return options(
        configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conan")
    );
  }

  @Before
  public void before() {
    BaseUrlHolder.set(this.nexusUrl.toString());
  }

  @Test
  public void createProxy() throws Exception {
    AbstractRepositoryApiRequest request = createProxyRequest(true);
    Response response = post(getCreateRepositoryPathUrl(ProxyType.NAME), request);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    Repository repository = repositoryManager.get(request.getName());
    assertNotNull(repository);
    assertEquals(ConanFormat.NAME, repository.getFormat().getValue());
    assertEquals(ProxyType.NAME, repository.getType().getValue());

    repositoryManager.delete(request.getName());
  }

  @Test
  public void createProxy_noAuthc() throws Exception {
    setBadCredentials();
    AbstractRepositoryApiRequest request = createProxyRequest(true);
    Response response = post(getCreateRepositoryPathUrl(ProxyType.NAME), request);
    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void createProxy_noAuthz() throws Exception {
    setUnauthorizedUser();
    AbstractRepositoryApiRequest request = createProxyRequest(true);
    Response response = post(getCreateRepositoryPathUrl(ProxyType.NAME), request);
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  public void updateProxy() throws Exception {
    repos.createConanProxy(PROXY_NAME, REMOTE_URL);

    AbstractRepositoryApiRequest request = createProxyRequest(false);

    Response response = put(getUpdateRepositoryPathUrl(ProxyType.NAME, PROXY_NAME), request);
    assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());

    Repository repository = repositoryManager.get(request.getName());
    assertNotNull(repository);

    assertThat(repository.getConfiguration().attributes("storage")
            .get("strictContentTypeValidation"),
        is(false));
    repositoryManager.delete(PROXY_NAME);
  }

  @Test
  public void updateProxy_noAuthc() throws Exception {
    setBadCredentials();
    AbstractRepositoryApiRequest request = createProxyRequest(false);

    Response response = put(getUpdateRepositoryPathUrl(ProxyType.NAME, PROXY_NAME), request);
    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void updateProxy_noAuthz() throws Exception {
    repos.createConanProxy(PROXY_NAME, REMOTE_URL);

    setUnauthorizedUser();
    AbstractRepositoryApiRequest request = createProxyRequest(false);

    Response response = put(getUpdateRepositoryPathUrl(ProxyType.NAME, PROXY_NAME), request);
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }
}
