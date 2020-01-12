package org.sonatype.repository.conan.internal.hosted.search;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;

@Exposed
public interface ConanHostedSearchFacet
    extends Facet
{
  /**
   * Returns result for matching recipe names based on rest queries ?q=
   */
  Response searchRecipes(final Context context);

  /**
   * Returns all binaries(info) info for a 'full package recipe reference query':
   * example of a client query: Poco/1.7.8p3@pocoproject/stable
   */
  Response searchPackages(final Context context);
}
