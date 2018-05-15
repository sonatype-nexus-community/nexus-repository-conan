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
package org.sonatype.repository.conan.internal.security.token;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;

/**
 * @since 0.0.2
 */
@Exposed
public interface ConanTokenFacet
    extends Facet
{
  /**
   * Performs a login for user authenticated in the request (creates token and returns login specific response).
   */
  Response login(Context context);

  /**
   * Determines if the user is currently authenticated
   */
  Response user(Context context);

  /**
   * Performs a log-out for currently authenticated user (deletes the token if found and returns logout specific
   * response).
   */
  Response logout(Context context);
}
