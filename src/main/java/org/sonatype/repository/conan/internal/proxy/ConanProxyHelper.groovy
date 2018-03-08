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

import org.sonatype.nexus.blobstore.api.Blob
import org.sonatype.nexus.common.hash.HashAlgorithm
import org.sonatype.nexus.repository.storage.Asset
import org.sonatype.nexus.repository.storage.Bucket
import org.sonatype.nexus.repository.storage.StorageTx
import org.sonatype.nexus.repository.view.Content
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State
import org.sonatype.nexus.repository.view.payloads.BlobPayload

import com.google.common.collect.ImmutableList

import static com.google.common.base.Preconditions.checkNotNull
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA512
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.AUTHOR
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * @since 0.0.1
 */
class ConanProxyHelper
{
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA256, SHA1, SHA512, MD5)

  static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class)
  }

  static String buildAssetPath(final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  static String project(final State state) {
    return match(state, "${PROJECT}")
  }

  static String version(final State state) {
    return match(state, "${VERSION}")
  }

  static String group(final State state) {
    return match(state, "${AUTHOR}")
  }

  private static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state)
    String result = state.getTokens().get(name)
    checkNotNull(result)
    return result
  }

  static Asset findAsset(final StorageTx tx, final Bucket bucket, final String assetName) {
    return tx.findAssetWithProperty(P_NAME, assetName, bucket)
  }

  static Content toContent(final Asset asset, final Blob blob) {
    Content content = new Content(new BlobPayload(blob, asset.requireContentType()))
    Content.extractFromAsset(asset, HASH_ALGORITHMS, content.getAttributes())
    return content
  }
}
