package com.sonatype.repository.conan.internal.proxy;

import com.sonatype.repository.conan.internal.ConanCliContainer;

import org.sonatype.nexus.it.support.NexusContainer;

import org.junit.Rule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConanProxyIT
{
  @Rule
  public ConanCliContainer container = new ConanCliContainer();

  @Rule
  public NexusContainer nxrm = new NexusContainer();

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
