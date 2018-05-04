package com.sonatype.repository.conan.internal;

import java.io.IOException;
import java.net.InetAddress;

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

  public static final String BUILD_FOLDER = "/root/test/build";

  public static final String WORK_DIR = "/root/test";

  public ConanCliContainer() {
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
    String hostAddress = InetAddress.getLocalHost().getHostAddress();
    return execute("remote", "add", repositoryName, "http://" + hostAddress + ":" + port + "/repository/" + repositoryName, "false");
  }

  public String login(final String repositoryName, final String username, final String password)
      throws IOException, InterruptedException
  {
    return execute("user", "-r", repositoryName, "-p", password, username);
  }
}
