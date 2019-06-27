package org.sonatype.repository.conan.internal.common.v1;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.security.SecurityHandler;
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Route.Builder;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler;
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.HandlerContributor;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.common.v1.ConanRoutes;

import static org.sonatype.repository.conan.internal.AssetKind.CONAN_EXPORT;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_FILE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_INFO;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_MANIFEST;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_SOURCES;
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

@Named
@Singleton
public class ConanControllerV1
    extends ComponentSupport
{
  @Inject
  protected TimingHandler timingHandler;

  @Inject
  protected SecurityHandler securityHandler;

  @Inject
  protected ExceptionHandler exceptionHandler;

  @Inject
  protected HandlerContributor handlerContributor;

  @Inject
  protected ConditionalRequestHandler conditionalRequestHandler;

  @Inject
  protected PartialFetchHandler partialFetchHandler;

  @Inject
  protected ContentHeadersHandler contentHeadersHandler;

  @Inject
  protected UnitOfWorkHandler unitOfWorkHandler;

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

  protected void createRoute(final Router.Builder builder,
                           final Builder matcher,
                           final AssetKind assetKind,
                           final Handler handler)
  {
    builder.route(matcher
        .handler(timingHandler)
        .handler(assetKindHandler(assetKind))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(handler)
        .create());
  }

  protected void createGetRoutes(final Router.Builder builder, final Handler urlHandler, final Handler fetchHandler) {
    createRoute(builder, ConanRoutes.downloadUrls(), DOWNLOAD_URL, urlHandler);
    createRoute(builder, ConanRoutes.conanManifest(), CONAN_MANIFEST, fetchHandler);
    createRoute(builder, ConanRoutes.conanFile(), CONAN_FILE, fetchHandler);
    createRoute(builder, ConanRoutes.conanInfo(), CONAN_INFO, fetchHandler);
    createRoute(builder, ConanRoutes.conanPackage(), CONAN_PACKAGE, fetchHandler);
    createRoute(builder, ConanRoutes.conanSource(), CONAN_SOURCES, fetchHandler);
    createRoute(builder, ConanRoutes.conanExport(), CONAN_EXPORT, fetchHandler);
  }
}
