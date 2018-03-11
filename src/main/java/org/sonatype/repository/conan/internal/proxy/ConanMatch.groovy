package org.sonatype.repository.conan.internal.proxy

import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State

/**
 *
 * Remote conan servers behave differently to locally running servers in that
 * the ordering of Group, Project, Version on some paths differ
 * Remote server: Group / Project / Version
 * Local server: Project / Version / Group
 * @since 0.0.2
 */
class ConanMatch
{
  static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class)
  }

  static String group(final State state) {
    if (isRemoteServer(state)) {
      return RemoteMatcher.group(state)
    }
    return LocalMatcher.group(state)
  }

  static String project(final State state) {
    if (isRemoteServer(state)) {
      return RemoteMatcher.project(state)
    }
    else {
      return LocalMatcher.project(state)
    }
  }

  static String version(final State state) {
    if (isRemoteServer(state)) {
      return RemoteMatcher.version(state)
    }
    return LocalMatcher.version(state)
  }

  private static boolean isRemoteServer(State state) {
    state.getTokens().get("version").contains(".")
  }
}
