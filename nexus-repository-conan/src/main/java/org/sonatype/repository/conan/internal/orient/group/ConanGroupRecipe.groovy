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
package org.sonatype.repository.conan.internal.orient.group

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.group.GroupFacetImpl
import org.sonatype.nexus.repository.group.GroupHandler
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.types.GroupType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.repository.conan.internal.ConanFormat
import org.sonatype.repository.conan.internal.ConanRecipeSupport
import org.sonatype.repository.conan.internal.common.PingController
import org.sonatype.repository.conan.internal.common.v1.ConanRoutes
import org.sonatype.repository.conan.internal.orient.common.UserController

/**
 * @since 1.next
 */
@Named(ConanGroupRecipe.NAME)
@Singleton
class ConanGroupRecipe
    extends ConanRecipeSupport
{
  public static final String NAME = "conan-group"

  private static final String VERSION = "v1";

  @Inject
  Provider<GroupFacetImpl> groupFacet

  @Inject
  GroupHandler groupHandler

  @Inject
  ConanGroupRecipe(@Named(GroupType.NAME) final Type type,
                   @Named(ConanFormat.NAME) final Format format)
  {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(groupFacet.get())
    repository.attach(storageFacet.get())
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(attributesFacet.get())
  }

  ViewFacet configure(ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    addBrowseUnsupportedRoute(builder)

    [PingController.pingMatcher(VERSION),
     UserController.credentialsMatcher(VERSION),
     UserController.authenticateMatcher(VERSION),
     ConanRoutes.putConanFileMatcher(),
     ConanRoutes.getConanFileMatcher(),
     ConanRoutes.putConanInfoMatcher(),
     ConanRoutes.getConanManifestMatcher(),
     ConanRoutes.putConanManifestMatcher(),
     ConanRoutes.getConanPackageMatcher(),
     ConanRoutes.putConanPackageZipMatcher(),
     ConanRoutes.getConanSourceMatcher(),
     ConanRoutes.getConanExportMatcher(),
     ConanRoutes.putConanExportMatcher(),
     ConanRoutes.getConanDigestMatcher(),
     ConanRoutes.getConanDownloadUrlMatcher(),
     ConanRoutes.getConanPackageSnapshotMatcher()].each { matcher ->
      builder.route(new Route.Builder().matcher(matcher)
          .handler(timingHandler)
          .handler(securityHandler)
          .handler(exceptionHandler)
          .handler(handlerContributor)
          .handler(groupHandler)
          .create())
    }

    builder.defaultHandlers(HttpHandlers.notFound())

    facet.configure(builder.create())

    return facet
  }
}
