package org.sonatype.repository.conan.internal.metadata;

import java.io.IOException;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.storage.TempBlob;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ConanManifestTest
    extends TestSupport
{

  @Mock
  TempBlob blob;

  @Test
  public void canParse() throws IOException {
    when(blob.get()).thenAnswer( stream -> getClass().getResourceAsStream("conanmanifest.txt"));

    AttributesMap attributesMap = ConanManifest.parse(blob);

    assertTrue(attributesMap.get(".c_src/CMakeLists.txt").equals("9fd53df6571335b5080891a9b40e66b2"));
    assertTrue(attributesMap.get("conanfile.py").equals("9ea8083ad1c71182fa64ca0378bade18"));
  }
}