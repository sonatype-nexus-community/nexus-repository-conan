package org.sonatype.repository.conan.internal.proxy.matcher;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class BintrayMatcherTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  private Context context;

  @Mock
  Handler handler;

  private AttributesMap attributesMap;

  private BintrayMatcher underTest;

  @Before
  public void setUp() throws Exception {
    attributesMap = new AttributesMap();
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributesMap);
    when(request.getAction()).thenReturn(HttpMethods.GET);

    underTest = new BintrayMatcher();
  }

  @Test
  public void canMatchOnDownloadUrls() {
    when(request.getPath()).thenReturn("/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");
    assertTrue(underTest.downloadUrls().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnPackagesDownloadUrl() {
    when(request.getPath()).thenReturn("/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/packages/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/download_urls");
    assertTrue(underTest.downloadUrls().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnAlternatePackagesDownloadUrl() {
    when(request.getPath()).thenReturn("/v1/conans/zlib/1.2.11/conan/stable/packages/8018a4df6e7d2b4630a814fa40c81b85b9182d2b/download_urls");
    assertTrue(underTest.downloadUrls().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("conan")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("zlib")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("1.2.11")));
  }

  @Test
  public void canMatchOnConanfile() {
    when(request.getPath()).thenReturn("/conan/conan/conan-center/v1/files/vthiery/jsonformoderncpp/2.1.1/stable/export/conanfile.py");
    assertTrue(underTest.conanFile().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanManifest() {
    when(request.getPath()).thenReturn("/conan/conan/conan-center/v1/files/vthiery/jsonformoderncpp/2.1.1/stable/export/conanmanifest.txt");
    assertTrue(underTest.conanManifest().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanInfo() {
    when(request.getPath()).thenReturn("/conan/conan/conan-center/v1/files/vthiery/jsonformoderncpp/2.1.1/stable/package/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conaninfo.txt");
    assertTrue(underTest.conanInfo().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }

  @Test
  public void canMatchOnConanPackage() {
    when(request.getPath()).thenReturn("/conan/conan/conan-center/v1/files/vthiery/jsonformoderncpp/2.1.1/stable/package/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conan_package.tgz");
    assertTrue(underTest.conanPackage().handler(handler).create().getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }
}