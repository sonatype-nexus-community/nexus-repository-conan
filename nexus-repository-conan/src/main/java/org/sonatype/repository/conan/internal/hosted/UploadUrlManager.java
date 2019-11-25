/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2017-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.conan.internal.hosted;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.sonatype.goodies.common.ComponentSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link InputStream} is assumed to be 'Map<String, Integer>' where the key
 * value is the asset to be added. This class generates url links to where the
 * asset locations will be once stored.
 * Note: the data is stored wihout the host utl prefixed.
 * @since 0.0.2
 */
public class UploadUrlManager
    extends ComponentSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * The {@link InputStream} should be a 'Map<String, String>'
   * The key is the upload file and the value is the file size.
   * We need to return the values as the save location using the path
   *
   * Example: "Map<Key, Value>" -> "Map<Key, ConvertedKey>"
   *
   * @param path to prefix to the key entries (mapped to value)
   * @param inputStream
   * @return json object for use as downnload_url endpoint without full hostname
   */
  public String convertKeys(final String path, final InputStream inputStream) {
    Map<String, String> jsonParse = readJson(inputStream);
    Map<String, String> jsonResponse = convertKeys(path, jsonParse);
    return writejson(jsonResponse);
  }

  private Map<String, String> convertKeys(final String path, final Map<String, String> jsonParse) {
    return jsonParse.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> path + e.getKey()));
  }

  /**
   * Uses the input stream to convert the key, value pairs prefixing
   * the path to each value entry.
   *
   * Example: "Map<Key, Value>" -> "Map<Key, ConvertedValue>"
   *
   * @param path to prefix to the key entries (mapped to value)
   * @param inputStream
   * @return json object for use as downnload_url endpoint with full hostname
   */
  public String prefixToValues(final String path, final InputStream inputStream) {
    Map<String, String> jsonParse = readJson(inputStream);
    Map<String, String> jsonResponse = prefixToValues(path, jsonParse);
    return writejson(jsonResponse);
  }

  public Map<String, String> valuesMap(final InputStream inputStream) {
    Map<String, String> jsonParse = readJson(inputStream);
    return jsonParse;
  }

  private Map<String, String> prefixToValues(final String path, final Map<String, String> jsonParse) {
    return jsonParse.entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey, e -> path + e.getValue()));
  }

  private String writejson(final Map<String, String> jsonResponse) {
    try {
      return MAPPER.writeValueAsString(jsonResponse);
    }
    catch (JsonProcessingException e) {
      log.error("Unable to write json object", e);
    }
    return null;
  }

  private Map<String, String> readJson(final InputStream inputStream) {
    try {
      return MAPPER.readValue(inputStream, new TypeReference<Map<String, String>>(){});
    }
    catch (IOException e) {
      log.error("Unable to convertKeys json", e);
    }
    return Collections.emptyMap();
  }
}
