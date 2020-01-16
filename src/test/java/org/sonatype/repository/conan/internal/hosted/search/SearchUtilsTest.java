package org.sonatype.repository.conan.internal.hosted.search;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SearchUtilsTest
    extends TestSupport
{
  private SearchUtils searchUtilsTest;

  @Before
  public void setUp() throws Exception {
    searchUtilsTest = new SearchUtils();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void assetCoordsFromParamsParsesStringCorrectly() {
    String testString;
    ConanCoords actualResult;

    // For Empty search
    testString = "*";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("*"));
    assertThat(actualResult.getVersion(), is("*"));
    assertThat(actualResult.getGroup(), is("*"));
    assertThat(actualResult.getChannel(), is("*"));

    // For search by name
    testString = "OpenSSL/";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("OpenSSL"));
    assertThat(actualResult.getVersion(), is("*"));
    assertThat(actualResult.getGroup(), is("*"));
    assertThat(actualResult.getChannel(), is("*"));

    // For search by name and version
    testString = "Poco/1.1.1";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("Poco"));
    assertThat(actualResult.getVersion(), is("1.1.1"));
    assertThat(actualResult.getGroup(), is("*"));
    assertThat(actualResult.getChannel(), is("*"));

    // For search by wildcard after version
    testString = "Package/1.0.0@*";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("Package"));
    assertThat(actualResult.getVersion(), is("1.0.0"));
    assertThat(actualResult.getGroup(), is("*"));
    assertThat(actualResult.getChannel(), is("*"));

    // For search by wildcard channel
    testString = "Package/1.0.0@conan/*";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("Package"));
    assertThat(actualResult.getVersion(), is("1.0.0"));
    assertThat(actualResult.getGroup(), is("conan"));
    assertThat(actualResult.getChannel(), is("*"));

    // Full search
    testString = "project/version@group/channel";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("project"));
    assertThat(actualResult.getVersion(), is("version"));
    assertThat(actualResult.getGroup(), is("group"));
    assertThat(actualResult.getChannel(), is("channel"));
  }

  /**
   * This test validates getRecipesJSON(indirectly) and getRecipesJSONHelper
   */
  @Test
  public void searchResponseJsonToRecipesParsesJson() {
    // A mock a Elastic Search Response' hit array.
    String hitArrayString = "[{\"_index\":\"a3c2cfd22bbb7e92dc12671c05dc5298cc29a1c6\",\"_type\":\"component\"," +
        "\"_id\":\"36e3dec8de528c9b812ce6e257caaf97\",\"_score\":1.8728045,\"_source\":{\"isPrerelease\":false," +
        "\"assets\":[{\"content_type\":\"application/gzip\",\"name\":\"/v1/conans/conan/zlib/1.2" +
        ".11/stable/conan_export.tgz\",\"attributes\":{},\"id\":\"4b378653591c6722b70e482861f32f9a\"}," +
        "{\"content_type\":\"text/plain\",\"name\":\"/v1/conans/conan/zlib/1.2" +
        ".11/stable/packages/51b39bbf59d48fe09b7ac83ddab65ed9b30af19c/download_urls\",\"attributes\":{}," +
        "\"id\":\"4b378653591c6722a9feb24a294c454a\"}],\"format\":\"conan\",\"name\":\"zlib\",\"attributes\":{}}}," +
        "{\"_index\":\"a3c2cfd22bbb7e92dc12671c05dc5298cc29a1c6\",\"_type\":\"component\"," +
        "\"_id\":\"36e3dec8de528c9b343d5b0c2316afa8\",\"_score\":1.8728045,\"_source\":{\"isPrerelease\":false," +
        "\"assets\":[{\"content_type\":\"application/gzip\",\"name\":\"/v1/conans/conan/zlib/1.2" +
        ".8/stable/conan_export.tgz\",\"attributes\":{},\"id\":\"4b378653591c672242027086aea13c24\"}," +
        "{\"content_type\":\"text/plain\",\"name\":\"/v1/conans/conan/zlib/1.2.8/stable/download_urls\"," +
        "\"attributes\":{},\"id\":\"4b378653591c672280e5798dfcd65ebb\"}],\"format\":\"conan\",\"name\":\"zlib\"," +
        "\"attributes\":{}}}]";

    JsonArray hitsArray = (JsonArray) new JsonParser().parse(hitArrayString);
    ConanCoords mockCoords = new ConanCoords("*", "*", "*", "stable", "*");

    JsonArray recipesResultArr = searchUtilsTest.getRecipesJSONHelper(hitsArray, mockCoords);
    String actualArrayStr = recipesResultArr.toString();
    String expectedArrayStr = "[\"zlib/1.2.11@conan/stable\",\"zlib/1.2.8@conan/stable\"]";

    assertThat(expectedArrayStr, is(actualArrayStr));
  }

  @Test
  public void getConanInfoUrls() {
  }

  @Test
  public void getBinariesInfo() {
  }

  @Test
  public void nexusAssetToRecipe() {
  }

  @Test
  public void recipeNameFromCoords() {
  }
}