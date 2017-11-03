package com.sonatype.repository.conan.internal.proxy

import com.sonatype.repository.conan.internal.metadata.ConanMetadata

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
import static com.sonatype.repository.conan.internal.metadata.ConanMetadata.AUTHOR
import static com.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static com.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION
import static org.sonatype.nexus.common.hash.HashAlgorithm.MD5
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA512
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME

class ConanProxyHelper
{
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA256, SHA1, SHA512, MD5)

  static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class)
  }

  static String buildAssetPath(final TokenMatcher.State matcherState) {
    String project = project(matcherState)
    String version = version(matcherState)
    String author = author(matcherState)
    return project + "/" + version + "/" + author + "/download_urls";
  }

  private static String project(final State state) {
    return match(state, "${PROJECT}")
  }

  private static String version(final State state) {
    return match(state, "${VERSION}")
  }

  private static String author(final State state) {
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
