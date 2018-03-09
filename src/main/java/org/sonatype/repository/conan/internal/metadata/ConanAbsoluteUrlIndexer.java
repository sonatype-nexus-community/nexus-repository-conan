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
package org.sonatype.repository.conan.internal.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyMap;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.view.Content.maintainLastModified;
import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_JSON;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_INDEX;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.findAsset;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.toContent;

/**
 * download_url files contain absolute paths to each asset
 *
 * This class removes the absolute address so as to redirect back to this repository
 *
 * @since 3.conan
 */
@Singleton
@Named
public class ConanAbsoluteUrlIndexer
    extends ComponentSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Nullable
  public TempBlob updateAbsoluteUrls(final TempBlob tempBlob,
                                     final Repository repository,
                                     final String assetName) {
    Map<String, URL> downloadUrlContents = readIndex(tempBlob.get(), assetName);
    Map<String, URL> indexes = new HashMap<>();

    for (Map.Entry<String, URL> entry : downloadUrlContents.entrySet()) {
      URL originalUrl = entry.getValue();
      URL indexUrl = getIndexedUrl(repository.getUrl(), entry.getValue().getPath());
      indexes.put(entry.getValue().getPath(), originalUrl);
      entry.setValue(indexUrl);
    }

    handleUpdatingIndexes(assetName, indexes, repository);
    return updateDownloadUrlContents(repository, downloadUrlContents);
  }

  @Nullable
  private TempBlob updateDownloadUrlContents(final Repository repository, final Map<String, URL> downloadUrlContents) {
    try {
      return convertFileToTempBlob(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(downloadUrlContents), repository);
    }
    catch (JsonProcessingException e) {
      log.warn("Unable to write to blob", e);
      return null;
    }
  }

  @Nullable
  private URL getIndexedUrl(final String repositoryUrl, final String path) {
    try {
      return new URL(repositoryUrl + path);
    }
    catch (MalformedURLException e) {
      log.error("Unable to create indexed url", e);
    }
    return null;
  }

  private TempBlob convertFileToTempBlob(final String resolvedMap, final Repository repository) {
    StorageFacet storageFacet = repository.facet(StorageFacet.class);
    return storageFacet.createTempBlob(new StringPayload(resolvedMap, defaultCharset(), null), HASH_ALGORITHMS);
  }

  @TransactionalStoreMetadata
  protected void handleUpdatingIndexes(final String assetName,
                                       final Map<String, URL> newIndexes,
                                       final Repository repository) {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(repository);
    Asset asset = findAsset(tx, bucket, assetName);
    if(asset == null) {
      asset = createIndexAsset(assetName, repository, tx, bucket);
    }

    try {
      Content content = toContent(asset, tx.requireBlob(asset.requireBlobRef()));
      Map<String, URL> currentIndexes = readIndex(content.openInputStream(), assetName);
      Map<String, URL> updatedIndexes = updateIndexes(currentIndexes, newIndexes);
      saveIndexes(tx, asset, updatedIndexes);
    }
    catch (IOException e) {
      log.warn("Unable to update index for {}", assetName, e);
    }
  }

  private Asset createIndexAsset(final String assetName,
                                 final Repository repository,
                                 final StorageTx tx,
                                 final Bucket bucket) {
    Asset asset = tx.createAsset(bucket, repository.getFormat());
    asset.name(assetName);
    asset.formatAttributes().set(P_ASSET_KIND, CONAN_INDEX.name());
    tx.saveAsset(asset);
    asset = findAsset(tx, bucket, assetName);
    try {
      saveIndexes(tx, asset, emptyMap());
    }
    catch (IOException e) {
      log.warn("Unable to save empty map to {}", assetName, e);
    }
    return asset;
  }

  private void saveIndexes(final StorageTx tx,
                           final Asset asset,
                           final Map<String, URL> updatedIndexes) throws IOException
  {
    ObjectMapper mapper = new ObjectMapper();
    String valueAsString = mapper.writeValueAsString(updatedIndexes);
    Content.applyToAsset(asset, maintainLastModified(asset, null));
    updateAsset(tx, asset, () -> new ByteArrayInputStream(valueAsString.getBytes()));
    asset.markAsDownloaded();
    tx.saveAsset(asset);
  }

  @TransactionalStoreMetadata
  public Map<String, URL> handleReadingIndexes(final String assetName,
                                               final Repository repository)
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(repository);
    Asset asset = findAsset(tx, bucket, assetName);
    if (asset == null) {
      log.error("Index {} not found", assetName);
      return emptyMap();
    }

    try {
      Content content = toContent(asset, tx.requireBlob(asset.requireBlobRef()));
      return readIndex(content.openInputStream(), assetName);
    }
    catch (IOException e) {
      log.warn("Unable to read index file for {}", assetName);
    }
    return emptyMap();
  }

  private void updateAsset(final StorageTx tx, final Asset asset, final Supplier<InputStream> stream) throws IOException {
    tx.setBlob(asset, asset.name(), stream , HASH_ALGORITHMS, null, APPLICATION_JSON, false);
  }

  private Map<String, URL> updateIndexes(final Map<String, URL> indexes, final Map<String, URL> lookups) {
    lookups.forEach((key, value) -> indexes.put(key, value));
    return indexes;
  }

  private Map<String, URL> readIndex(final InputStream stream, final String assetName) {
    ObjectMapper objectMapper = new ObjectMapper();

    TypeReference<HashMap<String, URL>> typeRef = new TypeReference<HashMap<String, URL>>() {};
    try {
      return objectMapper.readValue(stream, typeRef);
    }
    catch (IOException e) {
      log.warn("Unable to read index for asset {}", assetName, e);
    }
    return emptyMap();
  }
}

