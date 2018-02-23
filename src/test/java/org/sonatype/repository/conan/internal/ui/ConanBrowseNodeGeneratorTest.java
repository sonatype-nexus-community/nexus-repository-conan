package org.sonatype.repository.conan.internal.ui;

import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.DefaultComponent;

import org.junit.Before;
import org.junit.Test;

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
  public void setUp() throws Exception {
    component = new DefaultComponent().group("vthiery")
        .name("jsonformoderncpp")
        .version("2.1.1");
  }

  @Test
  public void canComputePackagePath() {
    Asset asset = createAsset(
        "v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/download_urls");

    List<String> assetPath = underTest.computeAssetPath(asset, component);

    assertThat(assetPath, contains("vthiery", "jsonformoderncpp", "2.1.1", "5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9", "download_urls"));
  }


  @Test
  public void canComputeNonPackagePath() {
    Asset asset = createAsset(
        "/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");

    List<String> assetPath = underTest.computeAssetPath(asset, component);

    assertThat(assetPath, contains("vthiery", "jsonformoderncpp", "2.1.1", "download_urls"));

  }

  private Asset createAsset(String assetName) {
    Asset asset = mock(Asset.class);
    when(asset.name()).thenReturn(assetName);
    return asset;
  }
}