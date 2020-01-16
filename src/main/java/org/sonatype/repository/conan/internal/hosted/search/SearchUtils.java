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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

@Named
@Singleton
public class SearchUtils
{
  public static final Integer DEFAULT_TIMEOUT = 20; // TODO: Verify the time unit

  public static final Integer ELASTIC_SEARCH_FROM = 0;

  public static final Integer ELASTIC_SEARCH_SIZE = 100;

  /**
   * Returns a ConanCoords object that represents the recipe info
   * required for a search.
   *
   * @param paramQ A string whose complete format is:
   *               name/version@group/channel
   *               But, some parts maybe missing. These are replaced by '*'
   * @return Extract the parts of paramQ and return a ConanCoords object
   */
  public ConanCoords coordsFromParams(String paramQ) {
    String packageName = "*";
    String version = "*";
    String group = "*";
    String channel = "*";

    if (paramQ != null) {
      String[] atSeparated = paramQ.split("@");

      if (atSeparated.length == 2) {
        String[] afterAt = atSeparated[1].split("/");
        if (afterAt.length == 2) {
          channel = afterAt[1];
        }
        if (afterAt.length != 0) {
          group = afterAt[0];
        }
      }

      String[] beforeAt = atSeparated[0].split("/");
      if (beforeAt.length == 2) {
        version = beforeAt[1];
      }
      if (beforeAt.length != 0) {
        packageName = beforeAt[0];
      }
    }

    return new ConanCoords(group, packageName, version, channel, "*");
  }

  /**
   * Uses the package path: package/version@user/channel represented
   * as a ConanCoords object to create a QueryBuilder to make search requests
   * to Elastic Search.
   *
   * @param coords Conancoords based on the search query made by the client
   * @return QueryBuilder for ElasticSearch.
   */
  QueryBuilder getQueryBuilder(ConanCoords coords, String repoName) {
    // TODO Implement channel QueryBuilder

    QueryStringQueryBuilder nameQuery = QueryBuilders.queryStringQuery(coords.getProject())
        .field("name");

    // This search by version name will not be fully correct as it is treated as string
    // This unintended behaviour may occur when searching something like: [<1.1.1]
    QueryStringQueryBuilder versionQuery = QueryBuilders.queryStringQuery(coords.getVersion())
        .field("version");

    // for nexus elastic search user is group
    QueryStringQueryBuilder groupQuery = QueryBuilders.queryStringQuery(coords.getGroup())
        .field("group");

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
   * { ..."hits":{...."hits":[{..."_source":{...,
   * "assets":[{"content_type":"application/gzip",
   * name":"/v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz",
   * ....
   * ...
   *
   * @param searchResponse The complete json result of search from ElasticSearch.
   *                       This was probably obtained after making a search request
   *                       to ElasticSearch using ConanCoords.
   * @return A json(String) of all the recipe info that conan client can recognize
   * Conan wants recipe info in form: {results: ["recipe1", "recipe2"....]}
   */
  public String getRecipesJSON(SearchResponse searchResponse, ConanCoords coords) {
    // TODO Get only the recipe info from Elastic Search, not the whole array of package file lists

    JsonArray allHits = getAllHits(searchResponse);
    JsonArray results = getRecipesJSONHelper(allHits, coords);

    // group the parsed binaries info as: {results: ["binary-recipe1", "binary-recipe2", ...]}
    JsonObject finalResult = new JsonObject();
    finalResult.add("results", results);

    return finalResult.toString();
  }

  public JsonArray getRecipesJSONHelper(JsonArray allHits, ConanCoords coords) {
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

      // filter by channel
      if (this.filterByChannel(recipe, coords.getChannel())) {
        results.add(new JsonPrimitive(recipe));
      }
    }

    return results;
  }

  /**
   * The "full package search" made by conan expects the server to return the content of
   * conaninfo file. This method iterates over the information of packages obtained from
   * 'Elastic Search' searchResponse and filters the location of the unique conanfiles
   * of the binaries of the package that the user is searching.
   *
   * @return Return all unique conanfile urls based on searchResponse.
   */
  public ArrayList<String> getConanInfoUrls(SearchResponse searchResponse) {
    JsonArray allHits = this.getAllHits(searchResponse);
    ArrayList<String> conanFileUrls = new ArrayList<>();

    for (JsonElement binaryHit : allHits) {
      /*
      The elements of the hits array of SearchResponse contains the info of a
      package groupd by the name. So all file names(attributes) of a search result,
      for example: "Poco/1.7.8p3@pocoproject/stable" are grouped in one hit.
       */

      JsonArray packageFiles = binaryHit.getAsJsonObject()
          .getAsJsonObject("_source")
          .getAsJsonArray("assets");

      for (JsonElement files : packageFiles) {
        // iterate over all the result of a package, and extract Unique conaninfo files.

        String assetKind = files.getAsJsonObject()
            .getAsJsonObject("attributes")
            .getAsJsonObject("conan")
            .get("asset_kind")
            .getAsString();

        if (assetKind.equals("CONAN_INFO")) {
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
   * @param context       This Context object is used for accessing the storage
   * @param conanInfoUrls All "ConanInfo Urls" for the recipes of a package.
   * @return The final result of the search that the user expects for a
   * 'full package recipe reference query'
   */
  public String getBinariesInfo(Context context, ArrayList<String> conanInfoUrls) {
    InputStream in;
    InputStreamReader inReader;
    BufferedReader reader;
    Content content;

    JsonObject parsedIni = new JsonObject(); // main Json result
    String packageHashValue;

    JsonObject parsedConanInfo;

    for (String conanInfoUrl : conanInfoUrls) {
      // parse the conaninfo file for a binary
      try {
        content = context.getRepository()
            .facet(ConanHostedFacet.class)
            .doGetPublic(conanInfoUrl);

        // Try to open the conaninfo file
        in = content.openInputStream();
        inReader = new InputStreamReader(in);
        reader = new BufferedReader(inReader);

        parsedConanInfo = extractConanInfoFile(reader);

        packageHashValue = StringUtils.substringBetween(conanInfoUrl, "packages/", "/conaninfo.txt");
        parsedIni.add(packageHashValue, parsedConanInfo); // key is the package hash. The content parsed is the value;
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    return parsedIni.toString();
  }

  public JsonObject extractConanInfoFile(BufferedReader reader) throws Exception {
    // helper class for storing intermediate information
    ConanInfofile conanInfofile = new ConanInfofile();
    String line;

    while ((line = reader.readLine()) != null) {
      conanInfofile.parseLine(line);
    }

    return conanInfofile.getMainResult();
  }


  private boolean filterByChannel(String recipeName, String channelFilter) {
    String recipeChannel;
    recipeChannel = recipeName.substring(recipeName.lastIndexOf('/') + 1);

    return FilenameUtils.wildcardMatch(recipeChannel, channelFilter);
  }

  private JsonArray getAllHits(SearchResponse searchResponse) {
    JsonObject responseJson;
    responseJson = new JsonParser().parse(searchResponse.toString()).getAsJsonObject();
    responseJson = responseJson.getAsJsonObject("hits"); // first hits object

    // hits array containing all the recipes and their content info
    return responseJson.getAsJsonArray("hits");
  }

  /**
   * Converts an asset name obtained by querying to elastic search to
   * conan based recipe name:
   *
   * @param assetName String like - "/v1/conans/conan/zlib/1.2.11/stable/conan_export.tgz"
   * @return recipe name from assetName: zlib/1.2.11@conan/stable
   */
  public String nexusAssetToRecipe(String assetName) {
    String[] symbols = assetName.split("/");
    return symbols[4] + "/" +
        symbols[5] + "@" +
        symbols[3] + '/' +
        symbols[6];
  }

  /**
   * @param coords ConanCoord object for a package recipe
   * @return Recipe name of form: name/version@group/channel
   */
  public String recipeNameFromCoords(ConanCoords coords) {
    return coords.getProject() + "/" +
        coords.getVersion() + "@" +
        coords.getGroup() + "/" +
        coords.getChannel();
  }

  private class ConanInfofile {
    // current section name
    private String sectionName;

    // represents the ini file for a binary
    private JsonObject completeBinaryInfo;

    // data to store content of section that contains key-value pairs
    private JsonObject sectionObject;

    // data to store the content of a section that contains list data
    private JsonArray sectionArray;

    public ConanInfofile() {
      sectionName = "";
      completeBinaryInfo = new JsonObject();
      sectionObject = new JsonObject();
      sectionArray = new JsonArray();
    }

    public JsonObject getMainResult() {
      return completeBinaryInfo;
    }

    public boolean isArraySection() {
      return sectionName.equals("requires") ||
          sectionName.equals("full_requires") ||
          sectionName.equals("recipe_hash");
    }

    public void parseLine(String line) {
      if (line.startsWith("[") && line.endsWith("]")) {
        // start of a 'Section' of an ini file

        if (isArraySection()) {
          // These sections do not have key=vaue form. Their result is stored as an array
          completeBinaryInfo.add(sectionName, sectionArray);
        }
        else if (!sectionName.equals("")) {
          // This is not the first section. So add previous section's content to the singlePackage array
          completeBinaryInfo.add(sectionName, sectionObject);
        }

        sectionName = StringUtils.substringBetween(line, "[", "]");
        sectionArray = new JsonArray();
        sectionObject = new JsonObject();
      }
      else {
        if (line.indexOf('=') != -1) {
          // section with key:value content
          String keyValue[] = line.split("=");
          sectionObject.addProperty(keyValue[0].trim(), keyValue[1].trim());
        }
        else if (isArraySection()) {
          // section with array content
          sectionArray.add(new JsonPrimitive(line.trim()));
        }
      }
    }
  }
}
