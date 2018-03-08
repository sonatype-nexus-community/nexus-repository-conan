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
package org.sonatype.repository.conan.internal.proxy

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.proxy.ProxyHandler
import org.sonatype.nexus.repository.types.ProxyType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.repository.conan.internal.AssetKind
import org.sonatype.repository.conan.internal.ConanFormat
import org.sonatype.repository.conan.internal.ConanRecipeSupport

import com.google.inject.Provider

import static org.sonatype.nexus.repository.http.HttpHandlers.notFound
import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.or
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_FILE
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_INFO
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_MANIFEST
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_SRC
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * @since 0.0.1
 */
@Named(ConanProxyRecipe.NAME)
@Singleton
class ConanProxyRecipe
  extends ConanRecipeSupport
{
  public static final String NAME = 'conan-proxy'

  @Inject
  Provider<ConanProxyFacet> proxyFacet;

  @Inject
  ProxyHandler proxyHandler

  @Inject
  protected ConanProxyRecipe(@Named(ProxyType.NAME) final Type type,
                             @Named(ConanFormat.NAME) final Format format) {
    super(type, format)
  }

  Closure assetKindHandler = { Context context, AssetKind value ->
    context.attributes.set(AssetKind, value)
    return context.proceed()
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(httpClientFacet.get())
    repository.attach(negativeCacheFacet.get())
    repository.attach(componentMaintenanceFacet.get())
    repository.attach(proxyFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(attributesFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(purgeUnusedFacet.get())
  }

  ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    builder.route(downloadUrls()
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(DOWNLOAD_URL))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .create())

    builder.route(conanManifest()
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(CONAN_MANIFEST))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .create())

    builder.route(conanFile()
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(CONAN_FILE))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .create())

    builder.route(conanInfo()
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(CONAN_INFO))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .create())

    builder.route(conanPackage()
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(CONAN_SRC))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(negativeCacheHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .create())

    builder.route(new Route.Builder()
        .matcher(BrowseUnsupportedHandler.MATCHER)
        .handler(browseUnsupportedHandler)
        .create())

    builder.defaultHandlers(notFound())
    facet.configure(builder.create())
    return facet
  }

  /**
   * Matches on urls ending with download_urls
   * @return
   */
  static Builder downloadUrls() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
                downloadUrlsPackagesMatcher(),
                downloadUrlsMatcher()
            )
        )
    )
  }

  static TokenMatcher downloadUrlsMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/download_urls")
  }

  static TokenMatcher downloadUrlsPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/packages/{sha:.+}/download_urls")
  }

  /**
   * Matches on the manifest files
   * @return
   */
  static Builder conanManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
                conanManifestMatcher(),
                conanManifestPackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher conanManifestMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/export/conanmanifest.txt")
  }

  static TokenMatcher conanManifestPackagesMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/package/{sha:.+}/conanmanifest.txt")
  }

  /**
   * Matches on conanfile.py
   * @return
   */
  static Builder conanFile() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            conanFileMatcher()
        )
    )
  }

  static TokenMatcher conanFileMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/export/conanfile.py")
  }

  static Builder conanInfo() {
    new Builder().matcher(
      and(
          new ActionMatcher(GET, HEAD),
          conanInfoMatcher()
      )
    )
  }

  static TokenMatcher conanInfoMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/package/{sha:.+}/conaninfo.txt")
  }

  static Builder conanPackage() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
              conanPackageMatcher(),
              conanSourcesMatcher()
            )
        )
    )
  }

  static TokenMatcher conanPackageMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/package/{sha:.+}/conan_package.tgz")
  }

  static TokenMatcher conanSourcesMatcher() {
    new TokenMatcher("/{path:.*}/{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}/export/conan_sources.tgz")
  }
}
