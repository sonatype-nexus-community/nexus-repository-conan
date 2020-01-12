package org.sonatype.repository.conan.internal.hosted.search;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Status;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import com.google.gson.JsonArray;

import static org.sonatype.repository.conan.internal.metadata.ConanCoords.convertFromState;
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

  @Override
  public Response searchRecipes(Context context) {
    Request request = context.getRequest();
    Parameters paramsObj = request.getParameters();
    String params = paramsObj.get("q"); // ?q=params..

    ConanCoords coords = searchUtils.coordsFromParams(params);
    String recipesResult = getRecipeNamesFromCoords(coords);
    Content content =
        new Content(new StringPayload(recipesResult, ContentTypes.APPLICATION_JSON));

    return HttpResponses.ok(content);
  }

  @Override
  public Response searchPackages(Context context) {
    TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
    ConanCoords coords = convertFromState(state);

    SearchResponse searchResponse = getSearchResponseFromCoords(coords);
    ArrayList<String> packageUrls = searchUtils.getConanInfoUrls(searchResponse);
    String packageResult = searchUtils.getPackageSearchReply(context, packageUrls);

    Content payload;

    if(packageResult.equals("{}")) {
      String notFound = "Recipe not found: " + searchUtils.recipeNameFromCoords(coords);
      payload = new Content(new StringPayload(notFound, ContentTypes.TEXT_PLAIN));

      // create a 404 response because HttpResponse does not have a method that provides
      // payload for 404 requests
      return (new Response.Builder()).status(Status.success(404)).payload(payload).build();
    }

    payload =
        new Content(new StringPayload(packageResult, ContentTypes.APPLICATION_JSON));

    return HttpResponses.ok(payload);
  }

  private String getRecipeNamesFromCoords(ConanCoords coords) {
    SearchResponse searchResponse = getSearchResponseFromCoords(coords);

    JsonArray allRecipes = searchUtils.getRecipesJSON(searchResponse);
    allRecipes = searchUtils.filterByChannel(allRecipes, coords.getChannel());

    String recipesResult = searchUtils.getRecipesResult(allRecipes);

    return recipesResult;
  }

  private SearchResponse getSearchResponseFromCoords(ConanCoords coords) {
    QueryBuilder query = searchUtils.getQueryBuilder(
        coords,
        getRepository().getName());

    // SortBuilder to get recipes in ascending order
    List<SortBuilder> sortRecipes = new ArrayList<>();
    sortRecipes.add(SortBuilders.fieldSort("name.raw").order(SortOrder.ASC));

    // TODO add sort based on version number because it is treated as string

    SearchResponse searchResponse = nexusSearchService.search(query,
        sortRecipes,
        0,
        10,
        SearchUtils.DEFAULT_TIMEOUT);

    return searchResponse;
  }
}
