package org.sonatype.repository.conan.internal.hosted.search;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

@Named
@Singleton
public class SearchUtils
{
  public static final Integer DEFAULT_TIMEOUT = 5;

  /**
   * Returns a ConanCoords object that represents the recipe info
   * required for a search.
   */
  public ConanCoords coordsFromParams(String paramQ) {
    String packageName = "*";
    String version = "*";
    String group = "*";
    String channel = "*";

    if(paramQ!=null) {
      String [] atSeparated = paramQ.split("@");

      if(atSeparated.length==2) {
        String [] afterAt = atSeparated[1].split("/");
        if(afterAt.length==2) channel = afterAt[1];
        if(afterAt.length!=0) group = afterAt[0];
      }

      String [] beforeAt = atSeparated[0].split("/");
      if(beforeAt.length==2) version = beforeAt[1];
      if(beforeAt.length!=0) packageName = beforeAt[0];
    }


    return new ConanCoords(group, packageName, version, channel, "*");
  }

  /**
   * Receives parameters for package path: package/version@user/channel(ConanCoords)
   * @return QueryBuilder for ElasticSearch based on parameters
   */
  QueryBuilder getQueryBuilder(ConanCoords coords, String repoName) {
    // TODO Implement channel QueryBuilder

    QueryStringQueryBuilder nameQuery = QueryBuilders.queryStringQuery(coords.getProject())
        .field("name.raw");

    // since semver searches do not work properly with conan search. Do not use this
    QueryStringQueryBuilder versionQuery = QueryBuilders.queryStringQuery("*")
        .field("version");

    // for nexus elastic search user is group
    QueryStringQueryBuilder groupQuery = QueryBuilders.queryStringQuery(coords.getGroup())
        .field("group.raw");

    QueryStringQueryBuilder repoQuery = QueryBuilders.queryStringQuery(repoName)
        .field("repository_name");

    QueryBuilder query = QueryBuilders.boolQuery()
        .must(nameQuery)
        .must(versionQuery)
        .must(groupQuery)
        .must(repoQuery);

    return query;
  }

  /**
   * This method receives a SearchResponse, which contains json
   * of the form:
   * $$$$$$$$$$$$$$$$$$
   * {
   *    ....
   *    "hits":{
   *       ....
   *       "hits":[
   *          {
   *             ....
   *             "_source":{
   *                "isPrerelease":false,
   *                "assets":[
   *                   {
   *                      "content_type":"application/gzip",
   *                      "name":"/v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz",
   *                      .....
   *                   },
   *                  .......
   * 				]
   *          },
   *          {
   *             ....
   *          }
   * ...
   * $$$$$$$$$$$$$
   * So we have a "hits" object that contains a hits array. This hits array groups recipes and
   * gives info about the content of everything inside the recipe. So what we basically do
   * is that extract the first content of a 'hit' (from "assets" object)
   * and then get the "name" attribute.
   * Then this name 'attribute' is used to generate the recipe string.
   *
   * @param searchResponse The complete json result of search to ElasticSearch
   * @return A json(String) of all the recipe info that conan client can recognize
   * Conan wants recipe info in form: {results: ["recipe1", "recipe2"....]}
   */
  public JsonArray  getRecipesJSON(SearchResponse searchResponse) {
    // TODO Get only the recipe info from Elastic Search, not the whole package content

    JsonArray allHits = this.getAllHits(searchResponse);
    JsonArray results = new JsonArray();

    // now using the name attribute of the first member from
    // "assets" attribute and get the recipe info
    String recipe;
    for (JsonElement elem : allHits) {
      JsonElement temp = elem.getAsJsonObject()
          .getAsJsonObject("_source")
          .getAsJsonArray("assets")
          .get(0)
          .getAsJsonObject()
          .get("name");

      recipe = this.nexusAssetToRecipe(temp.toString());
      results.add(new JsonPrimitive(recipe));
    }

    return results;
    //JsonObject finalResult = new JsonObject();
    //finalResult.add("results", results);

    //return finalResult.toString();
  }

  public JsonArray filterByChannel(JsonArray arr, String channelStr) {
    JsonArray filtered = new JsonArray();
    String recipe;
    int channelPos;
    for(JsonElement elem : arr) {
      recipe = elem.getAsString();
      channelPos = recipe.lastIndexOf('/') + 1;

      if(recipe.substring(channelPos).equals(channelStr) || channelStr=="*")
        filtered.add(elem);
    }
    return filtered;
  }

  public String getRecipesResult(JsonArray arr) {
    JsonObject finalResult = new JsonObject();
    finalResult.add("results", arr);
    return finalResult.toString();
  }

  private JsonArray getAllHits(SearchResponse searchResponse) {
    JsonObject responseJson;
    responseJson = new JsonParser().parse(searchResponse.toString()).getAsJsonObject();
    responseJson = responseJson.getAsJsonObject("hits"); // first hits object

    // hits array containing all the recipes and their contents
    return responseJson.getAsJsonArray("hits");
  }

  /**
   * Converts an asset name obtained by querying to elastic search to
   * conan equivalent recipe name:
   *
   * @param assetName String like - "/v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz"
   *
   * @return recipe name from assetName: zlib/1.2.11@conan/stable
   * */
  public String nexusAssetToRecipe(String assetName) {
    String [] symbols = assetName.split("/");
    return symbols[4] + "/" +
        symbols[5] + "@" +
        symbols[3] + '/' +
        symbols[6];
  }

}
