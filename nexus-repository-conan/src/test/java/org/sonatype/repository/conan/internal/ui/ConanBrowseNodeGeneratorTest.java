package org.sonatype.repository.conan.internal.ui;

import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.browse.BrowsePaths;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.DefaultComponent;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
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

    List<BrowsePaths> assetPath = underTest.computeAssetPaths(asset, component);

    assertThat(assetPath.size(), is(6));
    assertThat(assetPath.get(0).getBrowsePath(), is("vthiery"));
    assertThat(assetPath.get(1).getBrowsePath(), is("jsonformoderncpp"));
    assertThat(assetPath.get(2).getBrowsePath(), is("2.1.1"));
    assertThat(assetPath.get(3).getBrowsePath(), is("stable"));
    assertThat(assetPath.get(4).getBrowsePath(), is("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9"));
    assertThat(assetPath.get(5).getBrowsePath(), is("download_urls"));
  }


  @Test
  public void canComputeNonPackagePath() {
    Asset asset = createAsset(
        "/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");

    List<BrowsePaths> assetPath = underTest.computeAssetPaths(asset, component);

    assertThat(assetPath.size(), is(5));
    assertThat(assetPath.get(0).getBrowsePath(), is("vthiery"));
    assertThat(assetPath.get(1).getBrowsePath(), is("jsonformoderncpp"));
    assertThat(assetPath.get(2).getBrowsePath(), is("2.1.1"));
    assertThat(assetPath.get(3).getBrowsePath(), is("stable"));
    assertThat(assetPath.get(4).getBrowsePath(), is("download_urls"));
  }

  private Asset createAsset(String assetName) {
    Asset asset = mock(Asset.class);
    when(asset.name()).thenReturn(assetName);
    return asset;
  }
}
