package org.sonatype.repository.conan.internal.orient.group;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.group.GroupFacet;
import org.sonatype.nexus.repository.group.GroupFacetImpl;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.types.GroupType;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Response.Builder;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.validation.ConstraintViolationFactory;
import org.sonatype.repository.conan.internal.orient.metadata.ConanUrlIndexer;

import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_JSON;

@Exposed
@Named
public class ConanGroupFacet
    extends GroupFacetImpl
{
  private final ConanUrlIndexer conanUrlIndexer;

  @Inject
  public ConanGroupFacet(final RepositoryManager repositoryManager,
                         final ConstraintViolationFactory constraintViolationFactory,
                         @Named(GroupType.NAME) final Type groupType,
                         final ConanUrlIndexer conanUrlIndexer) {
    super(repositoryManager, constraintViolationFactory, groupType);
    this.conanUrlIndexer = conanUrlIndexer;
  }

  public Response rewriteContent(final Context context, final Response response) throws IOException {
    final Builder builder = new Builder();
    builder.copy(response);
    builder.payload(new StringPayload(
        conanUrlIndexer.updateAbsoluteUrls(context, new Content(response.getPayload()), getRepository()),
        APPLICATION_JSON));
    return builder.build();
  }
}
