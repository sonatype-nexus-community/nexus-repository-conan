package org.sonatype.repository.conan.internal.hosted;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.sonatype.goodies.testsupport.hamcrest.DiffMatchers.equalTo;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConanPackageZipMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConanfileMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConanfilePackagesMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConaninfoMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadManifestMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadManifestPackagesMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadUrlsMatcher;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadUrlsPackagesMatcher;

public class ConanHostedRecipeTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  Context context;

  AttributesMap attributesMap;

  @Before
  public void setUp() throws Exception {
    attributesMap = new AttributesMap();
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributesMap);
  }

  @Test
  public void canMatchOnConanUpload_url() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/upload_urls");
    assertTrue(uploadUrlsMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanPackage() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/upload_urls");
    assertTrue(uploadUrlsPackagesMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConanUploadManifest() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/conanmanifest.txt");
    assertTrue(uploadManifestMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanUploadManifestPackage() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conanmanifest.txt");
    assertTrue(uploadManifestPackagesMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConanfileUpload() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/conanfile.py");
    assertTrue(uploadConanfileMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo(null)));
  }

  @Test
  public void canMatchOnConanfileUploadPackage() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conanfile.py");
    assertTrue(uploadConanfilePackagesMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConaninfoUpload() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/conaninfo.txt");
    assertTrue(uploadConaninfoMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo(null)));
  }

  @Test
  public void canMatchOnConanPackageZipUploadPackage() {
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conan_package.tgz");
    assertTrue(uploadConanPackageZipMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }
}