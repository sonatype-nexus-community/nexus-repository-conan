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

import com.google.common.collect.ImmutableList;

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
    List<String> componentList = new ArrayList<>();
    componentList.add(component.group());
    componentList.add(component.name());
    componentList.add(component.version());
    return componentList;
  }

  private List<String> assetSegment(final String path) {
    String[] split = path.split("/");
    if(path.contains("packages")) {
      return ImmutableList.of(split[split.length-4], split[split.length-2], split[split.length-1]);
    }
    return ImmutableList.of(split[split.length-2], split[split.length-1]);
  }

  private List<String> createAssetPathList(final Asset asset, final Component component) {
    List<String> strings = createComponentPathList(asset, component);
    strings.addAll(assetSegment(asset.name()));
    return strings;
  }

}
