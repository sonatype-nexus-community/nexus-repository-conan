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
    createRoute(builder, ConanRoutes.uploadManifest(), CONAN_MANIFEST, hostedHandler.uploadManifest);
    createRoute(builder, ConanRoutes.uploadConanfile(), CONAN_FILE, hostedHandler.uploadConanFile);
    createRoute(builder, ConanRoutes.uploadConanInfo(), CONAN_INFO, hostedHandler.uploadConanInfo);
    createRoute(builder, ConanRoutes.uploadConanPackageZip(), CONAN_PACKAGE, hostedHandler.uploadConanPackage);
    createRoute(builder, ConanRoutes.uploadConanSource(), CONAN_SOURCES, hostedHandler.uploadConanSources);
    createRoute(builder, ConanRoutes.uploadConanExportZip(), CONAN_EXPORT, hostedHandler.uploadConanExport);

    createGetRoutes(builder, hostedHandler.downloadUrl, hostedHandler.download, hostedHandler.packageSnapshot);
  }
}
