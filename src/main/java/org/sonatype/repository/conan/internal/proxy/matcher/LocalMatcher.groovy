package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State
import org.sonatype.repository.conan.internal.AssetKind

import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * For {@link AssetKind#DOWNLOAD_URL} the server sends files in Group/Project/Version order
 * for all other types it is in Version/Group/Project order
 * @since 0.0.2
 */
class LocalMatcher
    extends ConanMatcher
{
  public static final String NAME = "local";

  String group(final State state, final AssetKind assetKind) {
    if(DOWNLOAD_URL == assetKind) {
      return match(state, "${GROUP}")
    }
    return match(state, "${VERSION}")
  }

  String project(final State state, final AssetKind assetKind) {
    if(DOWNLOAD_URL == assetKind) {
      return match(state, "${PROJECT}")
    }
    return match(state, "${GROUP}")
  }

  String version(final State state, final AssetKind assetKind) {
    if(DOWNLOAD_URL == assetKind) {
      return match(state, "${VERSION}")
    }
    return match(state, "${PROJECT}")
  }
}
