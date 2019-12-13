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
package org.sonatype.repository.conan.internal.hosted

import org.sonatype.goodies.testsupport.TestSupport

import org.junit.Before
import org.junit.Test

class UploadUrlManagerTest
  extends TestSupport
{
  private UploadUrlManager underTest

  @Before
  void setUp() throws Exception {
    underTest = new UploadUrlManager()
  }

  @Test
  void whenInputStreamThenConvertsKeyToValueDesition() throws Exception {
    String testJson = '{' +
        '"manifest.txt":16,' +
        '"conanfile.py":68' +
        '}'
    def inputStream = new ByteArrayInputStream(testJson.getBytes())

    def actualResponse = underTest.convertKeys('/v1/conans/group/version/project/exports/', inputStream)

    String expectedResponse = '{' +
        '"manifest.txt":"/v1/conans/group/version/project/exports/manifest.txt",' +
        '"conanfile.py":"/v1/conans/group/version/project/exports/conanfile.py"' +
        '}'

    assert actualResponse == expectedResponse
  }

  @Test
  void whenInputStreamThenConvertsValueToUrlDestination() throws Exception {
    String testJson = '{' +
        '"manifest.txt":"/v1/conans/group/version/project/exports/manifest.txt",' +
        '"conanfile.py":"/v1/conans/group/version/project/exports/conanfile.py"' +
        '}'
    def inputStream = new ByteArrayInputStream(testJson.getBytes())

    def actualResponse = underTest.prefixToValues("http://localhost:9300", inputStream)

    String expectedResponse = '{' +
        '"manifest.txt":"http://localhost:9300/v1/conans/group/version/project/exports/manifest.txt",' +
        '"conanfile.py":"http://localhost:9300/v1/conans/group/version/project/exports/conanfile.py"' +
        '}'

    assert actualResponse == expectedResponse
  }
}
