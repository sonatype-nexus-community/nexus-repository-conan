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
package org.sonatype.repository.conan.internal.proxy.matcher;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.view.handlers.HighAvailabilitySupportChecker;
import org.sonatype.repository.conan.internal.ConanFormat;
import org.sonatype.repository.conan.internal.proxy.v1.ConanProxyApiV1;
import org.sonatype.repository.conan.internal.proxy.v1.ConanProxyRecipe;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConanProxyRecipeTest
    extends TestSupport
{
  private static final String CONAN_FORMAT = "conan";


  @Mock
  private ConanFormat conanFormat;

  @Mock
  private HighAvailabilitySupportChecker highAvailabilitySupportChecker;

  @Mock
  private ConanProxyApiV1 conanApiV1;


  private ConanProxyRecipe conanProxyRecipe;

  @Before
  public void setUp() {
    when(conanFormat.getValue()).thenReturn(CONAN_FORMAT);
    conanProxyRecipe = new ConanProxyRecipe(new ProxyType(), conanFormat, conanApiV1);
    conanProxyRecipe.setHighAvailabilitySupportChecker(highAvailabilitySupportChecker);
  }

  @Test
  public void haEnabledProxyRepository() {
    when(highAvailabilitySupportChecker.isSupported(CONAN_FORMAT)).thenReturn(true);
    assertThat(conanProxyRecipe.isFeatureEnabled(), is(equalTo(true)));
    verify(highAvailabilitySupportChecker).isSupported(CONAN_FORMAT);
  }

  @Test
  public void haDisabledProxyRepository() {
    when(highAvailabilitySupportChecker.isSupported(CONAN_FORMAT)).thenReturn(false);
    assertThat(conanProxyRecipe.isFeatureEnabled(), is(equalTo(false)));
    verify(highAvailabilitySupportChecker).isSupported(CONAN_FORMAT);
  }
}
