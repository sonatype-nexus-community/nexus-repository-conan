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
package org.sonatype.repository.conan.internal.ui;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.BrowseNode;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.ProxyType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConanBrowseNodeFilterTest
    extends TestSupport
{
  @Mock
  private BrowseNode node;

  @Mock
  private RepositoryManager repositoryManager;

  private ConanBrowseNodeFilter underTest;

  private static final String repositoryName = "conan-test";

  @Before
  public void setUp() {
    underTest = new ConanBrowseNodeFilter(repositoryManager);
  }

  @Test
  public void downloadUrlsInHostedShouldNotBeVisible() {
    when(node.getName()).thenReturn("/path/download_urls");
    Repository repository = mock(Repository.class);
    when(repositoryManager.get(repositoryName)).thenReturn(repository);
    when(repository.getType()).thenReturn(new HostedType());

    assertFalse(underTest.test(node, repositoryName));
  }

  @Test
  public void downloadUrlsInProxyShouldBeVisible() {
    when(node.getName()).thenReturn("/path/download_urls");
    Repository repository = mock(Repository.class);
    when(repositoryManager.get(repositoryName)).thenReturn(repository);
    when(repository.getType()).thenReturn(new ProxyType());

    assertTrue(underTest.test(node, repositoryName));
  }
}
