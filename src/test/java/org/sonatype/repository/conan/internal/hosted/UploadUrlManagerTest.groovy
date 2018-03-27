package org.sonatype.repository.conan.internal.hosted

import org.sonatype.goodies.testsupport.TestSupport

import org.junit.Before
import org.junit.Test

class UploadUrlManagerTest
  extends TestSupport
{
  private UploadUrlManager underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new UploadUrlManager()
  }

  @Test
  public void whenInputStreamThenConvertsKeyToValueDesition() throws Exception {
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
  public void whenInputStreamThenConvertsValueToUrlDestination() throws Exception {
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
