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
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.DIGEST
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * @since 0.0.2
 */
@Named(ConanHostedRecipe.NAME)
@Singleton
class ConanHostedRecipe
  extends ConanRecipeSupport
{
  public static final String NAME = 'conan-hosted'

  private static final GString BASE_URL = "/v1/conans/{${PROJECT}}/{${VERSION}}/{${GROUP}}/{${STATE}}"

  private static final GString PACKAGES_URL = BASE_URL + "/packages/{${DIGEST}}"

  private static final String UPLOAD = "/upload_urls"

  private static final GString UPLOAD_URL = BASE_URL + UPLOAD

  private static final GString UPLOAD_PACKAGE_URL = PACKAGES_URL + UPLOAD

  private static final String CONANMANIFEST = "/conanmanifest.txt"

  private static final GString CONAN_MANIFEST_URL = BASE_URL + CONANMANIFEST

  private static final GString CONAN_MANIFEST_PACKAGE_URL = PACKAGES_URL + CONANMANIFEST

  private static final String CONANFILE = "/conanfile.py"

  private static final GString CONAN_FILE_URL = BASE_URL + CONANFILE

  private static final GString CONAN_FILE_PACKAGE_URL = PACKAGES_URL + CONANFILE

  private static final String CONANINFO = "/conaninfo.txt"

  private static final GString CONAN_INFO_URL = BASE_URL + CONANINFO

  private static final GString CONAN_INFO_PACKAGE_URL = PACKAGES_URL + CONANINFO

  private static final GString CONAN_PACKAGE_ZIP_URL = PACKAGES_URL + "/conan_package.tgz"

  private static final String DOWNLOAD = "/download_urls"

  private static final GString DOWNLOAD_URL = BASE_URL + DOWNLOAD

  private static final GString DOWNLOAD_PACKAGE_URL = PACKAGES_URL + DOWNLOAD

  private static final String CHECK_CREDENTIALS_URL = "/v1/users/check_credentials"

  private static final String AUTHENTICATE_URL = "/v1/users/authenticate"


  @Inject
  Provider<ConanHostedFacet> hostedFacet

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

    createRoute(builder, uploadUrls(), AssetKind.DOWNLOAD_URL, hostedHandler.uploadUrl)
    createRoute(builder, uploadManifest(), CONAN_MANIFEST, hostedHandler.uploadManifest)
    createRoute(builder, uploadConanfile(), CONAN_FILE, hostedHandler.uploadConanFile)
    createRoute(builder, uploadConaninfo(), CONAN_INFO, hostedHandler.uploadConanInfo)
    createRoute(builder, uploadConanPackageZip(), CONAN_PACKAGE, hostedHandler.uploadConanSource)

    createRoute(builder, downloadUrls(), AssetKind.DOWNLOAD_URL, hostedHandler.downloadUrl)
    createRoute(builder, downloadManifest(), CONAN_MANIFEST, hostedHandler.download)
    createRoute(builder, downloadConanfile(), CONAN_FILE, hostedHandler.download)
    createRoute(builder, downloadConaninfo(), CONAN_INFO, hostedHandler.download)
    createRoute(builder, downloadConanPackageZip(), CONAN_PACKAGE, hostedHandler.download)

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
                                     Builder matcher,
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
   */
  static Builder uploadUrls() {
    new Builder().matcher(
        and(
            new ActionMatcher(POST),
            or(
                new TokenMatcher(UPLOAD_URL),
                new TokenMatcher(UPLOAD_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder uploadManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
                new TokenMatcher(CONAN_MANIFEST_URL),
                new TokenMatcher(CONAN_MANIFEST_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder uploadConanfile() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
                new TokenMatcher(CONAN_FILE_URL),
                new TokenMatcher(CONAN_FILE_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder uploadConaninfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            or(
                new TokenMatcher(CONAN_INFO_URL),
                new TokenMatcher(CONAN_INFO_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder uploadConanPackageZip() {
    new Builder().matcher(
        and(
            new ActionMatcher(PUT),
            new TokenMatcher(CONAN_PACKAGE_ZIP_URL)
        )
    )
  }

  /**
   * Matches on urls ending with upload_urls
   */
  static Builder downloadUrls() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                new TokenMatcher(DOWNLOAD_URL),
                new TokenMatcher(DOWNLOAD_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder downloadManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                new TokenMatcher(CONAN_MANIFEST_URL),
                new TokenMatcher(CONAN_MANIFEST_PACKAGE_URL)
            )
        )
    )
  }

  /**
   * Matches on urls ending with conanfile.py
   */
  static Builder downloadConanfile() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                new TokenMatcher(CONAN_FILE_URL),
                new TokenMatcher(CONAN_FILE_PACKAGE_URL)
            )
        )
    )
  }

  static Builder downloadConaninfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            or(
                new TokenMatcher(CONAN_INFO_URL),
                new TokenMatcher(CONAN_INFO_PACKAGE_URL)
            )
        )
    )
  }

  static Builder downloadConanPackageZip() {
    new Builder().matcher(
        and(
            new ActionMatcher(HEAD, GET),
            new TokenMatcher(CONAN_PACKAGE_ZIP_URL)
        )
    )
  }

  /**
   * Matches on authentication endpoint
   */
  static Builder checkCredentials() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET),
            new TokenMatcher(CHECK_CREDENTIALS_URL)
        )
    )
  }

  /**
   * Matches on credential checking endpoint
   */
  static Builder authenticate() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET),
            new TokenMatcher(AUTHENTICATE_URL)
        )
    )
  }
}
