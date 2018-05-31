package org.sonatype.repository.conan.internal;

import java.io.IOException;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.MountableFile;

import static com.google.common.collect.ObjectArrays.concat;

public class ConanCliContainer
    extends GenericContainer
    implements NexusCliContainer
{
  private static final String CONAN = "conan";

  private static final String FOREVER = "while :; do sleep 1; done";

  private static final String BUILD_FOLDER = "/root/test/build";

  private static final String WORK_DIR = "/root/test";

  private final NetworkFinder networkFinder;

  public ConanCliContainer(final NetworkFinder networkFinder) {
    super(
        new ImageFromDockerfile()
            .withDockerfileFromBuilder(builder -> {
                builder
                    .from("python:latest")
                    .run("apt-get update -y")
                    .run("apt-get update")
                    .run("apt-get install -y cmake g++")
                    .run("pip install --no-cache-dir conan")
                    .run("conan remote remove conan-center")
                    .run("mkdir -p " + BUILD_FOLDER)
                    .cmd(FOREVER)
                    .workDir(WORK_DIR)
                    .build();
            })
    );
    this.networkFinder = networkFinder;
  }

  @Override
  public String execute(String... commands) throws IOException, InterruptedException {
    return execInContainer(concat(CONAN, commands)).getStdout();
  }

  @Override
  public String install(final String resourceFilename) throws IOException, InterruptedException {
    copyFileToContainer(MountableFile.forHostPath(resourceFilename), BUILD_FOLDER);
    return execInContainer(CONAN, "install", BUILD_FOLDER).getStdout();
  }

  public String addRemote(final String repositoryName, final int port) throws IOException, InterruptedException {
    return execute("remote", "add", repositoryName, "http://" + networkFinder.getAddressToUse() + ":" + port + "/repository/" + repositoryName, "false");
  }

  public String login(final String repositoryName, final String username, final String password)
      throws IOException, InterruptedException
  {
    return execute("user", "-r", repositoryName, "-p", password, username);
  }
}
