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
package org.sonatype.repository.conan.internal.proxy.v1;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.proxy.ProxyHandler;
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
public class ConanProxyControllerV1
    extends ConanControllerV1
{
  @Inject
  ProxyHandler proxyHandler;

  public void attach(final Router.Builder builder) {
    createRoute(builder, ConanRoutes.downloadUrlsProxy(), DOWNLOAD_URL, proxyHandler);
    createRoute(builder, ConanRoutes.conanManifestProxy(), CONAN_MANIFEST, proxyHandler);
    createRoute(builder, ConanRoutes.conanFileProxy(), CONAN_FILE, proxyHandler);
    createRoute(builder, ConanRoutes.conanInfoProxy(), CONAN_INFO, proxyHandler);
    createRoute(builder, ConanRoutes.conanPackageProxy(), CONAN_PACKAGE, proxyHandler);
    createRoute(builder, ConanRoutes.conanSourceProxy(), CONAN_SOURCES, proxyHandler);
    createRoute(builder, ConanRoutes.conanExportProxy(), CONAN_EXPORT, proxyHandler);
    createRoute(builder, ConanRoutes.packageSnapshotProxy(), CONAN_PACKAGE_SNAPSHOT, proxyHandler);
  }
}