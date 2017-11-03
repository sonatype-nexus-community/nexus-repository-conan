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
package com.sonatype.repository.conan.internal.proxy;

import com.sonatype.repository.conan.internal.ConanFormat;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.types.ProxyType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;

import static com.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.conanManifestMatcher;
import static com.sonatype.repository.conan.internal.proxy.ConanProxyRecipe.downloadUrlsMatcher;
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

  private ConanProxyRecipe recipe;

  @Before
  public void setUp() throws Exception {
    recipe = new ConanProxyRecipe(new ProxyType(), new ConanFormat());
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
    when(request.getPath()).thenReturn("/v1/conans/jsonformoderncpp/2.1.1/vthiery/stable/download_urls");
    assertTrue(downloadUrlsMatcher().matches(context));
  }


  @Test
  public void canMatchOnConanManifest() {
    //repository/conan-proxy            /vthiery/jsonformoderncpp/2.1.1/stable/export/conanmanifest.txt
    when(request.getPath()).thenReturn("/vthiery/jsonformoderncpp/2.1.1/stable/export/conanmanifest.txt");
    assertTrue(conanManifestMatcher().matches(context));
  }
}