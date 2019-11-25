package org.sonatype.repository.conan.internal.common;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Route.Builder;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.HandlerContributor;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.ActionMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.repository.conan.internal.AssetKind;

import static java.lang.String.format;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and;

@Named
@Singleton
public class PingController
    extends ComponentSupport
{
  private static final String PING = "/%s/ping";

  @Inject
  TimingHandler timingHandler;

  @Inject
  SecurityHandler securityHandler;

  @Inject
  ExceptionHandler exceptionHandler;

  @Inject
  HandlerContributor handlerContributor;

  private Handler assetKindHandler(final AssetKind assetKind) {
    return new Handler()
    {
      @Nonnull
      @Override
      public Response handle(@Nonnull final Context context) throws Exception {
        context.getAttributes().set(AssetKind.class, assetKind);
        return context.proceed();
      }
    };
  }

  private static Builder ping(final String version) {
    return new Builder().matcher(
        and(
            new ActionMatcher(GET),
            new TokenMatcher(format(PING, version))
        )
    );
  }

  /**
   * Acknowledges a ping request
   */
  public final Handler ping = context -> {
    log.debug("pong");
    return HttpResponses.ok();
  };

  public void attach(final Router.Builder builder, final String version) {
    builder.route(ping(version)
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(ping)
        .create());
  }
}
