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
package org.sonatype.repository.conan.internal.metadata;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Each project consists of these element. They are grouped here for easier access throughout the code base
 *
 * @since 0.0.2
 */
public class ConanCoords
{
  final private String group;

  final private String project;

  final private String version;

  final private String channel;

  public ConanCoords(final String group,
                     final String project,
                     final String version,
                     final String channel) {
    this.group = checkNotNull(group);
    this.project = checkNotNull(project);
    this.version = checkNotNull(version);
    this.channel = checkNotNull(channel);
  }

  public String getGroup() {
    return group;
  }

  public String getProject() {
    return project;
  }

  public String getVersion() {
    return version;
  }

  public String getChannel() {
    return channel;
  }
}
