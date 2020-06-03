package org.sonatype.nexus.plugins.conan;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.sonatype.nexus.pax.exam.NexusPaxExamSupport.nexusFeature;

public class ConanITConfig
{
  public static Option[] configureConanBase() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-conan"),
        systemProperty("nexus-exclude-features").value("nexus-cma-community")
    );
  }
}
