package com.sonatype.repository.conan.internal.utils;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.Query;
import org.sonatype.nexus.repository.storage.StorageTx;

import static java.util.Collections.singletonList;
import static org.sonatype.nexus.repository.storage.ComponentEntityAdapter.P_VERSION;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;

public class ConanFacetUtils
{

  /**
   * Find a component by its name and tag (version)
   *
   * @return found component or null if not found
   */
  @Nullable
  public static Component findComponent(final StorageTx tx,
                                        final Repository repository,
                                        final String name,
                                        final String version)
  {
    Iterable<Component> components = tx.findComponents(
        Query.builder()
            .where(P_NAME).eq(name)
            .and(P_VERSION).eq(version)
            .build(),
        singletonList(repository)
    );
    if (components.iterator().hasNext()) {
      return components.iterator().next();
    }
    return null;
  }
}
