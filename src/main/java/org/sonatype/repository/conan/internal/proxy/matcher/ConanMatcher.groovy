package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State
import org.sonatype.repository.conan.internal.metadata.ConanCoords

import static com.google.common.base.Preconditions.checkNotNull
import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.or
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.DIGEST
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

class ConanMatcher
{
  protected static String DOWNLOAD_FORM = "{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}"

  protected static String STANDARD_FORM = "{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}"

  /**
   * Matches on urls ending with download_urls
   * @return matcher for initial and package download_urls endpoints
   */
  Builder downloadUrls() {
    return new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
                downloadUrlsPackagesMatcher(),
                downloadUrlsMatcher()
            )
        )
    )
  }

  private static TokenMatcher downloadUrlsMatcher() {
    return new TokenMatcher("{path:.*}/${DOWNLOAD_FORM}/download_urls")
  }

  private static TokenMatcher downloadUrlsPackagesMatcher() {
    return new TokenMatcher("{path:.*}/${DOWNLOAD_FORM}/packages/{sha:.+}/download_urls")
  }

  /**
   * Matches on the manifest files
   * @return matcher for initial and package conanmanifest.txt
   */
  Builder conanManifest() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
                conanManifestPackagesMatcher(),
                conanManifestMatcher()
            )
        )
    )
  }

  private static TokenMatcher conanManifestMatcher() {
    new TokenMatcher("/${STANDARD_FORM}/conanmanifest.txt")
  }

  private static TokenMatcher conanManifestPackagesMatcher() {
    new TokenMatcher("/${STANDARD_FORM}/packages/{sha:.+}/conanmanifest.txt")
  }

  /**
   * Matches on conanfile.py
   * @return matcher for conanfile.py
   */
  Builder conanFile() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            conanFileMatcher()
        )
    )
  }

  private static TokenMatcher conanFileMatcher() {
    new TokenMatcher("/${STANDARD_FORM}/conanfile.py")
  }

  /**
   * Matches on conaninfo.txt
   * @return matcher for conaninfo.txt
   */
  Builder conanInfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            conanInfoMatcher()
        )
    )
  }

  private static TokenMatcher conanInfoMatcher() {
    new TokenMatcher("/${STANDARD_FORM}/packages/{sha:.+}/conaninfo.txt")
  }

  /**
   * Matches on conan_package.tgz
   * @return matcher for conan_package.tgz
   */
  Builder conanPackage() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            conanPackageMatcher()
        )
    )
  }

  private static TokenMatcher conanPackageMatcher() {
    new TokenMatcher("/${STANDARD_FORM}/packages/{sha:.+}/conan_package.tgz")
  }

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
