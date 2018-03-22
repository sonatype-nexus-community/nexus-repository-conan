package org.sonatype.repository.conan.internal.proxy.matcher;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Route.Builder;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;
import org.sonatype.repository.conan.internal.metadata.ConanMetadata;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION;

/**
 * Abstract class that is used to define the ordering of the server paths
 *
 * @since 0.0.2
 */
public abstract class ConanMatcher
{
  public abstract Builder downloadUrls();

  public abstract Builder conanManifest();

  public abstract Builder conanFile();

  public abstract Builder conanInfo();

  public abstract Builder conanPackage();

  static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  public static ConanCoords getCoords(final Context context) {
    State state = matcherState(context);
    return new ConanCoords(group(state), project(state), version(state), channel(state));
  }

  public static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  public static String group(final State matcherState) {
    return match(matcherState, GROUP);
  }

  public static String project(final State matcherState) {
    return match(matcherState, PROJECT);
  }

  public static String version(final State matcherState) {
    return match(matcherState, VERSION);
  }

  public static String channel(final State matcherState) {
    return match(matcherState, STATE);
  }
}
