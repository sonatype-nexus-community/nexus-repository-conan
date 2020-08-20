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
package org.sonatype.nexus.plugins.conan.internal;

import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.conan.internal.fixtures.RepositoryRuleConan;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Rule;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConanITSupport
    extends RepositoryITSupport
{
  @Rule
  public RepositoryRuleConan repos = new RepositoryRuleConan(() -> repositoryManager);

  @Override
  protected RepositoryRuleConan createRepositoryRule() {
    return new RepositoryRuleConan(() -> repositoryManager);
  }

  public ConanITSupport() {
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/conan"));

    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/test-classes/conan"));
  }

  @Nonnull
  protected ConanClient conanClient(final Repository repository) throws Exception {
    checkNotNull(repository);
    return conanClient(repositoryBaseUrl(repository));
  }

  protected ConanClient conanClient(final URL repositoryUrl) throws Exception {
    return new ConanClient(
        clientBuilder(repositoryUrl).build(),
        clientContext(),
        repositoryUrl.toURI()
    );
  }

  protected ConanClient conanClient(final Credentials credentials, final Repository repository)
      throws Exception
  {
    HttpClientBuilder builder = clientBuilder();
    if (credentials != null) {
      String hostname = nexusUrl.getHost();
      AuthScope scope = new AuthScope(hostname, -1);
      CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.
          setCredentials(scope, credentials);
      builder.setDefaultCredentialsProvider(credentialsProvider);
    }

    return new ConanClient(clientBuilder(repositoryBaseUrl(repository)).build(), clientContext(), repositoryBaseUrl(repository).toURI());
  }
}
