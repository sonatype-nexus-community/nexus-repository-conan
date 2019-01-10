package org.sonatype.repository.conan.internal.security;

import java.io.IOException;

import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.mime.internal.DefaultMimeSupport;
import org.sonatype.nexus.repository.storage.DefaultContentValidator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConanContentValidatorTest
{
  private ConanContentValidator underTest;

  @Before
  public void setUp() throws Exception {
    DefaultContentValidator defaultContentValidator = new DefaultContentValidator(new DefaultMimeSupport());

    underTest = new ConanContentValidator(defaultContentValidator);
  }

  @Test
  public void shouldBePythonFileWhenUsingSheBang() throws IOException {
    String contentType = underTest.determineContentType(true,
        () -> getClass().getResourceAsStream("conanfile.py"),
        MimeRulesSource.NOOP,
        "something/conanfile.py",
        "text/x-python");

    assertThat(contentType, is("text/x-python"));
  }
}