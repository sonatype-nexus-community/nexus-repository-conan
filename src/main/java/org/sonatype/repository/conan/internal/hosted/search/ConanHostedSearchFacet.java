package org.sonatype.repository.conan.internal.hosted.search;

import java.io.IOException;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;

@Exposed
public interface ConanHostedSearchFacet
    extends Facet
{
    /**
     * Fetches recipe based on rest queries ?q=
     */
    Content searchRecipes(final Context context) throws IOException;

    /**
     * Returs package info for a fully matching search qualifier:
     * eg: Poco/1.7.8p3@pocoproject/stable
     */
    Content searchPackages(final Context context) throws IOException;
}