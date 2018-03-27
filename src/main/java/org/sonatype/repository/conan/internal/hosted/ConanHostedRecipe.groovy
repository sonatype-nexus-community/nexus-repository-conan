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
package org.sonatype.repository.conan.internal.hosted

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Handler
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
import org.sonatype.repository.conan.internal.security.token.ConanTokenFacet

import com.google.inject.Provider

import static org.sonatype.nexus.repository.http.HttpHandlers.notFound
import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD
import static org.sonatype.nexus.repository.http.HttpMethods.POST
import static org.sonatype.nexus.repository.http.HttpMethods.PUT
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
 * @since conan.next
 */
@Named(ConanHostedRecipe.NAME)
@Singleton
class ConanHostedRecipe
  extends ConanRecipeSupport
{
  public static final String NAME = 'conan-hosted'

  @Inject
  Provider<ConanHostedFacet> hostedFacet;

  @Inject
  Provider<ConanTokenFacet> tokenFacet


  @Inject
  HostedHandlers hostedHandler

  @Inject
  protected ConanHostedRecipe(@Named(HostedType.NAME) final Type type,
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
    repository.attach(componentMaintenanceFacet.get())
    repository.attach(tokenFacet.get())
    repository.attach(hostedFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(attributesFacet.get())
    repository.attach(searchFacet.get())
  }

  ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    createRoute(builder, uploadUrls(), DOWNLOAD_URL, hostedHandler.uploadUrl)
    createRoute(builder, uploadManifest(), CONAN_MANIFEST, hostedHandler.uploadManifest)
    createRoute(builder, uploadConanfile(), CONAN_FILE, hostedHandler.uploadConanFile)
    createRoute(builder, uploadConaninfo(), CONAN_INFO, hostedHandler.uploadConanInfo)
    createRoute(builder, uploadConanPackageZip(), CONAN_SRC, hostedHandler.uploadConanSource)

    createRoute(builder, downloadUrls(), DOWNLOAD_URL, hostedHandler.downloadUrl)
    createRoute(builder, downloadManifest(), CONAN_MANIFEST, hostedHandler.download)
    createRoute(builder, downloadConanfile(), CONAN_FILE, hostedHandler.download)
    createRoute(builder, downloadConaninfo(), CONAN_INFO, hostedHandler.download)
    createRoute(builder, downloadConanPackageZip(), CONAN_SRC, hostedHandler.download)

    builder.route(checkCredentials()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandler.checkCredentials)
        .create())

    builder.route(authenticate()
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(hostedHandler.authenticate)
        .create())


    builder.route(new Route.Builder()
        .matcher(BrowseUnsupportedHandler.MATCHER)
        .handler(browseUnsupportedHandler)
        .create())

    builder.defaultHandlers(notFound())
    facet.configure(builder.create())
    return facet
  }

  private Router.Builder createRoute(Router.Builder builder,
                                     Route.Builder matcher,
                                     AssetKind assetKind,
                                     Handler handler) {
    builder.route(matcher
        .handler(timingHandler)
        .handler(assetKindHandler.rcurry(assetKind))
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(unitOfWorkHandler)
        .handler(handler)
        .create())
  }

  /**
   * Matches on urls ending with upload_urls
   * @return
   */
  static Builder uploadUrls() {
    new Builder().matcher(
        and(
            new ActionMatcher(POST),
            or(
                uploadUrlsPackagesMatcher(),
                uploadUrlsMatcher()
            )
        )
    )
  }

  static TokenMatcher uploadUrlsMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/upload_urls")
  }

  static TokenMatcher uploadUrlsPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/upload_urls")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder uploadManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
              uploadManifestMatcher(),
              uploadManifestPackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher uploadManifestMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conanmanifest.txt")
  }

  static TokenMatcher uploadManifestPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conanmanifest.txt")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder uploadConanfile() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
                uploadConanfileMatcher(),
                uploadConanfilePackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher uploadConanfileMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conanfile.py")
  }

  static TokenMatcher uploadConanfilePackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conanfile.py")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder uploadConaninfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
                uploadConaninfoMatcher(),
                uploadConaninfoPackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher uploadConaninfoMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conaninfo.txt")
  }

  static TokenMatcher uploadConaninfoPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conaninfo.txt")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder uploadConanPackageZip() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            uploadConanPackageZipMatcher()
        )
    )
  }

  static TokenMatcher uploadConanPackageZipMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conan_package.tgz")
  }

  /**
   * Matches on urls ending with upload_urls
   * @return
   */
  static Builder downloadUrls() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
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
    new TokenMatcher("/v1/conans/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/packages/{sha}/download_urls")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder downloadManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                downloadManifestMatcher(),
                downloadManifestPackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher downloadManifestMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conanmanifest.txt")
  }

  static TokenMatcher downloadManifestPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conanmanifest.txt")
  }

  /**
   * Matches on urls ending with conanfile.py
   * @return
   */
  static Builder downloadConanfile() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                downloadConanfileMatcher(),
                downloadConanfilePackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher downloadConanfileMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conanfile.py")
  }

  static TokenMatcher downloadConanfilePackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conanfile.py")
  }

  static Builder downloadConaninfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                downloadConaninfoMatcher(),
                downloadConaninfoPackagesMatcher()
            )
        )
    )
  }

  static TokenMatcher downloadConaninfoMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/conaninfo.txt")
  }

  static TokenMatcher downloadConaninfoPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conaninfo.txt")
  }

  static Builder downloadConanPackageZip() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            downloadConanPackageZipPackagesMatcher()
        )
    )
  }

  static TokenMatcher downloadConanPackageZipPackagesMatcher() {
    new TokenMatcher("/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}/packages/{sha}/conan_package.tgz")
  }

  static Builder checkCredentials() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            checkCredentialsMatcher()
        )
    )
  }

  /**
   * Matches on urls ending with upload_urls
   * @return
   */
  static Builder authenticate() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET),
            authenticateMatcher()
        )
    )
  }

  static TokenMatcher checkCredentialsMatcher() {
    new TokenMatcher("/v1/users/check_credentials")
  }

  static TokenMatcher authenticateMatcher() {
    new TokenMatcher("/v1/users/authenticate")
  }
}
