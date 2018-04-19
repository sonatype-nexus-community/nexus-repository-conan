package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State
import org.sonatype.repository.conan.internal.metadata.ConanCoords

import static com.google.common.base.Preconditions.checkNotNull
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.DIGEST
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

abstract class ConanMatcher
{
  protected static final String VERSION_URL = "/v1/conans"

  protected static String STANDARD_FORM = "{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}"

  abstract Builder downloadUrls()

  abstract Builder conanManifest()

  abstract Builder conanFile()

  abstract Builder conanInfo()

  abstract Builder conanPackage()

  static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state)
    return state.getTokens().get(name)
  }

  static ConanCoords getCoords(final Context context) {
    State state = matcherState(context)
    return new ConanCoords(group(state), project(state), version(state), channel(state), sha(state))
  }

  static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class)
  }

  static String group(final State matcherState) {
    return match(matcherState, GROUP)
  }

  static String project(final State matcherState) {
    return match(matcherState, PROJECT)
  }

  static String version(final State matcherState) {
    return match(matcherState, VERSION)
  }

  static String channel(final State matcherState) {
    return match(matcherState, STATE)
  }

  static String sha(final State matcherState) {
    return match(matcherState, DIGEST)
  }
}
