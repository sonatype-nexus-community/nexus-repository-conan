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
package com.sonatype.repository.conan.internal.metadata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.annotation.Nullable;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.TempBlob;

import org.apache.commons.io.IOUtils;

import static com.sonatype.repository.conan.internal.proxy.ConanProxyHelper.HASH_ALGORITHMS;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newInputStream;
import static java.util.UUID.randomUUID;

/**
 * download_url files contain absolute paths to each asset
 *
 * This class removes the absolute address so as to redirect back to this repository
 *
 * @since 3.conan
 */
public class ConanAbsoluteUrlRemover
    extends ComponentSupport
{
  private static final String CONAN_BASE_URL = "https://api.bintray.com/conan/conan/conan-center/v1/files";

  @Nullable
  public TempBlob removeAbsoluteUrls(final TempBlob tempBlob,
                                     final Repository repository) {
    try {
      Path tempFile = createTempFile("conan-download_url-" + randomUUID().toString(), "json");

      try {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile.toFile()))) {
          String input = IOUtils.toString(tempBlob.get());
          input = input.replaceAll(CONAN_BASE_URL, repository.getUrl());
          bufferedWriter.write(input);
        }
        return convertFileToTempBlob(tempFile, repository);
      } finally {
        delete(tempFile);
      }
    } catch (IOException e) {
      log.warn("Unable to create temp file");
    }
    return null;
  }

  private TempBlob convertFileToTempBlob(final Path tempFile, final Repository repository) throws IOException {
    StorageFacet storageFacet = repository.facet(StorageFacet.class);
    try (InputStream tempFileInputStream = newInputStream(tempFile)) {
      return storageFacet.createTempBlob(tempFileInputStream, HASH_ALGORITHMS);
    }
  }
}
