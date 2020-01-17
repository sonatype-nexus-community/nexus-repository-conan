package org.sonatype.repository.conan.internal.hosted.search;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SearchUtilsTest
    extends TestSupport
{
  private SearchUtils searchUtilsTest;

  @Before
  public void setUp() {
    searchUtilsTest = new SearchUtils();
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

    // For search by wildcard after version
    testString = "Package/1.0.0@*";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("Package"));
    assertThat(actualResult.getVersion(), is("1.0.0"));
    assertThat(actualResult.getGroup(), is("*"));
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
    // An excerpt of Elastic Search hits array for a test search of: conan search zlib
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

  /**
   * Test for getConanInfoUrlFromHit and getConanInfoUrls(indirectly)
   */
  @Test
  public void conaninfoUrlsAreExtractedFromHit() {
    /*
      An excerpt of an Elastic Search hit array for a 'full package recipe reference query'
      for the query: 'conan search Poco/1.1.1@conan/stable'
     */

    String hitExcerpt = "{\"_index\":\"a3c2cfd22bbb7e92dc12671c05dc5298cc29a1c6\",\"_type\":\"component\"," +
        "\"_id\":\"13b29e449f0e3b8d39964ed3e110e2e3\",\"_score\":2.8494635,\"_source\":{\"isPrerelease\":false," +
        "\"assets\":[{\"content_type\":\"text/x-python\",\"name\":\"/v1/conans/conan/OpenSSL/1.1.1/stable/conanfile" +
        ".py\",\"attributes\":{},\"id\":\"c7545579a7a15390a3714bcd0c5c8cee\"},{\"content_type\":\"text/plain\"," +
        "\"name\":\"/v1/conans/conan/OpenSSL/1.1.1/stable/packages/009a50ddeb47afbc9361cbc63650560c127e1234/conaninfo" +
        ".txt\",\"attributes\":{\"provenance\":{\"hashes_not_verified\":false}," +
        "\"conan\":{\"asset_kind\":\"CONAN_INFO\"},\"checksum\":{\"sha1\":\"718a062b8ef5fc79e276e4bc088c7157370d9af2" +
        "\",\"sha256\":\"6cc9f30f10c5929e67f0750c4ac5807062a2b2ce6f85e2b9e744c702bdb559f9\"," +
        "\"md5\":\"b5f8edc342f28d896492f229ccfc2f35\"},\"content\":{\"last_modified\":1574542510050},\"cache\":{}}," +
        "\"id\":\"08909bf0c86cf6c940d0d1ba3562f15d\"},{\"content_type\":\"application/gzip\"," +
        "\"name\":\"/v1/conans/conan/OpenSSL/1.1" +
        ".1/stable/packages/24cc36842a06a69aacb4e3bcb1043c2e136b52d1/conan_package.tgz\"," +
        "\"attributes\":{\"provenance\":{\"hashes_not_verified\":false}," +
        "\"conan\":{\"asset_kind\":\"CONAN_PACKAGE\"}},\"id\":\"f21ff3b2a4c24aaaf5c69c8c4d00ae4c\"}," +
        "{\"content_type\":\"text/plain\",\"name\":\"/v1/conans/conan/OpenSSL/1.1" +
        ".1/stable/packages/24cc36842a06a69aacb4e3bcb1043c2e136b52d1/conaninfo.txt\"," +
        "\"attributes\":{\"provenance\":{\"hashes_not_verified\":false},\"conan\":{\"asset_kind\":\"CONAN_INFO\"}," +
        "\"checksum\":{\"sha1\":\"f894f56f23d7ea9bf2cb50a6e606c58046e6cb0f\"," +
        "\"sha256\":\"90e15aecece59367173dc039bd83638f1d8055f5f87bb094ef5d835b935c0416\"," +
        "\"md5\":\"6af7213bb20503424ee3ebf5edfc1d47\"},\"content\":{\"last_modified\":1574542511002},\"cache\":{}}," +
        "\"id\":\"294c6332b49a6d45cd227bfc917711dd\"},{\"content_type\":\"application/gzip\"," +
        "\"name\":\"/v1/conans/conan/OpenSSL/1.1" +
        ".1/stable/packages/ef21a5ccd6e2072df69dffc879be49754f14c35d/conan_package.tgz\"," +
        "\"attributes\":{\"provenance\":{\"hashes_not_verified\":false},\"conan\":{\"asset_kind\":\"CONAN_PACKAGE\"}," +
        "\"checksum\":{\"sha1\":\"984c39eff656190674ceee5c2a253fd2fa0027c0\"," +
        "\"sha256\":\"89ea2246fd1f0d1e87e75f86a6f94ebd30ce4e256b7cbeb916655a7fa250dd89\"," +
        "\"md5\":\"4b4e5cb93cab98493fcd5e6dbe46e574\"},\"content\":{\"last_modified\":1574542545946},\"cache\":{}}," +
        "\"id\":\"f21ff3b2a4c24aaaa751408f49cb259b\"}],\"format\":\"conan\",\"name\":\"OpenSSL\",\"attributes\":{}," +
        "\"normalized_version\":\"000000001.000000001.000000001\",\"lastDownloaded\":\"2020-01-16T14:27:25" +
        ".667+0000\",\"version\":\"1.1.1\",\"lastBlobUpdated\":\"2019-11-23T20:55:46.359+0000\"," +
        "\"repository_name\":\"conan-hosted\",\"group\":\"conan\"}}";
    JsonElement packageHit = new JsonParser().parse(hitExcerpt);

    ArrayList<String> actualUrls = new ArrayList<>();
    ArrayList<String> expectedUrls = new ArrayList<>();
    expectedUrls
        .add("/v1/conans/conan/OpenSSL/1.1.1/stable/packages/009a50ddeb47afbc9361cbc63650560c127e1234/conaninfo.txt");
    expectedUrls
        .add("/v1/conans/conan/OpenSSL/1.1.1/stable/packages/24cc36842a06a69aacb4e3bcb1043c2e136b52d1/conaninfo.txt");

    searchUtilsTest.getConanInfoUrlFromHit(packageHit, actualUrls);

    assertThat(actualUrls.toString(), is(expectedUrls.toString()));
  }

  /**
   * Test for class ConanInfofile and indirectly for extractConanInfoFile,
   * getBinariesInfo
   */
  @Test
  public void ConanInfofileClassWorks() {
    SearchUtils.ConanInfofile conanInfofile = new SearchUtils.ConanInfofile();

    /*
      mock a conaninfo file using list
     */
    List<String> conaninfoContent = new ArrayList<String>();
    JsonObject expectedJsonObj = new JsonObject();
    JsonObject tempJsonObj = new JsonObject();
    JsonArray tempJsonArr = new JsonArray();

    conaninfoContent.add("[settings]");
    conaninfoContent.add("arch=x86_64");
    tempJsonObj.addProperty("arch", "x86_64");
    conaninfoContent.add("build_type=Debug");
    tempJsonObj.addProperty("build_type", "Debug");
    conaninfoContent.add("compiler=Visual Studio");
    tempJsonObj.addProperty("compiler", "Visual Studio");
    conaninfoContent.add("compiler.runtime=MDd");
    tempJsonObj.addProperty("compiler.runtime", "MDd");
    conaninfoContent.add("compiler.version=15");
    tempJsonObj.addProperty("compiler.version", "15");
    conaninfoContent.add("os=Windows");
    tempJsonObj.addProperty("os", "Windows");

    conaninfoContent.add("[requires]");
    conaninfoContent.add("lib/1.Y.Z");
    tempJsonArr.add(new JsonPrimitive("lib/1.Y.Z"));

    expectedJsonObj.add("settings", tempJsonObj);
    expectedJsonObj.add("requires", tempJsonArr);

    for (String line : conaninfoContent) {
      conanInfofile.parseLine(line);
    }
    String conaninfo = conanInfofile.getMainResult().toString();

    assertThat(conaninfo, is(expectedJsonObj.toString()));
  }
}