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

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.testsuite.testsupport.FormatClientSupport;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConanClient
    extends FormatClientSupport
{
  public ConanClient(
      final CloseableHttpClient httpClient,
      final HttpClientContext httpClientContext,
      final URI repositoryBaseUri)
  {
    super(httpClient, httpClientContext, repositoryBaseUri);
  }

  public HttpResponse getHttpResponse(final String path) throws IOException {
    try (CloseableHttpResponse closeableHttpResponse = super.get(path)) {
      HttpEntity entity = closeableHttpResponse.getEntity();

      BasicHttpEntity basicHttpEntity = new BasicHttpEntity();
      String content = EntityUtils.toString(entity);
      basicHttpEntity.setContent(IOUtils.toInputStream(content));

      basicHttpEntity.setContentEncoding(entity.getContentEncoding());
      basicHttpEntity.setContentLength(entity.getContentLength());
      basicHttpEntity.setContentType(entity.getContentType());
      basicHttpEntity.setChunked(entity.isChunked());

      StatusLine statusLine = closeableHttpResponse.getStatusLine();
      HttpResponse response = new BasicHttpResponse(statusLine);
      response.setEntity(basicHttpEntity);
      response.setHeaders(closeableHttpResponse.getAllHeaders());
      response.setLocale(closeableHttpResponse.getLocale());
      return response;
    }
  }

  public HttpResponse put(final String path, final File file) throws Exception {
    checkNotNull(path);
    checkNotNull(file);

    final HttpPut put = new HttpPut(repositoryBaseUri.resolve(path));
    put.setEntity(new FileEntity(file, ContentType.create("application/octet-stream")));

    return execute(put);
  }
}
