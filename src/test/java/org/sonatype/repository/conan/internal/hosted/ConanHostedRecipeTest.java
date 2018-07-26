package org.sonatype.repository.conan.internal.hosted;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
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
import static org.sonatype.nexus.repository.http.HttpMethods.POST;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConanPackageZip;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConanfile;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadConaninfo;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadManifest;
import static org.sonatype.repository.conan.internal.hosted.ConanHostedRecipe.uploadUrls;

public class ConanHostedRecipeTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  Context context;

  @Mock
  Handler handler;

  AttributesMap attributesMap;

  @Before
  public void setUp() {
    attributesMap = new AttributesMap();
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributesMap);
  }

  @Test
  public void canMatchOnConanUpload_url() {
    when(request.getAction()).thenReturn(POST);
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/upload_urls");

    assertTrue(uploadUrls().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanPackage() {
    when(request.getAction()).thenReturn(POST);
    when(request.getPath()).thenReturn("/v1/conans/project/2.1.1/group/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/upload_urls");

    assertTrue(uploadUrls().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConanUploadManifest() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/conanmanifest.txt");

    assertTrue(uploadManifest().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanUploadManifestPackage() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conanmanifest.txt");

    assertTrue(uploadManifest().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConanfileUpload() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/conanfile.py");

    assertTrue(uploadConanfile().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo(null)));
  }

  @Test
  public void canMatchOnConanfileUploadPackage() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conanfile.py");

    assertTrue(uploadConanfile().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }

  @Test
  public void canMatchOnConaninfoUpload() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/conaninfo.txt");

    assertTrue(uploadConaninfo().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo(null)));
  }

  @Test
  public void canMatchOnConanPackageZipUploadPackage() {
    when(request.getAction()).thenReturn(PUT);
    when(request.getPath()).thenReturn("/v1/conans/group/project/2.1.1/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conan_package.tgz");

    assertTrue(uploadConanPackageZip().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("group")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("project")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
    assertThat(matcherState.getTokens().get("sha"), is(equalTo("5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9")));
  }
}