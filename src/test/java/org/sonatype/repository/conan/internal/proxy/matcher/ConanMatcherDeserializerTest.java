package org.sonatype.repository.conan.internal.proxy.matcher;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConanMatcherDeserializerTest
{
  private static ObjectMapper mapper = new ObjectMapper();

  private static String test = "{" +
      "\"conanMatcher\": " + "\"" + "%s" + "\"" +
      "}";

  static class MatcherExample {
    @JsonDeserialize(using = ConanMatcherDeserializer.class)
    @JsonTypeInfo(use = Id.NONE)
    public ConanMatcher conanMatcher;
  }

  @Test
  public void whenValueIsRemoteShouldCreateLocal() throws Exception {
    MatcherExample conanMatcher = mapper.readValue(String.format(test, BintrayMatcher.NAME), MatcherExample.class);

    assertTrue(conanMatcher.conanMatcher instanceof BintrayMatcher);
  }

  @Test
  public void whenValueIsLocalShouldCreateLocal() throws Exception {
    MatcherExample conanMatcher = mapper.readValue(String.format(test, StandardMatcher.NAME), MatcherExample.class);

    assertTrue(conanMatcher.conanMatcher instanceof StandardMatcher);
  }

  @Test(expected = InvalidFormatException.class)
  public void whenInvalidShouldThrowException() throws Exception {
    mapper.readValue(String.format(test, "unknown"), MatcherExample.class);
  }
}
