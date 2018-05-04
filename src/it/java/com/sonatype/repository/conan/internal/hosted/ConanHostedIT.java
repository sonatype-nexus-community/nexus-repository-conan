package com.sonatype.repository.conan.internal.hosted;

import java.io.IOException;

import com.sonatype.repository.conan.internal.ConanCliContainer;

import org.sonatype.nexus.it.support.NexusContainer;
import org.sonatype.nexus.itscriptclient.realms.Realm;
import org.sonatype.nexus.itscriptclient.repository.Repository;
import org.sonatype.nexus.itscriptclient.script.ScriptRunner;
import org.sonatype.repository.conan.internal.security.token.ConanToken;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConanHostedIT
{
  private static final String CONAN_HOSTED = "conan-hosted";

  private static final String PROJECT_NAME = "Hello/0.1@demo/testing";

  private static final String INSTALL_SUCCESSFUL = "PROJECT: Generated conaninfo.txt";

  private static final String INSTALL_FILE = "conanfile.txt";

  private Realm realm;

  @Rule
  public ConanCliContainer conanClient = new ConanCliContainer();

  @Rule
  public NexusContainer nxrm = new NexusContainer();

  @Before
  public void setUp() throws Exception {
    ScriptRunner scriptRunner = new ScriptRunner(nxrm);
    Repository repository = new Repository(scriptRunner);
    realm = new Realm(scriptRunner);

    repository.createHosted(CONAN_HOSTED, CONAN_HOSTED, "default", true);

    conanClient.addRemote(CONAN_HOSTED, nxrm.getPort());
    conanClient.login(CONAN_HOSTED, "admin", "admin123");
  }

  @Test
  public void pushProjectWithRealmDisabledFails() throws Exception {
    createAndUploadProject();

    String response = conanClient.install(getClass().getResource(INSTALL_FILE).getPath());
    assertThat(response, not(containsString(INSTALL_SUCCESSFUL)));
  }

  @Test
  public void pushProjectWithRealmEnabledIsSuccess() throws Exception {
    realm.enable(ConanToken.NAME, true);

    createAndUploadProject();

    String response = conanClient.install(getClass().getResource(INSTALL_FILE).getPath());
    assertThat(response, containsString(INSTALL_SUCCESSFUL));
  }

  private void createAndUploadProject() throws IOException, InterruptedException {
    conanClient.execute("new", PROJECT_NAME);
    conanClient.execute("create", ".", "demo/testing");
    conanClient.execute("upload", PROJECT_NAME, "--all", "-r", CONAN_HOSTED);
    conanClient.execute("remove", "-f", PROJECT_NAME);
  }
}
