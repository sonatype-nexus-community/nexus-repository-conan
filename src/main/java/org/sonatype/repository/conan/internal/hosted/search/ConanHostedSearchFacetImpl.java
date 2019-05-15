package org.sonatype.repository.conan.internal.hosted.search;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.google.gson.JsonArray;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
public class ConanHostedSearchFacetImpl
    extends FacetSupport
    implements ConanHostedSearchFacet
{
  private final SearchService nexusSearchService;
  private final SearchUtils searchUtils;

  @Inject
  public ConanHostedSearchFacetImpl(final SearchService nexusSearchService,
                                    final SearchUtils searchUtils) {
    this.nexusSearchService = checkNotNull(nexusSearchService);
    this.searchUtils = checkNotNull(searchUtils);
  }

  /*
  Implements Minimum functionality of searching a recipe based on Name, Group
  and Channel.
   */
  @Override
  public Content searchRecipes(Context context) {
    Request request = context.getRequest();
    Parameters paramsObj = request.getParameters();
    String params = paramsObj.get("q"); // ?q=params..

    // form ConanCoords from the request, since it is the only
    // requirement to search for recipes.
    ConanCoords coords = searchUtils.coordsFromParams(params);

    return searchRecipesFromCoords(coords);
  }

  @Override
  public Content searchPackages(Context context) {
    return null;
  }

  private Content searchRecipesFromCoords(ConanCoords coords) {
    QueryBuilder query = searchUtils.getQueryBuilder(
        coords,
        getRepository().getName());

    // SortBuilder to get recipes in ascending order
    List<SortBuilder> sortRecipes = new ArrayList<>();
    sortRecipes.add(SortBuilders.fieldSort("name.raw").order(SortOrder.ASC));

    SearchResponse searchResponse = nexusSearchService.search(query,
        sortRecipes,
        0,
        10);

    JsonArray allRecipes = searchUtils.getRecipesJSON(searchResponse);
    allRecipes = searchUtils.filterByChannel(allRecipes, coords.getChannel());

    String recipesResult = searchUtils.getRecipesResult(allRecipes);

    return new Content(new StringPayload(recipesResult, ContentTypes.APPLICATION_JSON));
  }
}
