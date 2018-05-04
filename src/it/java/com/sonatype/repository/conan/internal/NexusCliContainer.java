package com.sonatype.repository.conan.internal;

import java.io.IOException;

public interface NexusCliContainer
{
  String execute(String... commands) throws IOException, InterruptedException;

  String install(final String resourceFilename) throws IOException, InterruptedException;
}
