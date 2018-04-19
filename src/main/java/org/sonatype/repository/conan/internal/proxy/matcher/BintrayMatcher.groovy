package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.repository.conan.internal.AssetKind

import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.or

/**
 * For all {@link AssetKind} the server sends files in Group/Project/Version order
 * @since 0.0.2
 */
class BintrayMatcher
    extends ConanMatcher
{
  public static final String NAME = "bintray"

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
    return new TokenMatcher("/{path:.*}/${DOWNLOAD_FORM}/download_urls")
  }

  private static TokenMatcher downloadUrlsPackagesMatcher() {
    return new TokenMatcher("/{path:.*}/${DOWNLOAD_FORM}/packages/{sha:.+}/download_urls")
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
                conanManifestMatcher(),
                conanManifestPackagesMatcher()
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
}
