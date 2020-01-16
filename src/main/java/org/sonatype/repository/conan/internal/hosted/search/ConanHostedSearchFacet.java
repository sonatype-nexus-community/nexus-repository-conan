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
   *
   * An example of a client query that results in this response is:
   * "conan search OpenSSL/1.1.1@*"
   */
  Response searchRecipes(final Context context);

  /**
   * Returns the matching binaries' details(content of conaninfo file of a binary)
   * for a 'full package recipe reference query'.
   *
   * An example of a client query that results in this response is:
   * "conan search Poco/1.7.8p3@pocoproject/stable"
   */
  Response searchBinaries(final Context context);
}
