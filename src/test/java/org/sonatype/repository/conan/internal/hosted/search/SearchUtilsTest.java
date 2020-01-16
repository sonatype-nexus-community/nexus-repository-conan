package org.sonatype.repository.conan.internal.hosted.search;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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

    // For search by name(1)
    testString = "Poco";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("Poco"));
    assertThat(actualResult.getVersion(), is("*"));
    assertThat(actualResult.getGroup(), is("*"));
    assertThat(actualResult.getChannel(), is("*"));

    // For search by name(2)
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

    // search by group only
    testString = "*@conan/*";
    actualResult = searchUtilsTest.coordsFromParams(testString);
    assertThat(actualResult.getProject(), is("*"));
    assertThat(actualResult.getVersion(), is("*"));
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

  @Test
  public void getQueryBuilder() {
  }

  @Test
  public void getRecipesJSON() {
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