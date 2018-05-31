package org.sonatype.repository.conan.internal.proxy;

import org.sonatype.repository.conan.internal.ConanCliContainer;
import org.sonatype.repository.conan.internal.NetworkFinder;

import org.sonatype.nexus.it.support.NexusContainer;
import org.sonatype.nexus.itscriptclient.repository.Repository;
import org.sonatype.nexus.itscriptclient.script.ScriptRunner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConanProxyIT
{
  private static final String CONAN_PROXY = "conan-proxy";

  private static final String INSTALL_FILE = "conanfile.txt";

  @Rule
  public ConanCliContainer container = new ConanCliContainer(new NetworkFinder());

  @Rule
  public NexusContainer nxrm = new NexusContainer();

  @Before
  public void setUp() throws Exception {
    ScriptRunner scriptRunner = new ScriptRunner(nxrm);
    Repository repository = new Repository(scriptRunner);

    repository.createProxy(CONAN_PROXY, CONAN_PROXY, "https://conan.bintray.com/", "default", true);

    container.addRemote(CONAN_PROXY, nxrm.getPort());
  }

  @Test
  public void proxyIsSetup() throws Exception {
    String response = container.execute("remote", "list");

    assertThat(response, containsString(CONAN_PROXY));
  }

  @Test
  public void conanInstall() throws Exception {
    String response = container.install(getClass().getResource(INSTALL_FILE).getPath());

    assertThat(response, containsString("PROJECT: Generated conaninfo.txt"));
  }
}
