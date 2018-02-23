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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.util.UUID.randomUUID;
import static org.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;

/**
 * download_url files contain absolute paths to each asset
 *
 * This class removes the absolute address so as to redirect back to this repository
 *
 * @since 0.0.1
 */
public class ConanAbsoluteUrlRemover
    extends ComponentSupport
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Nullable
  public TempBlob removeAbsoluteUrls(final TempBlob tempBlob,
                                     final Repository repository,
                                     final Map<String, URL> indexMap) {

    TypeReference<HashMap<String, URL>> typeRef = new TypeReference<HashMap<String, URL>>() {};
    try {
      Map<String, URL> response = MAPPER.readValue(tempBlob.get(), typeRef);

      for (Map.Entry<String, URL> entry : response.entrySet()) {
        URL originalUrl = entry.getValue();
        URL indexUrl = new URL(repository.getUrl() + entry.getValue().getPath());
        indexMap.put(entry.getValue().getPath(), originalUrl);
        entry.setValue(indexUrl);
      }

      return convertFileToTempBlob(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(response), repository);
    }
    catch (IOException e) {
      log.warn("Unable to document", e);
      return null;
    }
  }

  private TempBlob convertFileToTempBlob(final String resolvedMap, final Repository repository) throws IOException {
    StorageFacet storageFacet = repository.facet(StorageFacet.class);
    return storageFacet.createTempBlob(new StringPayload(resolvedMap, defaultCharset(), null), HASH_ALGORITHMS);
  }
}
