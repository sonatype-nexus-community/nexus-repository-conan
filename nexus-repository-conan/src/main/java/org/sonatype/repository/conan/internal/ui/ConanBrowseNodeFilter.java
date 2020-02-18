package org.sonatype.repository.conan.internal.ui;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.BrowseNode;
import org.sonatype.nexus.repository.storage.BrowseNodeFilter;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.repository.conan.internal.ConanFormat;

import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

/**
 *
 */
@Singleton
@Named(ConanFormat.NAME)
public class ConanBrowseNodeFilter
    implements BrowseNodeFilter
{
  @Inject
  private RepositoryManager repositoryManager;

  @Override
  public boolean test(final BrowseNode node, final String repositoryName) {
    Repository repository = repositoryManager.get(repositoryName);
    @SuppressWarnings("ConstantConditions")
    Type type = repository.getType();
    if (HostedType.NAME.equals(type.getValue())) {
      return !node.getName().endsWith(DOWNLOAD_URL.getFilename());
    }
    return true;
  }
}
