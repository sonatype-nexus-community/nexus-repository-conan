package org.sonatype.repository.conan.internal.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.view.Payload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ConanUrlIndexerTest
    extends TestSupport
{
  private static final String DOWNLOAD_URL = "jsonformoderncpp_download_url.json";

  private static final String EXPECTED_DOWNLOAD_URL = "jsonformoderncpp_download_url_converted.json";

  @Mock
  TempBlob download_url;

  @Mock
  TempBlob processedBlob;

  @Mock
  Repository repository;

  @Mock
  StorageFacet storageFacet;

  ConanUrlIndexer underTest;

  @Before
  public void setUp() {
    setupRepositoryMock();

    class ConanUrlIndexerForTest
        extends ConanUrlIndexer
    {

    }
    underTest = new ConanUrlIndexerForTest();
  }

  @Test
  @Ignore
  public void replacesUrl() {
    when(download_url.get()).thenReturn(getClass().getResourceAsStream(DOWNLOAD_URL));
    when(repository.getUrl()).thenReturn("http://localhost/repository/conan-proxy");

    //TempBlob tempBlob = underTest.collectAbsoluteUrlsRefs(download_url, "assetName");

    //assertAbsoluteUrlMatches(tempBlob.get(), getClass().getResourceAsStream(EXPECTED_DOWNLOAD_URL));
  }

  private void setupRepositoryMock() {
    when(repository.facet(StorageFacet.class)).thenReturn(storageFacet);
    when(storageFacet.createTempBlob(any(Payload.class), any(Iterable.class))).thenAnswer(args -> {
      Payload payload = (Payload) args.getArguments()[0];
      byte[] bytes = IOUtils.toByteArray(payload.openInputStream());
      when(processedBlob.get()).thenReturn(new ByteArrayInputStream(bytes));
      return processedBlob;
    });
  }

  private void assertAbsoluteUrlMatches(final InputStream json, final InputStream expected) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    TypeReference<HashMap<String, URL>> typeRef = new TypeReference<HashMap<String, URL>>() {};
    Map<String, URL> actualResponse = objectMapper.readValue(json, typeRef);
    Map<String, URL> expectedResponse = objectMapper.readValue(expected, typeRef);

    actualResponse.forEach((key, value) -> expectedResponse.get(key).equals(value));
  }
}