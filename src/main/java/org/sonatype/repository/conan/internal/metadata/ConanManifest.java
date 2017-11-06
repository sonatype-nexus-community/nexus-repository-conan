package org.sonatype.repository.conan.internal.metadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.storage.TempBlob;

/**
 * A conan manifest file contains md5 values for subsequent files
 */
public class ConanManifest
{
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
      e.printStackTrace();
    }
    return attributesMap;
  }
}
