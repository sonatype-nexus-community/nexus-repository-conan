package org.sonatype.repository.conan.internal.ui;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.storage.BrowseNode;
import org.sonatype.nexus.repository.storage.BrowseNodeFilter;
import org.sonatype.repository.conan.internal.ConanFormat;

import static org.sonatype.repository.conan.internal.AssetKind.CONAN_PACKAGE_SNAPSHOT;
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

@Singleton
@Named(ConanFormat.NAME)
public class ConanBrowseNodeFilter
    implements BrowseNodeFilter
{
  @Override
  public boolean test(final BrowseNode node, final String repositoryName) {
    return !node.getName().endsWith(DOWNLOAD_URL.getFilename());
  }
}
