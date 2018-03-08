package org.sonatype.repository.conan.internal.proxy

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State

import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * @since 0.0.2
 */
class RemoteMatcher
    implements ConanMatcher
{
  static String group(final State state) {
    return match(state, "${GROUP}")
  }

  static String project(final State state) {
    return match(state, "${PROJECT}")
  }

  static String version(final State state) {
    return match(state, "${VERSION}")
  }
}
