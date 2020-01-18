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
package org.sonatype.repository.conan.internal.hosted;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Headers;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.hosted.search.ConanHostedSearchFacet;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;
import org.sonatype.repository.conan.internal.security.token.ConanTokenFacet;

import static org.sonatype.nexus.repository.http.HttpStatus.NOT_FOUND;
import static org.sonatype.repository.conan.internal.metadata.ConanCoords.convertFromState;
import static org.sonatype.repository.conan.internal.metadata.ConanCoords.getPath;

/**
 * @since 0.0.2
 */
@Named
@Singleton
public class HostedHandlers
    extends ComponentSupport
{
  private static final String V1_CONANS = "/v1/conans/";

  private static final String CLIENT_CHECKSUM = "X-Checksum-Sha1";

  final Handler uploadUrl = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    ConanCoords coord = convertFromState(state);
    String assetPath = getAssetPath(coord);

    return context.getRepository()
        .facet(ConanHostedFacet.class)
        .uploadDownloadUrl(assetPath, coord, context.getRequest().getPayload(), assetKind);
  };

  final Handler uploadManifest = context -> upload(context, "conanmanifest.txt");

  final Handler uploadConanFile = context -> upload(context, "conanfile.py");

  final Handler uploadConanInfo = context -> upload(context, "conaninfo.txt");

  final Handler uploadConanPackage = context -> upload(context, "conan_package.tgz");

  final Handler uploadConanSources = context -> upload(context, "conan_sources.tgz");

  final Handler uploadConanExport = context -> upload(context, "conan_export.tgz");

  private Response upload(final Context context, final String filename) throws IOException {

    /* If the header contains {@link HostedHandlers#CLIENT_CHECKSUM} then this is supposed
    to be used to check against existing content.
    Currently we always assume it is not a mtch by returning a 404
    TODO Check the SHA1 against existing asset to determine if an upload is required
     */
    Headers headers = context.getRequest().getHeaders();
    String method = context.getRequest().getAction();

    if (headers.contains(CLIENT_CHECKSUM) && method != "PUT") {
      return new Response.Builder()
          .status(Status.failure(NOT_FOUND))
          .build();
    }

    State state = context.getAttributes().require(State.class);
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    ConanCoords coord = convertFromState(state);
    String assetPath = getAssetPath(coord) + "/" + filename;

    return context.getRepository()
        .facet(ConanHostedFacet.class)
        .upload(assetPath, coord, context.getRequest().getPayload(), assetKind);
  }

  private String getAssetPath(final ConanCoords coord) {
    return V1_CONANS + getPath(coord);
  }

  final Handler downloadUrl = context -> {
    State state = context.getAttributes().require(State.class);
    ConanCoords coord = convertFromState(state);
    String path = getAssetPath(coord) + "/download_urls";

    return context.getRepository()
        .facet(ConanHostedFacet.class)
        .getDownloadUrl(path, context);
  };

  final Handler download = context ->
      context.getRepository()
          .facet(ConanHostedFacet.class)
          .get(context);

  /**
   * Acknowledges a ping request
   */
  final Handler ping = context -> {
    log.debug("pong");
    return HttpResponses.ok();
  };

  /**
   * Return matching recipe names.
   */
  final Handler searchRecipes = context ->
      context.getRepository()
          .facet(ConanHostedSearchFacet.class)
          .searchRecipes(context);

  /**
   * Search by full package name and return full recipes info
   */
  final Handler searchBinaries = context ->
      context.getRepository()
          .facet(ConanHostedSearchFacet.class)
          .searchBinaries(context);

  final Handler packageSnapshot = context -> {
    State state = context.getAttributes().require(State.class);
    ConanCoords coord = convertFromState(state);
    String path = getAssetPath(coord);

    return context.getRepository()
        .facet(ConanHostedFacet.class)
        .getPackageSnapshot(path, context);
  };

  /**
   * Checks if there is a Bearer Authentication: token
   * otherwise returns 401
   */
  final Handler checkCredentials = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    Repository repository = context.getRepository();
    log.debug("[checkCredentials] repository: {} tokens: {}", repository.getName(), state.getTokens());

    return repository.facet(ConanTokenFacet.class).user(context);
  };

  /**
   * Authenticates the endpoint, generates a response containing the token to be used by the client
   */
  final Handler authenticate = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    Repository repository = context.getRepository();
    log.debug("[authenticate] repository: {} tokens: {}", repository.getName(), state.getTokens());

    return repository.facet(ConanTokenFacet.class).login(context);
  };
}
