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
package org.sonatype.repository.conan.internal.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import com.sonatype.repository.conan.internal.AssetKind;
import com.sonatype.repository.conan.internal.metadata.ConanAbsoluteUrlRemover;
import com.sonatype.repository.conan.internal.metadata.ConanHashVerifier;
import com.sonatype.repository.conan.internal.metadata.ConanManifest;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.cache.CacheController;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Supplier;
import com.google.common.hash.HashCode;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.repository.conan.internal.AssetKind.CONAN_SRC;
import static com.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.buildAssetPath;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.findAsset;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.project;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.toContent;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.version;
import static com.sonatype.repository.conan.internal.utils.ConanFacetUtils.findComponent;
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.view.Content.maintainLastModified;

@Named
public class ConanProxyFacet
    extends ProxyFacetSupport
{

  private static final ConanAbsoluteUrlRemover absoluteUrlRemover = new ConanAbsoluteUrlRemover();

  private static final ConanHashVerifier hashVerifier = new ConanHashVerifier();

  private static final String CONAN_BINARY_BASE_URL = "http://api.bintray.com/conan/conan/conan-center/v1/files";

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    log.error("doValidate with config {}", configuration);
    super.doValidate(configuration);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    return getAsset(buildAssetPath(context));
  }

  @TransactionalTouchBlob
  @Nullable
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = context.getAttributes().require(TokenMatcher.State.class);
    String assetPath = buildAssetPath(context);

    if (assetKind.equals(CONAN_SRC)) {
      return putPackage(assetPath, content, project(matcherState), version(matcherState));
    }
    return putMetadata(assetPath, content, assetKind);
  }

  private Content putPackage(final String assetPath,
                             final Content content,
                             final String project,
                             final String version) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), HASH_ALGORITHMS)) {
      return doPutPackage(assetPath, project, version, tempBlob, content);
    }
  }

  private Content putMetadata(final String assetPath,
                              final Content content,
                              final AssetKind assetKind)
      throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), HASH_ALGORITHMS)) {
      AttributesMap attributesMap;
      switch (assetKind) {
        case DOWNLOAD_URL:
          TempBlob blob = absoluteUrlRemover.removeAbsoluteUrls(tempBlob, getRepository());
          return doSaveMetadata(assetPath, blob, content, assetKind, new AttributesMap());
        case CONAN_MANIFEST:
          attributesMap = ConanManifest.parse(tempBlob);
          break;
        case CONAN_FILE:
          //TODO: Parse file using to get license information and description, email, author etc

        default:
          attributesMap = new AttributesMap();
          break;
      }
      return doSaveMetadata(assetPath, tempBlob, content, assetKind, attributesMap);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutPackage(final String assetPath,
                               final String project,
                               final String version,
                               final TempBlob tempBlob,
                               final Payload content) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Component component = findComponent(tx, getRepository(), project, version);
    if(component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat()).name(project).version(version);
    }
    tx.saveComponent(component);

    Asset asset = findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, CONAN_SRC.name());
    }
    return saveAsset(tx, asset, tempBlob, content, null);
  }

  @TransactionalStoreBlob
  protected Content doSaveMetadata(final String assetPath,
                                   final TempBlob metadataContent,
                                   final Payload payload,
                                   final AssetKind assetKind,
                                   final AttributesMap attributesMap) throws IOException
  {
    HashCode hash = null;
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
      for (Entry<String, Object> entry : attributesMap) {
        asset.formatAttributes().set(entry.getKey(), entry.getValue());
      }
      hash = hashVerifier.lookupHashFromAsset(tx, bucket, assetPath);
    }
    return saveAsset(tx, asset, metadataContent, payload, hash);
  }

  /**
   * Save an asset and create a blob
   *
   * @return blob content
   */
  private static Content saveAsset(final StorageTx tx,
                                   final Asset asset,
                                   final Supplier<InputStream> contentSupplier,
                                   final Payload payload,
                                   final HashCode hash) throws IOException
  {
    AttributesMap contentAttributes = null;
    String contentType = null;
    if (payload instanceof Content) {
      contentAttributes = ((Content) payload).getAttributes();
      contentType = payload.getContentType();
    }
    return saveAsset(tx, asset, contentSupplier, contentType, contentAttributes, hash);
  }

  /**
   * Save an asset and create a blob
   *
   * @return blob content
   */
  private static Content saveAsset(final StorageTx tx,
                                   final Asset asset,
                                   final Supplier<InputStream> contentSupplier,
                                   final String contentType,
                                   final AttributesMap contentAttributes,
                                   final HashCode hash) throws IOException
  {
    Content.applyToAsset(asset, maintainLastModified(asset, contentAttributes));
    AssetBlob assetBlob = tx.setBlob(
        asset, asset.name(), contentSupplier, HASH_ALGORITHMS, null, contentType, false
    );

    if(!hashVerifier.verify(hash, assetBlob.getHashes().get(MD5))) {
      return null;
    }
    asset.markAsDownloaded();
    tx.saveAsset(asset);
    return toContent(asset, assetBlob.getBlob());
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);

    if(DOWNLOAD_URL.equals(assetKind)) {
      return context.getRequest().getPath();
    }
    return CONAN_BINARY_BASE_URL + context.getRequest().getPath();
  }

  @Nonnull
  @Override
  protected CacheController getCacheController(@Nonnull final Context context) {
    final AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    return checkNotNull(cacheControllerHolder.get(assetKind.getCacheType()));
  }
}
