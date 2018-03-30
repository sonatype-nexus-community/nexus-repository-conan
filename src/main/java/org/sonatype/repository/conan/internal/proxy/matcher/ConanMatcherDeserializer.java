package org.sonatype.repository.conan.internal.proxy.matcher;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Based on the configuration will create a {@link BintrayMatcher} or {@link StandardMatcher}
 *
 * @since 0.0.2
 */
public class ConanMatcherDeserializer
    extends JsonDeserializer<ConanMatcher>
{
  @Override
  public ConanMatcher deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
      throws IOException
  {
    if(BintrayMatcher.NAME.equals(jsonParser.getValueAsString())) {
      return new BintrayMatcher();
    }
    else if(StandardMatcher.NAME.equals(jsonParser.getValueAsString())) {
      return new StandardMatcher();
    }

    throw deserializationContext.weirdKeyException(ConanMatcher.class, jsonParser.getValueAsString(),
        "Unknown ConanMatcher type");
  }
}
