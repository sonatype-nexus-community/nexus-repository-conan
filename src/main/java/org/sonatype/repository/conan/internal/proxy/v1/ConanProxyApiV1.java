package org.sonatype.repository.conan.internal.proxy.v1;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.proxy.ProxyHandler;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.repository.conan.internal.common.PingController;
import org.sonatype.repository.conan.internal.common.UserController;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class ConanProxyApiV1
{
  private static final String version = "v1";

  private PingController pingController;

  private UserController userController;

  private ConanProxyControllerV1 conanProxyControllerV1;

  private ProxyHandler proxyHandler;

  @Inject
  public ConanProxyApiV1(final PingController pingController,
                         final UserController userController,
                         final ConanProxyControllerV1 conanProxyControllerV1,
                         final ProxyHandler proxyHandler) {
    this.pingController = checkNotNull(pingController);
    this.userController = checkNotNull(userController);
    this.conanProxyControllerV1 = checkNotNull(conanProxyControllerV1);
    this.proxyHandler = checkNotNull(proxyHandler);
  }

  public void create(final Router.Builder builder) {
    pingController.attach(builder, proxyHandler, version);
    conanProxyControllerV1.attach(builder);
  }
}
