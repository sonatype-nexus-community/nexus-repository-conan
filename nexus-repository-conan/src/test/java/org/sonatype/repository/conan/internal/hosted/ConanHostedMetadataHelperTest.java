package org.sonatype.repository.conan.internal.hosted;

import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.repository.conan.internal.hosted.v1.ConanHostedMetadataHelper;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_EXPORT;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_FILE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_INFO;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_MANIFEST;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_SOURCES;

/**
 * @author Maksim Lukaretskiy
 */
public class ConanHostedMetadataHelperTest
    extends TestSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String path = "should be ignored";

  private static final String group = "vthiery";

  private static final String project = "jsonformoderncpp";

  private static final String version = "3.7.0";

  private static final String channel = "stable";

  private static final String sha = "5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9";

  @Test
  public void generateAssetPackagesDownloadUrls() {
    String assetPath =
        String.format("%s%s/%s/%s/%s/packages/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel, sha);

    ImmutableMap<String, String> expected = ImmutableMap.of(
        CONAN_INFO.getFilename(), String.format("%s/%s", assetPath, CONAN_INFO.getFilename()),
        CONAN_PACKAGE.getFilename(), String.format("%s/%s", assetPath, CONAN_PACKAGE.getFilename()),
        CONAN_MANIFEST.getFilename(), String.format("%s/%s", assetPath, CONAN_MANIFEST.getFilename())
    );
    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, sha);
    Map<String, String> actual = ConanHostedMetadataHelper.generateAssetPackagesDownloadUrls(conanCoords);

    assertThat(actual, is(expected));
  }

  @Test
  public void generatePackagesDownloadUrlsAsJson() throws Exception {
    String repositoryURL = "http://localhost:8081/repositories/conan-proxy";

    String assetPath =
        String.format("%s%s/%s/%s/%s/packages/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel, sha);

    String expected = MAPPER.writeValueAsString(ImmutableMap.of(
        CONAN_INFO.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_INFO.getFilename()),
        CONAN_PACKAGE.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_PACKAGE.getFilename()),
        CONAN_MANIFEST.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_MANIFEST.getFilename())
    ));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, sha);
    String actual = ConanHostedMetadataHelper.generatePackagesDownloadUrlsAsJson(conanCoords, repositoryURL);

    assertThat(actual, is(expected));
  }

  @Test
  public void generateDownloadUrlsAsJson() throws Exception {
    String repositoryURL = "http://localhost:8081/repositories/conan-proxy";

    String assetPath =
        String.format("%s%s/%s/%s/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel);

    String expected = MAPPER.writeValueAsString(ImmutableMap.of(
        CONAN_EXPORT.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_EXPORT.getFilename()),
        CONAN_SOURCES.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_SOURCES.getFilename()),
        CONAN_MANIFEST.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_MANIFEST.getFilename()),
        CONAN_FILE.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_FILE.getFilename())
    ));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, null);
    String actual = ConanHostedMetadataHelper.generateDownloadUrlsAsJson(conanCoords, repositoryURL);

    assertThat(actual, is(expected));
  }
}
