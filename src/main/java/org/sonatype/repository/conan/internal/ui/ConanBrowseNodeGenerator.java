package org.sonatype.repository.conan.internal.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.browse.BrowsePaths;
import org.sonatype.nexus.repository.browse.ComponentPathBrowseNodeGenerator;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.repository.conan.internal.ConanFormat;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Singleton
@Named(ConanFormat.NAME)
public class ConanBrowseNodeGenerator
    extends ComponentPathBrowseNodeGenerator
{
  public ConanBrowseNodeGenerator() {
    super();
  }

  @Override
  public List<BrowsePaths> computeComponentPaths(final Asset asset, final Component component) {
    return BrowsePaths.fromPaths(createComponentPathList(asset, component), false);
  }

  @Override
  public List<BrowsePaths> computeAssetPaths(final Asset asset, final Component component) {
    checkNotNull(asset);

    if(component != null) {
      return BrowsePaths.fromPaths(createAssetPathList(asset, component), false);
    } else {
      return super.computeAssetPaths(asset, component);
    }
  }

  private List<String> createComponentPathList(final Asset asset, final Component component) {
    List<String> path = new ArrayList<>();
    path.add(component.group());
    path.add(component.name());
    path.add(component.version());
    return path;
  }

  private List<String> createAssetPathList(final Asset asset, final Component component) {
    List<String> path = createComponentPathList(asset, component);

    String[] assetSegments = asset.name().split("/");
    if(asset.name().contains("packages")) {
      path.add(assetSegments[assetSegments.length-4]);
    }

    path.add(assetSegments[assetSegments.length-2]);
    path.add(assetSegments[assetSegments.length-1]);

    return path;
  }

}
