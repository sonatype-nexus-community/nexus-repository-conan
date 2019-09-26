package org.sonatype.repository.conan.internal.ui;

import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.browse.BrowsePaths;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.DefaultComponent;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConanBrowseNodeGeneratorTest
    extends TestSupport
{
  Component component;

  ConanBrowseNodeGenerator underTest = new ConanBrowseNodeGenerator();

  @Before
  public void setUp() {
    component = new DefaultComponent().group("vthiery")
        .name("jsonformoderncpp")
        .version("2.1.1");
  }

  @Test
  public void canComputePackagePath() {
    Asset asset = createAsset(
        "v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/download_urls");

    List<String> assetPath = underTest.computeAssetPath(asset, component);

    assertThat(assetPath, contains("vthiery", "jsonformoderncpp", "2.1.1", "stable", "5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9", "download_urls"));
  }


  @Test
  public void canComputeNonPackagePath() {
    Asset asset = createAsset(
        "/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");

    List<String> assetPath = underTest.computeAssetPath(asset, component);

    assertThat(assetPath, contains("vthiery", "jsonformoderncpp", "2.1.1", "stable", "download_urls"));
  }

  private void assertPaths(List<BrowsePaths> paths, List<String> expectedPaths) {
    assertThat(expectedPaths.size(), is(expectedPaths.size()));
    assertThat(paths.size(), is(expectedPaths.size()));

    String requestPath = "";

    for (int i = 0 ; i < expectedPaths.size() ; i++) {
      requestPath += expectedPaths.get(i);
      if (i < expectedPaths.size() - 1) {
        requestPath += "/";
      }
      assertThat(paths.get(i).getBrowsePath(), is(expectedPaths.get(i)));
      assertThat(paths.get(i).getRequestPath(), is(requestPath));
    }
  }

  private Asset createAsset(String assetName) {
    Asset asset = mock(Asset.class);
    when(asset.name()).thenReturn(assetName);
    return asset;
  }
}