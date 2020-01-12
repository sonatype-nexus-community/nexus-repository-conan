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
   * Fetches recipe based on rest queries ?q=
   */
  Response searchRecipes(final Context context);

  /**
   * Returs package info for a fully matching search qualifier:
   * eg: Poco/1.7.8p3@pocoproject/stable
   */
  Response searchPackages(final Context context);
}
