package org.sonatype.repository.conan.internal.search;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.rest.SearchMapping;
import org.sonatype.nexus.repository.rest.SearchMappings;
import org.sonatype.repository.conan.internal.ConanFormat;

import com.google.common.collect.ImmutableList;

@Named(ConanFormat.NAME)
@Singleton
public class ConanSearchMappings
    extends ComponentSupport
    implements SearchMappings
{
  private static final List<SearchMapping> MAPPINGS = ImmutableList.of(
      new SearchMapping("conan.baseVersion", "attributes.conan.baseVersion", "baseVersion"),
      new SearchMapping("conan.channel", "attributes.conan.channel", "channel")
  );

  @Override
  public Iterable<SearchMapping> get() {
    return MAPPINGS;
  }
}
