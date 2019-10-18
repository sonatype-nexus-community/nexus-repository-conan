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

import javax.annotation.Nullable;
import javax.validation.constraints.Null.List;

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.DIGEST;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PATH;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION;

/**
 * Each project consists of these element. They are grouped here for easier access throughout the code base
 *
 * @since 0.0.2
 */
public class ConanCoords
{
  final private String path;

  final private String group;

  final private String project;

  final private String version;

  final private String channel;

  final private String sha;

  public ConanCoords(@Nullable final String path,
                     final String group,
                     final String project,
                     final String version,
                     final String channel,
                     @Nullable final String sha) {
    this.path = path;
    this.group = checkNotNull(group);
    this.project = checkNotNull(project);
    this.version = checkNotNull(version);
    this.channel = checkNotNull(channel);
    this.sha = sha;
  }

  public String getPath() { return path; }

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

  public String getSha() {
    return sha;
  }

  public static ConanCoords convertFromState(TokenMatcher.State state) {
    return new ConanCoords(
        state.getTokens().getOrDefault(PATH, null),
        state.getTokens().get(GROUP),
        state.getTokens().get(PROJECT),
        state.getTokens().get(VERSION),
        state.getTokens().get(STATE),
        state.getTokens().getOrDefault(DIGEST, null)
    );
  }

  public static String getPath(ConanCoords coord) {
    return String.format("%s%s/%s/%s/%s%s",
        coord.getPath() == null ? "" : coord.getPath() + "/",
        coord.getGroup(),
        coord.getProject(),
        coord.getVersion(),
        coord.getChannel(),
        coord.getSha() == null ? "" : "/packages/" + coord.getSha());
  }
}
