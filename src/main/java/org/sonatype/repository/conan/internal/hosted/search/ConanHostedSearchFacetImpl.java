package org.sonatype.repository.conan.internal.hosted.search;

import java.util.ArrayList;

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
    String params = paramsObj.get("q"); // get ?q=params..

    ConanCoords coords = searchUtils.coordsFromParams(params);
    SearchResponse searchResponse = getSearchResponseFromCoords(coords);
    String recipesResult = searchUtils.getRecipesJSON(searchResponse, coords);

    Content content =
        new Content(new StringPayload(recipesResult, ContentTypes.APPLICATION_JSON));

    return HttpResponses.ok(content);
  }

  @Override
  public Response searchPackages(Context context) {
    TokenMatcher.State state =
        context.getAttributes().require(TokenMatcher.State.class);
    ConanCoords coords = convertFromState(state);

    SearchResponse searchResponse = getSearchResponseFromCoords(coords);
    ArrayList<String> packageUrls = searchUtils.getConanInfoUrls(searchResponse);
    String packageResult = searchUtils.getBinariesInfo(context, packageUrls);

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

  /**
   * Make request to Elastic search based on the client input, which is represented
   * by ConanCoords and return the raw result.
   *
   * @param coords Represents the request that the client made.
   * @return An Elastic Search SearchResponse
   */
  private SearchResponse getSearchResponseFromCoords(ConanCoords coords) {
    QueryBuilder query = searchUtils.getQueryBuilder(
        coords,
        getRepository().getName());

    SearchResponse searchResponse = nexusSearchService.search(query,
        null,
        SearchUtils.ELASTIC_SEARCH_FROM,
        SearchUtils.ELASTIC_SEARCH_SIZE,
        SearchUtils.DEFAULT_TIMEOUT);

    return searchResponse;
  }
}
