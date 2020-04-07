package org.sonatype.repository.conan.internal.hosted;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.Spy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
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
public class ConanHostedMetadataFacetSupportTest
    extends TestSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String path = "should be ignored";

  private static final String group = "vthiery";

  private static final String project = "jsonformoderncpp";

  private static final String version = "3.7.0";

  private static final String channel = "stable";

  private static final String sha = "5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9";

  @Spy
  private ConanHostedMetadataFacetSupport conanHostedMetadataFacetSupport = new ConanHostedMetadataFacetSupport();

  @Test
  public void generateAssetPackagesDownloadUrls() throws Exception {
    String assetPath =
        String.format("%s%s/%s/%s/%s/packages/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel, sha);

    Mockito.doReturn("some_hash_1").when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_INFO.getFilename()));
    Mockito.doReturn("some_hash_2").when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_PACKAGE.getFilename()));
    Mockito.doReturn("some_hash_3").when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_MANIFEST.getFilename()));

    String expected = MAPPER.writeValueAsString(ImmutableMap.of(
        CONAN_INFO.getFilename(), "some_hash_1",
        CONAN_PACKAGE.getFilename(), "some_hash_2",
        CONAN_MANIFEST.getFilename(), "some_hash_3"
    ));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, sha);
    String actual = conanHostedMetadataFacetSupport.generatePackageSnapshotAsJson(conanCoords);

    assertThat(actual, is(expected));
  }

  @Test
  public void generateAssetPackagesDownloadUrlsHashNotFound() throws Exception {
    String assetPath =
        String.format("%s%s/%s/%s/%s/packages/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel, sha);

    Mockito.doReturn(null).when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_INFO.getFilename()));
    Mockito.doReturn(null).when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_PACKAGE.getFilename()));
    Mockito.doReturn(null).when(conanHostedMetadataFacetSupport)
        .getHash(String.format("%s/%s", assetPath, CONAN_MANIFEST.getFilename()));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, sha);
    String actual = conanHostedMetadataFacetSupport.generatePackageSnapshotAsJson(conanCoords);

    assertThat(actual, nullValue());
  }

  @Test
  public void generateUploadPackagesUrlsAsJson() throws Exception {
    String repositoryURL = "http://localhost:8081/repositories/conan-proxy";

    String assetPath =
        String.format("%s%s/%s/%s/%s/packages/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel, sha);

    ImmutableSet<String> assetsToUpload = ImmutableSet
        .of(CONAN_INFO.getFilename(), CONAN_PACKAGE.getFilename(), CONAN_MANIFEST.getFilename());

    String expected = MAPPER.writeValueAsString(ImmutableMap.of(
        CONAN_INFO.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_INFO.getFilename()),
        CONAN_PACKAGE.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_PACKAGE.getFilename()),
        CONAN_MANIFEST.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_MANIFEST.getFilename())
    ));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, sha);
    String actual =
        conanHostedMetadataFacetSupport.generatePackagesUploadUrlsAsJson(conanCoords, repositoryURL, assetsToUpload);

    assertThat(actual, is(expected));
  }

  @Test
  public void generateUploadUrlsAsJson() throws Exception {
    String repositoryURL = "http://localhost:8081/repositories/conan-proxy";

    String assetPath =
        String.format("%s%s/%s/%s/%s", ConanHostedHelper.CONAN_HOSTED_PREFIX, group, project, version,
            channel);

    ImmutableSet<String> assetsToUpload = ImmutableSet
        .of(CONAN_EXPORT.getFilename(), CONAN_SOURCES.getFilename(), CONAN_MANIFEST.getFilename(),
            CONAN_FILE.getFilename());

    String expected = MAPPER.writeValueAsString(ImmutableMap.of(
        CONAN_EXPORT.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_EXPORT.getFilename()),
        CONAN_SOURCES.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_SOURCES.getFilename()),
        CONAN_MANIFEST.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_MANIFEST.getFilename()),
        CONAN_FILE.getFilename(), String.format("%s/%s/%s", repositoryURL, assetPath, CONAN_FILE.getFilename())
    ));

    ConanCoords conanCoords = new ConanCoords(path, group, project, version, channel, null);
    String actual =
        conanHostedMetadataFacetSupport.generateUploadUrlsAsJson(conanCoords, repositoryURL, assetsToUpload);

    assertThat(actual, is(expected));
  }
}
