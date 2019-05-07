package org.sonatype.repository.conan.internal.hosted.search;

import java.io.IOException;

import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;

@Named
public class ConanHostedSearchFacetImpl
    extends FacetSupport
    implements ConanHostedSearchFacet
{
    @Override
    public Content searchRecipes(Context context) throws IOException {
        return null;
    }

    @Override
    public Content searchPackages(Context context) throws IOException {
        return null;
    }
}

