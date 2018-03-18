package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State
import org.sonatype.repository.conan.internal.AssetKind
import org.sonatype.repository.conan.internal.proxy.matcher.ConanMatcher

import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * For all {@link AssetKind} the server sends files in Group/Project/Version order
 * @since 0.0.2
 */
class RemoteMatcher
    extends ConanMatcher
{
  public static final String NAME = "remote";

  String group(final State state, final AssetKind assetKind) {
    return match(state, "${GROUP}")
  }

  String project(final State state, final AssetKind assetKind) {
    return match(state, "${PROJECT}")
  }

  String version(final State state, final AssetKind assetKind) {
    return match(state, "${VERSION}")
  }
}
