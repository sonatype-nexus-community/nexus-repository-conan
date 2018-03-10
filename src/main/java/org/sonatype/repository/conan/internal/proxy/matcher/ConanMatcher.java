package org.sonatype.repository.conan.internal.proxy.matcher;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract class that is used to define the ordering of the server paths
 *
 * @since 0.0.2
 */
public abstract class ConanMatcher
{
  public abstract String group(final State state);

  public abstract String project(final State state);

  public abstract String version(final State state);

  static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  public static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }
}
