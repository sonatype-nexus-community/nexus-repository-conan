package org.sonatype.repository.conan.internal.proxy.matcher;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@Ignore
public class StandardMatcherTest
    extends TestSupport
{
  @Mock
  Request request;

  @Mock
  private Context context;

  @Mock
  Handler handler;

  private AttributesMap attributesMap;

  private StandardMatcher underTest;

  @Before
  public void setUp() throws Exception {
    attributesMap = new AttributesMap();
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributesMap);
    when(request.getAction()).thenReturn(HttpMethods.GET);

    underTest = new StandardMatcher();
  }

  @Test
  public void canMatchOnDownloadUrls() {
    when(request.getPath()).thenReturn("v1/conans/Hello/0.2/demo/testing/download_urls");
    Route route = underTest.downloadUrls().handler(handler).create();
    assertTrue(route.getMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("group"), is(equalTo("demo")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("Hello")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("0.2")));
  }
}