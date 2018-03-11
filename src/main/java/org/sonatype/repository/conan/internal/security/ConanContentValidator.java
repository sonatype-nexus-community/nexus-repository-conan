package org.sonatype.repository.conan.internal.security;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.repository.storage.ContentValidator;
import org.sonatype.nexus.repository.storage.DefaultContentValidator;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.repository.conan.internal.ConanFormat;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.2
 */
@Named(ConanFormat.NAME)
@Singleton
public class ConanContentValidator
    implements ContentValidator
{
  private final DefaultContentValidator defaultContentValidator;

  @Inject
  public ConanContentValidator(final DefaultContentValidator defaultContentValidator) {
    this.defaultContentValidator = checkNotNull(defaultContentValidator);
  }

  @Nonnull
  @Override
  public String determineContentType(final boolean strictContentTypeValidation,
                                     final Supplier<InputStream> contentSupplier,
                                     @Nullable final MimeRulesSource mimeRulesSource,
                                     @Nullable final String contentName,
                                     @Nullable final String declaredContentType) throws IOException
  {
    if (contentName != null && contentName.endsWith(".tgz")) {
      return ContentTypes.APPLICATION_GZIP;
    }
    return defaultContentValidator.determineContentType(
        strictContentTypeValidation, contentSupplier, mimeRulesSource, contentName, declaredContentType
    );
  }
}
