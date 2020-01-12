package org.sonatype.repository.conan.internal.hosted.search;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.repository.conan.internal.hosted.ConanHostedFacet;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import org.apache.commons.lang.StringUtils;
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
   *
   * @param paramQ This string is equal to the search string from conan:
   *               name/version@group/channel
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
   * Receives parameters for package path: package/version@user/channel
   * as ConanCoords object
   *
   * @param coords Conancoords built from search string passed by user.
   *
   * @return QueryBuilder for ElasticSearch based on parameters
   */
  QueryBuilder getQueryBuilder(ConanCoords coords, String repoName) {
    // TODO Implement channel QueryBuilder

    QueryStringQueryBuilder nameQuery = QueryBuilders.queryStringQuery(coords.getProject())
        .field("name.raw");

    // This search by version name will not be fully correct as it is treated as string
    // This unintended behaviour may occur when searching something like: [<1.1.1]
    QueryStringQueryBuilder versionQuery = QueryBuilders.queryStringQuery(coords.getVersion())
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
   * { ..."hits":{...."hits":[{..."_source":{...,
   *    "assets":[{"content_type":"application/gzip",
   *    name":"/v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz",
   *    ....
   * ...
   *
   * @param searchResponse The complete json result of search from ElasticSearch.
   *                       This was probably obtained after making a search request
   *                       based on ConanCoords.
   * @return A json(String) of all the recipe info that conan client can recognize
   * Conan wants recipe info in form: {results: ["recipe1", "recipe2"....]}
   */
  public JsonArray  getRecipesJSON(SearchResponse searchResponse) {
    // TODO Get only the recipe info from Elastic Search, not the whole array of package file lists

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
  }

  /**
   * The "full package search" made by conan expects the server to return the content of
   * conaninfo file. This method iterates over the information of packages obtained from
   * 'Elastic Search' searchResponse and filters the location of the conanfiles.
   *
   * @return Return all unique conanfile urls based on searchResponse.
   * */
  public ArrayList<String> getConanInfoUrls(SearchResponse searchResponse) {
    JsonArray allHits = this.getAllHits(searchResponse);
    ArrayList<String> conanFileUrls = new ArrayList<>();

    for (JsonElement elem : allHits) {
      JsonArray packageFiles = elem.getAsJsonObject()
          .getAsJsonObject("_source")
          .getAsJsonArray("assets");

      for(JsonElement files : packageFiles) {
        String assetKind = files.getAsJsonObject()
            .getAsJsonObject("attributes")
            .getAsJsonObject("conan")
            .get("asset_kind")
            .getAsString();

        if(assetKind.equals("CONAN_INFO")) {
          conanFileUrls.add(files.getAsJsonObject().get("name").getAsString());
        }
      }
    }

    return conanFileUrls;
  }

  /**
   * Content of conanInfoUrls is loaded and transformed into json object for
   * each conaninfo file.
   *
   * @param context This Context object is used for accessing the storage
   * @param conanInfoUrls All "ConanInfo Urls" for the recipes of a package.
   * @return The final result of the search that the user expects for a fully qualifiying
   * package search.
   */
  public String getPackageSearchReply(Context context, ArrayList<String> conanInfoUrls) {
    String line;
    InputStream in;
    InputStreamReader inReader;
    BufferedReader reader;
    Content content;

    JsonObject parsedIni = new JsonObject(); // main Json result
    JsonObject singlePackage; // temporary ini result for a single package recipe
    JsonObject section;
    JsonArray arrayElement = new JsonArray();
    String sectionName = "";
    String keyValue[];

    for(String conanInfoUrl : conanInfoUrls) {
      section = new JsonObject();

      try {
        content = context.getRepository()
            .facet(ConanHostedFacet.class)
            .doGetPublic(conanInfoUrl);

        in = content.openInputStream();
        inReader = new InputStreamReader(in);
        reader = new BufferedReader(inReader);

        singlePackage = new JsonObject();

        // TODO filter only the information that conan client wants fromo conaninfo file

        while( (line = reader.readLine()) != null) {
          if(line.startsWith("[") && line.endsWith("]")) {
            // start of a 'Section' of an ini file

            if(sectionName.equals("requires") || sectionName.equals("full_requires") || sectionName.equals("recipe_hash")) {
              //

              singlePackage.add(sectionName, arrayElement);
              arrayElement = new JsonArray();
            } else if(!sectionName.equals("")) {
              singlePackage.add(sectionName, section);
            }

            sectionName = StringUtils.substringBetween(line, "[", "]");
            section = new JsonObject();
          } else {
            if(line.indexOf('=') != -1) {
              keyValue = line.split("=");
              section.addProperty(keyValue[0].trim(), keyValue[1].trim());
            } else if(sectionName.equals("requires") || sectionName.equals("full_requires") || sectionName.equals("recipe_hash")){
              arrayElement.add(new JsonPrimitive(line.trim()));
            }
          }

        }

        parsedIni.add(StringUtils.substringBetween(conanInfoUrl, "packages/","/conaninfo.txt"), singlePackage);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return parsedIni.toString();
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

  public String recipeNameFromCoords(ConanCoords coords) {
    return coords.getProject() + "/"+
        coords.getVersion() + "@" +
        coords.getGroup() + "/" +
        coords.getChannel();
  }

}
