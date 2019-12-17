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
  private static final int PACKAGE_SNAPSHOT_PATH_LENGTH = 8;

  private static final String PACKAGES_SEGMENT = "packages";

  public ConanBrowseNodeGenerator() {
    super();
  }

  @Override
  public List<BrowsePaths> computeComponentPaths(final Asset asset, final Component component) {
    List<String> componentList = new ArrayList<>();
    componentList.add(component.group());
    componentList.add(component.name());
    componentList.add(component.version());
    return BrowsePaths.fromPaths(componentList, true);
  }

  @Override
  public List<BrowsePaths> computeAssetPaths(final Asset asset, final Component component) {
    checkNotNull(asset);

    if (component != null) {
      List<BrowsePaths> strings = computeComponentPaths(asset, component);
      strings.addAll(assetSegment(asset.name()));
      return strings;
    }
    else {
      return super.computeAssetPaths(asset, component);
    }
  }

  private List<BrowsePaths> assetSegment(final String path) {
    String[] split = path.split("/");
    int fileNameIndex = split.length - 1;
    int channelIndex = split.length - 2;
    int packageNameIndex;
    int packagesSegmentIndex;

    if (path.contains(PACKAGES_SEGMENT)) {
      if (split.length == PACKAGE_SNAPSHOT_PATH_LENGTH) {
        packageNameIndex = split.length - 1;
        packagesSegmentIndex = split.length - 2;
        channelIndex = split.length - 3;
        return BrowsePaths
            .fromPaths(ImmutableList.of(split[channelIndex], split[packagesSegmentIndex], split[packageNameIndex]),
                false);
      }
      else {
        packageNameIndex = split.length - 2;
        packagesSegmentIndex = split.length - 3;
        channelIndex = split.length - 4;
        return BrowsePaths
            .fromPaths(ImmutableList
                    .of(split[channelIndex], split[packagesSegmentIndex], split[packageNameIndex], split[fileNameIndex]),
                false);
      }
    }
    return BrowsePaths.fromPaths(ImmutableList.of(split[channelIndex], split[fileNameIndex]), false);
  }
}
