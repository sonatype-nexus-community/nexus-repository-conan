package org.sonatype.repository.conan.internal.orient.group;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.group.GroupHandler;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.repository.conan.internal.AssetKind;
import org.sonatype.repository.conan.internal.orient.hosted.v1.ConanHostedFacet;
import org.sonatype.repository.conan.internal.orient.metadata.ConanUrlIndexer;

import static org.sonatype.repository.conan.internal.AssetKind.DIGEST;
import static org.sonatype.repository.conan.internal.AssetKind.DOWNLOAD_URL;

@Named
@Singleton
public class ConanGroupHandler
    extends GroupHandler
{
  @Override
  public Response handle(final Context context) throws Exception {
    final Response response = super.handle(context);

    if (context.getRequest().getPath().endsWith("/download_urls") ||
        context.getRequest().getPath().endsWith("/digest")) {
      return context.getRepository()
          .facet(ConanGroupFacet.class)
          .rewriteContent(context, response);
    }
    return response;
  }
}
