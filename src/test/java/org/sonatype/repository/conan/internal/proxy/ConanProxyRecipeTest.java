/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.conan.internal.proxy;


import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static org.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.conanFileMatcher;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.conanInfoMatcher;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.conanManifestMatcher;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.conanPackageMatcher;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.downloadUrlsMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ConanProxyRecipeTest
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

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void canMatchOnDownloadUrls() {
    when(request.getPath()).thenReturn("/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");
    assertTrue(downloadUrlsMatcher().matches(context));
  }

  @Test
  public void canMatchOnDownloadUrls2() {
    when(request.getPath()).thenReturn("/v1/conans/zlib/1.2.11/conan/stable/packages/8018a4df6e7d2b4630a814fa40c81b85b9182d2b/download_urls");
    assertTrue(downloadUrlsMatcher().matches(context));
  }

  @Test
  public void canMatchOnConanfile() {
    when(request.getPath()).thenReturn("/vthiery/jsonformoderncpp/2.1.1/stable/export/conanfile.py");
    assertTrue(conanFileMatcher().matches(context));
  }

  @Test
  public void canMatchOnConanManifest() {
    when(request.getPath()).thenReturn("/vthiery/jsonformoderncpp/2.1.1/stable/export/conanmanifest.txt");
    assertTrue(conanManifestMatcher().matches(context));
  }

  @Test
  public void canMatchOnConanInfo() {
    when(request.getPath()).thenReturn("/v1/files/vthiery/jsonformoderncpp/2.1.1/stable/package/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conaninfo.txt");
    assertTrue(conanInfoMatcher().matches(context));
  }

  @Test
  public void canMatchOnConanPackage() {
    when(request.getPath()).thenReturn("/vthiery/jsonformoderncpp/2.1.1/stable/package/5ab84d6acfe1f23c4fae0ab88f26e3a396351ac9/conan_package.tgz");
    assertTrue(conanPackageMatcher().matches(context));
    TokenMatcher.State matcherState = attributesMap.require(TokenMatcher.State.class);
    assertThat(matcherState.getTokens().get("author"), is(equalTo("vthiery")));
    assertThat(matcherState.getTokens().get("project"), is(equalTo("jsonformoderncpp")));
    assertThat(matcherState.getTokens().get("version"), is(equalTo("2.1.1")));
  }
}