package org.sonatype.repository.conan.internal.proxy

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State

import static com.google.common.base.Preconditions.checkNotNull

/**
 * @since 0.0.2
 */
trait ConanMatcher
{
  abstract static String group(final State state)

  abstract static String project(final State state)

  abstract static String version(final State state)

  static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state)
    String result = state.getTokens().get(name)
    checkNotNull(result)
    return result
  }
}
