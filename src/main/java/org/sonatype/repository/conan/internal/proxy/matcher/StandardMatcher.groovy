package org.sonatype.repository.conan.internal.proxy.matcher

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
 * For {@link AssetKind#DOWNLOAD_URL} the server sends files in Group/Project/Version order
 * for all other types it is in Version/Group/Project order
 * @since 0.0.2
 */
class StandardMatcher
    extends ConanMatcher
{
  public static final String NAME = "standard";

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
    return new TokenMatcher("/v1/conans/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/download_urls");
  }

  private static TokenMatcher downloadUrlsPackagesMatcher() {
    return new TokenMatcher("/v1/conans/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/packages/{sha:.+}/download_urls");
  }

  /**
   * Matches on the manifest files
   * @return matcher for initial and package conanmanifest.txt endpoints
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
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/export/conanmanifest.txt")
  }

  private static TokenMatcher conanManifestPackagesMatcher() {
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/package/{sha:.+}/conanmanifest.txt")
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
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/export/conanfile.py")
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
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/package/{sha:.+}/conaninfo.txt")
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
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/package/{sha:.+}/conan_package.tgz")
  }

  private static TokenMatcher conanSourcesMatcher() {
    new TokenMatcher("/{path:.*}/{${PROJECT}:.+}/{${VERSION}:.+}/{${GROUP}:.+}/{${STATE}:.+}/export/conan_sources.tgz")
  }
}
