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
package org.sonatype.repository.conan.internal.hosted.v1;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Router;
import org.sonatype.repository.conan.internal.common.v1.ConanControllerV1;
import org.sonatype.repository.conan.internal.common.v1.ConanRoutes;

import static org.sonatype.repository.conan.internal.AssetKind.CONAN_EXPORT;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_FILE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_INFO;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_MANIFEST;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE_SNAPSHOT;
import static org.sonatype.repository.conan.internal.AssetKind.CONAN_SOURCES;
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

@Named
@Singleton
public class ConanHostedControllerV1
    extends ConanControllerV1
{
  @Inject
  private HostedHandlers hostedHandler;

  public void attach(final Router.Builder builder) {
    createRoute(builder, ConanRoutes.uploadUrls(), DOWNLOAD_URL, hostedHandler.uploadUrl);
    createRoute(builder, ConanRoutes.uploadDownloadUrls(), DOWNLOAD_URL, hostedHandler.uploadUrl);
    createRoute(builder, ConanRoutes.uploadManifest(), CONAN_MANIFEST, hostedHandler.uploadContentHandler);
    createRoute(builder, ConanRoutes.uploadConanfile(), CONAN_FILE, hostedHandler.uploadContentHandler);
    createRoute(builder, ConanRoutes.uploadConanInfo(), CONAN_INFO, hostedHandler.uploadContentHandler);
    createRoute(builder, ConanRoutes.uploadConanPackageZip(), CONAN_PACKAGE, hostedHandler.uploadContentHandler);
    createRoute(builder, ConanRoutes.uploadConanSource(), CONAN_SOURCES, hostedHandler.uploadContentHandler);
    createRoute(builder, ConanRoutes.uploadConanExportZip(), CONAN_EXPORT, hostedHandler.uploadContentHandler);

    // GET/FETCH ROUTING
    createRoute(builder, ConanRoutes.downloadUrlsHosted(), DOWNLOAD_URL, hostedHandler.downloadUrl);
    createRoute(builder, ConanRoutes.conanManifestHosted(), CONAN_MANIFEST, hostedHandler.download);
    createRoute(builder, ConanRoutes.conanFileHosted(), CONAN_FILE, hostedHandler.download);
    createRoute(builder, ConanRoutes.conanInfoHosted(), CONAN_INFO, hostedHandler.download);
    createRoute(builder, ConanRoutes.conanPackageHosted(), CONAN_PACKAGE, hostedHandler.download);
    createRoute(builder, ConanRoutes.conanSourceHosted(), CONAN_SOURCES, hostedHandler.download);
    createRoute(builder, ConanRoutes.conanExportHosted(), CONAN_EXPORT, hostedHandler.download);
    createRoute(builder, ConanRoutes.packageSnapshotHosted(), CONAN_PACKAGE_SNAPSHOT, hostedHandler.packageSnapshot);
    // GET/FETCH ROUTING
  }
}
