package org.sonatype.repository.conan.internal.proxy.matcher;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Based on the configuration will create a {@link RemoteMatcher} or {@link LocalMatcher}
 *
 * @since 0.0.2
 */
public class ConanMatcherDeserializer
    extends JsonDeserializer<ConanMatcher>
{
  @Override
  public ConanMatcher deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext)
      throws IOException, JsonProcessingException
  {
    if(RemoteMatcher.NAME.equals(jsonParser.getValueAsString())) {
      return new RemoteMatcher();
    }
    return new LocalMatcher();
  }
}