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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload.InputStreamSupplier;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.transaction.UnitOfWork;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;
import org.sonatype.repository.conan.internal.utils.ConanFacetUtils;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.http.HttpStatus.OK;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.view.Content.maintainLastModified;
import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_JSON;
import static org.sonatype.nexus.repository.view.Status.success;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.findAsset;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.toContent;
import static org.sonatype.repository.conan.internal.utils.ConanFacetUtils.findComponent;

/**
 * @since conan.next
 */
@Exposed
@Named
public class ConanHostedFacet
    extends FacetSupport
{
  private final UploadUrlManager uploadUrlManager;

  @Inject
  public ConanHostedFacet(final UploadUrlManager uploadUrlManager) {
    this.uploadUrlManager = uploadUrlManager;
  }

  /**
   * Services the upload_url endpoint which is basically the same as
   * the get of download_url.
   * @param assetPath
   * @param coord
   * @param payload
   * @param assetKind
   * @return if successful content of the download_url is returned
   * @throws IOException
   */
  public Response uploadDownloadUrl(final String assetPath,
                                    final ConanCoords coord,
                                    final Payload payload,
                                    final AssetKind assetKind) throws IOException {
    checkNotNull(assetPath);
    checkNotNull(coord);
    checkNotNull(payload);
    checkNotNull(assetKind);

    String savedJson = getSavedJson(assetPath, payload);
    doPutArchive(assetPath + "/download_urls", coord, new StringPayload(savedJson, APPLICATION_JSON), assetKind);
    String response = getResponseJson(savedJson);

    return new Response.Builder()
        .status(success(OK))
        .payload(new StringPayload(response, APPLICATION_JSON))
        .build();
  }

  private String getResponseJson(final String savedJson) throws IOException {
    String response;
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(savedJson.getBytes())) {
      response = uploadUrlManager.prefixToValues(getRepository().getUrl(), byteArrayInputStream);
    }
    return response;
  }

  private String getSavedJson(final String assetPath, final Payload payload) throws IOException {
    String savedJson;
    try (InputStream inputStream = payload.openInputStream()) {
      savedJson = uploadUrlManager.convertKeys(assetPath + "/", inputStream);
    }
    return savedJson;
  }

  public Response upload(final String assetPath,
                         final ConanCoords coord,
                         final Payload payload,
                         final AssetKind assetKind) throws IOException {
    checkNotNull(assetPath);
    checkNotNull(coord);
    checkNotNull(payload);
    checkNotNull(assetKind);

    doPutArchive(assetPath, coord, payload, assetKind);

    return new Response.Builder()
        .status(success(OK))
        .build();
  }

  private void doPutArchive(final String assetPath,
                            final ConanCoords coord,
                            final Payload payload,
                            final AssetKind assetKind) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(payload, ConanFacetUtils.HASH_ALGORITHMS)) {
      doPutArchive(coord, assetPath, tempBlob, assetKind);
    }
  }

  @TransactionalStoreBlob
  protected void doPutArchive(final ConanCoords coord,
                              final String path,
                              final TempBlob tempBlob,
                              final AssetKind assetKind) throws IOException
  {
    checkNotNull(path);
    checkNotNull(tempBlob);

    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Map<String, String> attributes = new HashMap<>();
    attributes.put(GROUP, coord.getGroup());
    attributes.put(PROJECT, coord.getProject());
    attributes.put(VERSION, coord.getVersion());
    attributes.put(STATE, coord.getChannel());

    Component component = findComponent(tx, getRepository(), coord);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .group(coord.getGroup())
          .name(coord.getProject())
          .version(coord.getVersion());
    }
    tx.saveComponent(component);

    Asset asset = findAsset(tx, bucket, path);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(path);
      asset.formatAttributes().set(P_ASSET_KIND, assetKind);
    }

    saveAsset(tx, asset, tempBlob);
  }

  private Content saveAsset(final StorageTx tx,
                            final Asset asset,
                            final Supplier<InputStream> contentSupplier) throws IOException
  {
    return saveAsset(tx, asset, contentSupplier, null, null);
  }

  private Content saveAsset(final StorageTx tx,
                            final Asset asset,
                            final Supplier<InputStream> contentSupplier,
                            final String contentType,
                            final AttributesMap contentAttributes) throws IOException
  {
    Content.applyToAsset(asset, maintainLastModified(asset, contentAttributes));
    AssetBlob assetBlob = tx.setBlob(
        asset, asset.name(), contentSupplier, HASH_ALGORITHMS, null, contentType, false
    );

    asset.markAsDownloaded();
    tx.saveAsset(asset);
    return toContent(asset, assetBlob.getBlob());
  }

  /**
   * Services the download_urls endpoint for root and package data
   * @param context
   * @return json response of conan files to lookup
   * @throws IOException
   */
  public Response getDownloadUrl(final Context context) throws IOException {
    Content content = doGet(context.getRequest().getPath());
    if(content == null) {
      return null;
    }

    String response;
    try (InputStream inputStream = content.openInputStream()) {
      response = uploadUrlManager.prefixToValues(getRepository().getUrl(), inputStream);
    }

    return new Response.Builder()
        .status(success(OK))
        .payload(new StringPayload(response, APPLICATION_JSON))
        .build();
  }

  public Response get(final Context context) {
    Content content = doGet(context.getRequest().getPath());

    return new Response.Builder()
        .status(success(OK))
        .payload(new StreamPayload(
            new InputStreamSupplier() {
              @Nonnull
              @Override
              public InputStream get() throws IOException {
                return content.openInputStream();
              }
            },
            content.getSize(),
            content.getContentType()))
        .build();
  }

  @Nullable
  @TransactionalStoreBlob
  protected Content doGet(final String path)
  {
    checkNotNull(path);

    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = findAsset(tx, tx.findBucket(getRepository()), path);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }
}
