package org.sonatype.repository.conan.internal.ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

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
  public List<String> computeComponentPath(final Asset asset, final Component component) {
    List<String> componentList = new ArrayList<>();
    componentList.add(component.group());
    componentList.add(component.name());
    componentList.add(component.version());
    return componentList;
  }

  public List<String> assetSegment(final String path) {
    String[] split = path.split("/");
    if(path.contains("packages")) {
      return ImmutableList.of(split[split.length-4], split[split.length-2], split[split.length-1]);
    }
    return ImmutableList.of(split[split.length-2], split[split.length-1]);
  }

  @Override
  public List<String> computeAssetPath(final Asset asset, final Component component) {
    checkNotNull(asset);

    if(component != null) {
      List<String> strings = computeComponentPath(asset, component);
      strings.addAll(assetSegment(asset.name()));
      return strings;
    } else {
      return super.computeAssetPath(asset, component);
    }
  }

}
