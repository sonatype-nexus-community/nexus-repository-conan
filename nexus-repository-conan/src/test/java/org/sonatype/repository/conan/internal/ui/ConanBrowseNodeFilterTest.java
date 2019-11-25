package org.sonatype.repository.conan.internal.ui;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.storage.BrowseNode;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ConanBrowseNodeFilterTest
    extends TestSupport
{
  @Mock
  private BrowseNode node;

  private ConanBrowseNodeFilter underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new ConanBrowseNodeFilter();
  }

  @Test
  public void filteredOnDownloadUrls() throws Exception {
    when(node.getName()).thenReturn("/some/path/download_urls");

    assertFalse(underTest.test(node, ""));
  }

  @Test
  public void notFilteredOnOtherUrls() throws Exception {
    when(node.getName()).thenReturn("/some/path/foobar");

    assertTrue(underTest.test(node, ""));
  }
}
