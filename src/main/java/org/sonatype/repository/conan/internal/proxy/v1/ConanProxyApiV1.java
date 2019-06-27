package org.sonatype.repository.conan.internal.proxy.v1;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.proxy.ProxyHandler;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.repository.conan.internal.common.PingController;

@Named
@Singleton
public class ConanProxyApiV1
{
  private static final String version = "v1";

  private PingController pingController;

  private ConanProxyControllerV1 conanProxyControllerV1;

  private ProxyHandler proxyHandler;

  @Inject
  public ConanProxyApiV1(final PingController pingController,
                         final ConanProxyControllerV1 conanProxyControllerV1,
                         final ProxyHandler proxyHandler) {
    this.pingController = pingController;
    this.conanProxyControllerV1 = conanProxyControllerV1;
    this.proxyHandler = proxyHandler;
  }

  public void create(final Router.Builder builder) {
    pingController.attach(builder, proxyHandler, version);
    conanProxyControllerV1.attach(builder);
  }
}
