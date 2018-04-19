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
package org.sonatype.repository.conan.internal.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyMap;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;

/**
 * download_url files contain absolute paths to each asset
 *
 * This class removes the absolute address so as to redirect back to this repository
 *
 * @since 0.0.2
 */
@Singleton
@Named
public class ConanUrlIndexer
    extends ComponentSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public AttributesMap collectAbsoluteUrlsRefs(final TempBlob tempBlob,
                                               final String assetName) {
    AttributesMap attributesMap = new AttributesMap();
    Map<String, URL> downloadUrlContents = readIndex(tempBlob.get());

    for (Map.Entry<String, URL> entry : downloadUrlContents.entrySet()) {
      String originalHost = entry.getValue().toString().split(entry.getValue().getPath())[0];
      attributesMap.set(entry.getKey(), originalHost);
    }
    return attributesMap;
  }

  public String updateAbsoluteUrls(final Content content,
                                   final Repository repository) throws IOException
  {
    Map<String, URL> downloadUrlContents = readIndex(content.openInputStream());
    Map<String, String> remappedContents = new HashMap<>();

    for (Map.Entry<String, URL> entry : downloadUrlContents.entrySet()) {
      remappedContents.put(entry.getKey(), repository.getUrl() + entry.getValue().getPath());
    }

    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(remappedContents);
  }


  @Nullable
  private TempBlob updateDownloadUrlContents(final Repository repository, final Map<String, URL> downloadUrlContents) {
    try {
      return convertFileToTempBlob(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(downloadUrlContents), repository);
    }
    catch (JsonProcessingException e) {
      log.warn("Unable to write to blob", e);
      return null;
    }
  }

  @Nullable
  private URL getIndexedUrl(final String repositoryUrl, final String path) {
    try {
      return new URL(repositoryUrl + path);
    }
    catch (MalformedURLException e) {
      log.error("Unable to create indexed url", e);
    }
    return null;
  }

  private TempBlob convertFileToTempBlob(final String resolvedMap, final Repository repository) {
    StorageFacet storageFacet = repository.facet(StorageFacet.class);
    return storageFacet.createTempBlob(new StringPayload(resolvedMap, defaultCharset(), null), HASH_ALGORITHMS);
  }


  private Map<String, URL> readIndex(final InputStream stream) {
    ObjectMapper objectMapper = new ObjectMapper();

    TypeReference<HashMap<String, URL>> typeRef = new TypeReference<HashMap<String, URL>>() {};
    try {
      return objectMapper.readValue(stream, typeRef);
    }
    catch (IOException e) {
      log.warn("Unable to read index for asset",  e);
    }
    return emptyMap();
  }

  public String findUrl(final InputStream inputStream, final String find) {
    Map<String, URL> downloadUrlContents = readIndex(inputStream);
    if(downloadUrlContents.containsKey(find)) {
      return downloadUrlContents.get(find).toString();
    }
    return null;
  }
}

