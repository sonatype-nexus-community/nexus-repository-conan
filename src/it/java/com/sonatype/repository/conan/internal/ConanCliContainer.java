package com.sonatype.repository.conan.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sonatype.repository.conan.internal.NexusCliContainer;

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

  private static final String hostAddress;

  static {
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      throw new RuntimeException("Unable to find host name");
    }
  }

  public ConanCliContainer() {
    super(
        new ImageFromDockerfile()
            .withDockerfileFromBuilder(builder -> {
                builder
                    .from("python:2.7")
                    .run("pip install --no-cache-dir conan")
                    .run("conan remote remove conan-center")
                    .run("conan remote add conan-proxy http://" + hostAddress + ":8081/repository/conan-proxy false")
                    .cmd(FOREVER)
                    .build();
            })
    );
  }

  @Override
  public String execute(String... commands) throws IOException, InterruptedException {
    return execInContainer(concat(CONAN, commands)).getStdout();
  }

  @Override
  public String install(String resourceFilename) throws IOException, InterruptedException {
    copyFileToContainer(MountableFile.forClasspathResource(resourceFilename), "/");
    return execInContainer(CONAN, "install", ".").getStdout();
  }
}
