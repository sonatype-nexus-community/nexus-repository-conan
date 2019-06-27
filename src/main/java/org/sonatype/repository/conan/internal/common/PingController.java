package org.sonatype.repository.conan.internal.common;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Route.Builder;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.HandlerContributor;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.ActionMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.repository.conan.internal.hosted.v1.HostedHandlers;

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

  private static Builder ping(final String version) {
    return new Builder().matcher(
        and(
            new ActionMatcher(GET),
            new TokenMatcher(format(PING, version))
        )
    );
  }

  public void attach(final Router.Builder builder, final Handler handler, final String version) {
    builder.route(ping(version)
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(handler)
        .create());
  }
}
