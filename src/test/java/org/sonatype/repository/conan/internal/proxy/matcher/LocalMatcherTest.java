package org.sonatype.repository.conan.internal.proxy.matcher;

import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_FILE;
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

public class LocalMatcherTest
    extends TestSupport
{
  @Mock
  private State state;

  private Map<String, String> tokens;

  private LocalMatcher underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new LocalMatcher();

    tokens = ImmutableMap.of(
        "group", "foo",
        "project", "bar",
        "version", "baz");
    when(state.getTokens()).thenReturn(tokens);
  }

  @Test
  public void whenRetrievingGroupShouldBeAtVersionMatcher() throws Exception {
    assertThat(underTest.group(state, CONAN_FILE), is("baz"));
  }

  @Test
  public void whenRetrievingProjectShouldBeAtGroupMatcher() throws Exception {
    assertThat(underTest.project(state, CONAN_FILE), is("foo"));
  }

  @Test
  public void whenRetrievingVersionShouldBeAtProjectMatcher() throws Exception {
    assertThat(underTest.version(state, CONAN_FILE), is("bar"));
  }

  @Test
  public void whenRetrievingGroupForDownloadUrlAssetShouldBeAsGroup() throws Exception {
    assertThat(underTest.group(state, DOWNLOAD_URL), is("foo"));
  }

  @Test
  public void whenRetrievingProjectForDownloadUrlAssetShouldBeAsProject() throws Exception {
    assertThat(underTest.project(state, DOWNLOAD_URL), is("bar"));
  }

  @Test
  public void whenRetrievingVersionForDownloadUrlAssetShouldBeAsVersion() throws Exception {
    assertThat(underTest.version(state, DOWNLOAD_URL), is("baz"));
  }
}