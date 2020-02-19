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
package org.sonatype.repository.conan.internal.hosted.v1;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Headers;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.hosted.ConanHostedHelper;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import static org.sonatype.nexus.repository.http.HttpStatus.NOT_FOUND;
import static org.sonatype.nexus.repository.http.HttpStatus.NOT_IMPLEMENTED;
import static org.sonatype.nexus.repository.http.HttpStatus.OK;
import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_JSON;
import static org.sonatype.nexus.repository.view.ContentTypes.TEXT_PLAIN;
import static org.sonatype.nexus.repository.view.Status.failure;
import static org.sonatype.nexus.repository.view.Status.success;

/**
 * @since 0.0.2
 */
@Named
@Singleton
public class HostedHandlers
    extends ComponentSupport
{
  private static final String CLIENT_CHECKSUM = "X-Checksum-Sha1";

  /**
   * Upload handler for all asset except asset kind is DOWNLOAD_URL
   */
  public static final Handler uploadContentHandler = context -> {
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
    ConanCoords coord = ConanHostedHelper.convertFromState(state);
    String assetPath = ConanHostedHelper.getHostedAssetPath(coord, assetKind);

    return context.getRepository()
        .facet(ConanHostedFacet.class)
        .upload(assetPath, coord, context.getRequest().getPayload(), assetKind);
  };

  public static final Handler getDigest = context -> new Response.Builder()
      .status(failure(NOT_IMPLEMENTED))
      .payload(new StringPayload("Hosted does not support digest files and install with force update option", TEXT_PLAIN))
      .build();

  public static final Handler getDownloadUrl = context -> {
    State state = context.getAttributes().require(State.class);
    ConanCoords coord = ConanHostedHelper.convertFromState(state);
    String json = context.getRepository()
        .facet(ConanHostedFacet.class)
        .getDownloadUrlAsJson(coord);
    return new Response.Builder()
        .status(success(OK))
        .payload(new StringPayload(json, APPLICATION_JSON))
        .build();
  };

  public static final Handler getAssets = context ->
      context.getRepository()
          .facet(ConanHostedFacet.class)
          .get(context);

  public static final Handler getPackageSnapshot = context -> {
    State state = context.getAttributes().require(State.class);
    ConanCoords coord = ConanHostedHelper.convertFromState(state);

    String json = context.getRepository()
        .facet(ConanHostedFacet.class)
        .generatePackageSnapshotAsJson(coord);
    return new Response.Builder()
        .status(success(OK))
        .payload(new StringPayload(json, APPLICATION_JSON))
        .build();
  };
}
