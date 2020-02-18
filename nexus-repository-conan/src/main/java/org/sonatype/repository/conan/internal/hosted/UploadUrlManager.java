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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.fasterxml.jackson.core.JsonProcessingException;

import static java.util.stream.Collectors.toMap;

/**
 * @since 0.0.2
 */
public class UploadUrlManager
    extends ComponentSupport
{
  private static final List<AssetKind> DOWNLOAD_URL_PACKAGE_ASSET_KINDS = Collections
      .unmodifiableList(Arrays.asList(
          AssetKind.CONAN_PACKAGE,
          AssetKind.CONAN_INFO,
          AssetKind.CONAN_MANIFEST)
      );

  private static final List<AssetKind> DOWNLOAD_URL_ASSET_KINDS = Collections
      .unmodifiableList(Arrays.asList(
          AssetKind.CONAN_EXPORT,
          AssetKind.CONAN_FILE,
          AssetKind.CONAN_SOURCES,
          AssetKind.CONAN_MANIFEST)
      );

  public static Map<String, String> generateAssetPackagesDownloadUrls(final ConanCoords coords) {
    return DOWNLOAD_URL_PACKAGE_ASSET_KINDS
        .stream()
        .collect(
            toMap(AssetKind::getFilename, x -> ConanHostedHelper.getHostedAssetPath(coords, x)));
  }

  private static Map<String, String> generateDownloadUrls(
      final List<AssetKind> assetKinds,
      final ConanCoords coords, final String repositoryUrl)
  {
    return assetKinds
        .stream()
        .collect(toMap(AssetKind::getFilename,
            x -> repositoryUrl + "/" + ConanHostedHelper.getHostedAssetPath(coords, x)));
  }

  public static String generatePackagesDownloadUrlsAsJson(
      final ConanCoords coords,
      final String repositoryUrl) throws JsonProcessingException
  {
    Map<String, String> downloadUrls = generateDownloadUrls(DOWNLOAD_URL_PACKAGE_ASSET_KINDS,
        coords, repositoryUrl);
    return ConanHostedHelper.MAPPER.writeValueAsString(downloadUrls);
  }

  public static String generateDownloadUrlsAsJson(
      final ConanCoords coords,
      final String repositoryUrl)
      throws JsonProcessingException
  {
    Map<String, String> downloadUrls = generateDownloadUrls(DOWNLOAD_URL_ASSET_KINDS, coords,
        repositoryUrl);
    return ConanHostedHelper.MAPPER.writeValueAsString(downloadUrls);
  }
}
