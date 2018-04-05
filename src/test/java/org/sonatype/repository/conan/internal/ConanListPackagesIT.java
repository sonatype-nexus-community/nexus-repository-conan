package org.sonatype.repository.conan.internal;

import org.junit.Rule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConanListPackagesIT
{
  @Rule
  public ConanCliContainer container = new ConanCliContainer();

  @Test
  public void proxyIsSetup() throws Exception {
    String response = container.execute("remote", "list");

    assertThat(response.contains("conan-proxy"));
  }

  @Test
  public void conanInstall() throws Exception {
    String response = container.install("conanfile.txt");

    assertThat(response.contains("PROJECT: Generated conaninfo.txt"));
  }
}
