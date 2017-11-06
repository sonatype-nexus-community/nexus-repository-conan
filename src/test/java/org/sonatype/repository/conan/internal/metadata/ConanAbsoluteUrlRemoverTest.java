package org.sonatype.repository.conan.internal.metadata;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.TempBlob;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.sonatype.goodies.testsupport.hamcrest.DiffMatchers.equalTo;

public class ConanAbsoluteUrlRemoverTest
    extends TestSupport
{
  private static final String DOWNLOAD_URL = "jsonformoderncpp_download_url.json";

  private static final String EXPECTED_DOWNLOAD_URL = "jsonformoderncpp_download_url_converted.json";

  private static final String OUTPUT_FILE = "output.json";

  @Mock
  TempBlob download_url;

  @Mock
  TempBlob processedBlob;

  @Mock
  Repository repository;

  @Mock
  StorageFacet storageFacet;

  ConanAbsoluteUrlRemover underTest;

  @Before
  public void setUp() throws Exception {
    setupRepositoryMock();

    underTest = new ConanAbsoluteUrlRemover();
  }

  @Test
  public void replacesUrl() throws Exception {
    when(download_url.get()).thenReturn(getClass().getResourceAsStream(DOWNLOAD_URL));
    when(repository.getUrl()).thenReturn("http://localhost/repository/conan-proxy");

    TempBlob tempBlob = underTest.removeAbsoluteUrls(download_url, repository);

    assertAbsoluteUrlMatches(tempBlob.get(), EXPECTED_DOWNLOAD_URL);
  }

  private void setupRepositoryMock() {
    when(repository.facet(StorageFacet.class)).thenReturn(storageFacet);
    when(storageFacet.createTempBlob(any(InputStream.class), any(Iterable.class))).thenAnswer(args -> {
      InputStream inputStream = (InputStream) args.getArguments()[0];
      byte[] bytes = IOUtils.toByteArray(inputStream);
      when(processedBlob.get()).thenReturn(new ByteArrayInputStream(bytes));
      return processedBlob;
    });
  }

  private void assertAbsoluteUrlMatches(final InputStream json, final String expected) throws IOException {
    String expectedJson = IOUtils.toString(getClass().getResourceAsStream(expected));
    String resultJson = IOUtils.toString(json);
    assertThat(resultJson, is(equalTo(expectedJson)));
  }
}