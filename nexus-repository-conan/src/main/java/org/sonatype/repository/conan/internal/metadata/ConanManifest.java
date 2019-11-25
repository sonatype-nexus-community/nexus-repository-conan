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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.storage.TempBlob;

import org.slf4j.Logger;

/**
 * A conan manifest file contains md5 values for subsequent files
 *
 * @since 0.0.1
 */
public class ConanManifest
{
  private static final Logger LOGGER = Loggers.getLogger(ConanManifest.class);

  /**
   * Extract all the md5 for files and add to attributes for later lookup
   * @param blob
   * @return
   */
  public static AttributesMap parse(TempBlob blob) {
    AttributesMap attributesMap = new AttributesMap();
    try(BufferedReader reader = new BufferedReader(new InputStreamReader(blob.get()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] split = line.split(":");
        if (split.length == 2) {
          attributesMap.set(split[0].trim(), split[1].trim());
        }
      }
    } catch (IOException e) {
      LOGGER.warn("Unable to convertKeys manifest file");
    }
    return attributesMap;
  }
}
