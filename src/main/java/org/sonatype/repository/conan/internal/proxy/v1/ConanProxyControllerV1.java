package org.sonatype.repository.conan.internal.proxy.v1;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.proxy.ProxyHandler;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.repository.conan.internal.common.v1.ConanControllerV1;

@Named
@Singleton
public class ConanProxyControllerV1
    extends ConanControllerV1
{
  @Inject
  ProxyHandler proxyHandler;

  public void attach(final Router.Builder builder) {
    createGetRoutes(builder, proxyHandler, proxyHandler);
  }
}
