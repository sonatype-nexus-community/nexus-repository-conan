package org.sonatype.repository.conan.internal.proxy.matcher

import org.sonatype.nexus.repository.view.Matcher
import org.sonatype.nexus.repository.view.Route.Builder
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher
import org.sonatype.repository.conan.internal.AssetKind

import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.and
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.or
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION

/**
 * For all {@link AssetKind} the server sends files in Group/Project/Version order
 * @since 0.0.2
 */
class BintrayMatcher
    extends ConanMatcher
{
  public static final String NAME = "bintray";

  private static final String VERSION_URL = "/v1/conans";

  private static String STANDARD_FORM = "{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}";

  private static String BINTRAY_FORM = "{${GROUP}:.+}/{${PROJECT}:.+}/{${VERSION}:.+}/{${STATE}:.+}"

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
    );
  }

  private static TokenMatcher downloadUrlsMatcher() {
    return new TokenMatcher("${VERSION_URL}/${STANDARD_FORM}/download_urls")
  }

  private static TokenMatcher downloadUrlsPackagesMatcher() {
    return new TokenMatcher("${VERSION_URL}/${STANDARD_FORM}/packages/{sha:.+}/download_urls")
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
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/export/conanmanifest.txt")
  }

  private static TokenMatcher conanManifestPackagesMatcher() {
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/package/{sha:.+}/conanmanifest.txt")
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
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/export/conanfile.py")
  }

  /**
   * Matches on conaninfo.txt
   * @return matcher for conaninfo.txt
   */
  public Builder conanInfo() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            conanInfoMatcher()
        )
    )
  }

  private static TokenMatcher conanInfoMatcher() {
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/package/{sha:.+}/conaninfo.txt")
  }

  /**
   * Matches on conan_package.tgz
   * @return matcher for conan_package.tgz
   */
  Builder conanPackage() {
    new Builder().matcher(
        and(
            new ActionMatcher(GET, HEAD),
            or(
                conanPackageMatcher(),
                conanSourcesMatcher()
            )
        )
    )
  }

  private static TokenMatcher conanPackageMatcher() {
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/package/{sha:.+}/conan_package.tgz")
  }

  private static TokenMatcher conanSourcesMatcher() {
    new TokenMatcher("/{path:.*}/${BINTRAY_FORM}/export/conan_sources.tgz")
  }
}
