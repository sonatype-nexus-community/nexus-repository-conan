package org.sonatype.repository.conan.internal.proxy.matcher;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ConanMatcherTest
{
  static class MatcherExample {
    @JsonDeserialize(using = ConanMatcherDeserializer.class)
    @JsonTypeInfo(use = Id.NONE)
    public ConanMatcher conanMatcher;
  }

  @Test
  public void whenValueIsRemoteShouldCreateLocal() throws Exception {
    String test = "{" +
        "\"conanMatcher\": " + "\"" + RemoteMatcher.NAME + "\"" +
        "}";

    ObjectMapper mapper = new ObjectMapper();
    MatcherExample conanMatcher = mapper.readValue(test, MatcherExample.class);

    assertTrue(conanMatcher.conanMatcher instanceof RemoteMatcher);
  }

  @Test
  public void whenValueIsLocalShouldCreateLocal() throws Exception {
    String test = "{" +
        "\"conanMatcher\": " + "\"" + LocalMatcher.NAME + "\"" +
        "}";

    ObjectMapper mapper = new ObjectMapper();
    MatcherExample conanMatcher = mapper.readValue(test, MatcherExample.class);

    assertTrue(conanMatcher.conanMatcher instanceof LocalMatcher);
  }
}