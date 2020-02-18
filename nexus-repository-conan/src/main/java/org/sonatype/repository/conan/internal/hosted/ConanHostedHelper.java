package org.sonatype.repository.conan.internal.hosted;

import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.metadata.ConanCoords;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.DIGEST;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.GROUP;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PATH;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.PROJECT;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.STATE;
import static org.sonatype.repository.conan.internal.metadata.ConanMetadata.VERSION;

/**
 * @since 1.0.0
 */
public class ConanHostedHelper
{
  public static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String CONAN_HOSTED_PREFIX = "conans/";

  public static String getHostedAssetPath(final ConanCoords coord, final AssetKind assetKind) {
    String path = String.format("%s/%s/%s/%s%s",
        coord.getGroup(),
        coord.getProject(),
        coord.getVersion(),
        coord.getChannel(),
        coord.getSha() == null ? "" : "/packages/" + coord.getSha());
    return String.format("%s%s/%s", CONAN_HOSTED_PREFIX, path, assetKind.getFilename());
  }

  public static ConanCoords convertFromState(final TokenMatcher.State state) {
    return new ConanCoords(
        state.getTokens().get(PATH),
        state.getTokens().get(GROUP),
        state.getTokens().get(PROJECT),
        state.getTokens().get(VERSION),
        state.getTokens().get(STATE),
        state.getTokens().getOrDefault(DIGEST, null)
    );
  }
}
