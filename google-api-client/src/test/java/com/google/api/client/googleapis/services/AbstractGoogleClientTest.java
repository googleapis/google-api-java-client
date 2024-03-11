/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.services;

import static org.junit.Assert.assertThrows;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.testing.services.MockGoogleClient;
import com.google.api.client.googleapis.testing.services.MockGoogleClientRequest;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Key;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests {@link AbstractGoogleClient}.
 *
 * @author Yaniv Inbar
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractGoogleClientTest extends TestCase {

  @Mock private GoogleCredentials googleCredentials;

  @Mock private HttpCredentialsAdapter httpCredentialsAdapter;

  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final JsonObjectParser JSON_OBJECT_PARSER = new JsonObjectParser(JSON_FACTORY);
  private static final HttpTransport TRANSPORT = new MockHttpTransport();

  private static class TestHttpRequestInitializer implements HttpRequestInitializer {

    @Override
    public void initialize(HttpRequest httpRequest) {
      // no-op
    }
  }

  private static class TestRemoteRequestInitializer implements GoogleClientRequestInitializer {

    boolean isCalled;

    TestRemoteRequestInitializer() {}

    public void initialize(AbstractGoogleClientRequest<?> request) {
      isCalled = true;
    }
  }

  @Test
  public void testGoogleClientBuilder() {
    String rootUrl = "https://test.googleapis.com/";
    String servicePath = "test/path/v1/";
    GoogleClientRequestInitializer jsonHttpRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    AbstractGoogleClient.Builder setApplicationName =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setGoogleClientRequestInitializer(jsonHttpRequestInitializer)
            .setSuppressAllChecks(true);
    AbstractGoogleClient client = setApplicationName.build();

    assertEquals(rootUrl + servicePath, client.getBaseUrl());
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals(servicePath, client.getServicePath());
    assertEquals(applicationName, client.getApplicationName());
    assertEquals(jsonHttpRequestInitializer, client.getGoogleClientRequestInitializer());
    assertTrue(client.getSuppressPatternChecks());
    assertTrue(client.getSuppressRequiredParameterChecks());
  }

  @Test
  public void testGoogleClientBuilder_setsCorrectRootUrl_nonMtlsUrl() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .build();
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals(Credentials.GOOGLE_DEFAULT_UNIVERSE, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_setsCorrectRootUrl_mtlsUrl() {
    String rootUrl = "https://test.mtls.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .build();
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals(Credentials.GOOGLE_DEFAULT_UNIVERSE, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_customUniverseDomain_nonMtlsUrl() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    String universeDomain = "random.com";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setUniverseDomain(universeDomain)
            .build();
    assertEquals("https://test.random.com/", client.getRootUrl());
    assertEquals(universeDomain, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_customUniverseDomain_mtlsUrl() {
    String rootUrl = "https://test.mtls.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    final AbstractGoogleClient.Builder builder =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setUniverseDomain("random.com");

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            new ThrowingRunnable() {
              @Override
              public void run() {
                builder.build();
              }
            });
    assertEquals(
        "mTLS is not supported in any universe other than googleapis.com", exception.getMessage());
  }

  @Test
  public void testGoogleClientBuilder_customEndpoint_defaultUniverseDomain() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setRootUrl("https://randomendpoint.com/")
            .build();
    assertEquals("https://randomendpoint.com/", client.getRootUrl());
    assertEquals(Credentials.GOOGLE_DEFAULT_UNIVERSE, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_customEndpoint_customUniverseDomain() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    String universeDomain = "random.com";
    String customRootUrl = "https://randomendpoint.com/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setRootUrl(customRootUrl)
            .setUniverseDomain(universeDomain)
            .build();
    assertEquals(customRootUrl, client.getRootUrl());
    assertEquals(universeDomain, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_noCustomUniverseDomain_universeDomainEnvVar() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    // Env Var Universe Domain is `random.com`
    String envVarUniverseDomain = "random.com";
    String expectedRootUrl = "https://test.random.com/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .build();
    assertEquals(expectedRootUrl, client.getRootUrl());
    assertEquals(envVarUniverseDomain, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientBuilder_customUniverseDomain_universeDomainEnvVar() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    // Env Var Universe Domain is `random.com`
    String customUniverseDomain = "test.com";
    String expectedRootUrl = "https://test.test.com/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .setUniverseDomain(customUniverseDomain)
            .build();
    assertEquals(expectedRootUrl, client.getRootUrl());
    assertEquals(customUniverseDomain, client.getUniverseDomain());
  }

  @Test
  public void testGoogleClientSuppressionDefaults() {
    String rootUrl = "http://www.testgoogleapis.com/test/";
    String servicePath = "path/v1/";

    // Assert suppression defaults.
    AbstractGoogleClient.Builder googleClientBuilder =
        new MockGoogleClient.Builder(TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, null);
    assertFalse(googleClientBuilder.getSuppressPatternChecks());
    assertFalse(googleClientBuilder.getSuppressRequiredParameterChecks());

    AbstractGoogleClient googleClient = googleClientBuilder.build();
    assertFalse(googleClient.getSuppressPatternChecks());
    assertFalse(googleClient.getSuppressRequiredParameterChecks());
  }

  @Test
  public void testBaseServerAndBasePathBuilder() {
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT,
                "http://www.testgoogleapis.com/test/",
                "path/v1/",
                JSON_OBJECT_PARSER,
                null)
            .setApplicationName("Test Application")
            .setRootUrl("http://www.googleapis.com/test/")
            .setServicePath("path/v2/")
            .build();

    assertEquals("http://www.googleapis.com/test/path/v2/", client.getBaseUrl());
  }

  @Test
  public void testInitialize() throws Exception {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT, "http://www.test.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application")
            .setGoogleClientRequestInitializer(remoteRequestInitializer)
            .build();
    client.initialize(null);
    assertTrue(remoteRequestInitializer.isCalled);
  }

  @Test
  public void testParseServiceName_nonMtlsRootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://random.googleapis.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertEquals(clientBuilder.getServiceName(), "random");
  }

  @Test
  public void testParseServiceName_mtlsRootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://test.mtls.googleapis.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertEquals(clientBuilder.getServiceName(), "test");
  }

  @Test
  public void testParseServiceName_nonGDURootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://test.random.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertNull(clientBuilder.getServiceName());
  }

  @Test
  public void testIsUserSetEndpoint_nonMtlsRootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://random.googleapis.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertFalse(clientBuilder.isUserConfiguredEndpoint);
  }

  @Test
  public void testIsUserSetEndpoint_mtlsRootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://test.mtls.googleapis.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertFalse(clientBuilder.isUserConfiguredEndpoint);
  }

  @Test
  public void testIsUserSetEndpoint_nonGDURootUrl() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT, "https://test.random.com/", "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application");
    assertTrue(clientBuilder.isUserConfiguredEndpoint);
  }

  @Test
  public void testIsUserSetEndpoint_regionalEndpoint() {
    AbstractGoogleClient.Builder clientBuilder =
        new MockGoogleClient.Builder(
                TRANSPORT,
                "https://us-east-4.coolservice.googleapis.com/",
                "",
                JSON_OBJECT_PARSER,
                null)
            .setApplicationName("Test Application");
    assertTrue(clientBuilder.isUserConfiguredEndpoint);
  }

  @Test
  public void validateUniverseDomain_validUniverseDomain() throws IOException {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    Mockito.when(httpCredentialsAdapter.getCredentials()).thenReturn(googleCredentials);
    Mockito.when(googleCredentials.getUniverseDomain())
        .thenReturn(Credentials.GOOGLE_DEFAULT_UNIVERSE);

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, httpCredentialsAdapter)
            .setApplicationName(applicationName)
            .build();

    // Nothing throws
    client.validateUniverseDomain();
  }

  @Test
  public void validateUniverseDomain_invalidUniverseDomain() throws IOException {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    Mockito.when(httpCredentialsAdapter.getCredentials()).thenReturn(googleCredentials);
    Mockito.when(googleCredentials.getUniverseDomain()).thenReturn("invalid.universe.domain");

    final AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT, rootUrl, servicePath, JSON_OBJECT_PARSER, httpCredentialsAdapter)
            .setApplicationName(applicationName)
            .build();
    assertThrows(
        IOException.class,
        new ThrowingRunnable() {
          @Override
          public void run() throws IOException {
            client.validateUniverseDomain();
          }
        });
  }

  @Test
  public void validateUniverseDomain_notUsingHttpCredentialsAdapter_defaultUniverseDomain()
      throws IOException {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";

    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT,
                rootUrl,
                servicePath,
                JSON_OBJECT_PARSER,
                new TestHttpRequestInitializer())
            .setApplicationName(applicationName)
            .build();

    // Nothing throws
    client.validateUniverseDomain();
  }

  @Test
  public void validateUniverseDomain_notUsingHttpCredentialsAdapter_customUniverseDomain() {
    String rootUrl = "https://test.googleapis.com/";
    String applicationName = "Test Application";
    String servicePath = "test/";
    String universeDomain = "random.com";

    final AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                TRANSPORT,
                rootUrl,
                servicePath,
                JSON_OBJECT_PARSER,
                new TestHttpRequestInitializer())
            .setApplicationName(applicationName)
            .setUniverseDomain(universeDomain)
            .build();
    assertThrows(
        IOException.class,
        new ThrowingRunnable() {
          @Override
          public void run() throws IOException {
            client.validateUniverseDomain();
          }
        });
  }

  private static final String TEST_RESUMABLE_REQUEST_URL =
      "http://www.test.com/request/url?uploadType=resumable";
  private static final String TEST_UPLOAD_URL = "http://www.test.com/media/upload/location";
  private static final String TEST_CONTENT_TYPE = "image/jpeg";

  private static class MediaTransport extends MockHttpTransport {

    int bytesUploaded;
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    boolean contentLengthNotSpecified;
    List<String> userAgentsRecorded = new ArrayList<>();

    protected MediaTransport() {}

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      if (name.equals("POST")) {
        assertEquals(TEST_RESUMABLE_REQUEST_URL, url);

        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            // Assert that the required headers are set.
            if (!contentLengthNotSpecified) {
              assertEquals(
                  Integer.toString(contentLength), getFirstHeaderValue("x-upload-content-length"));
            }
            assertEquals(TEST_CONTENT_TYPE, getFirstHeaderValue("x-upload-content-type"));
            // This is the initiation call. Return 200 with the upload URI.
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(200);
            response.addHeader("Location", TEST_UPLOAD_URL);
            return response;
          }
        };
      }
      assertEquals(TEST_UPLOAD_URL, url);

      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

          String bytesRange =
              bytesUploaded + "-" + (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1);
          String expectedContentRange = "bytes " + bytesRange + "/" + contentLength;
          assertEquals(expectedContentRange, getFirstHeaderValue("Content-Range"));
          bytesUploaded += MediaHttpUploader.DEFAULT_CHUNK_SIZE;
          userAgentsRecorded.add(getFirstHeaderValue("User-Agent"));

          if (bytesUploaded == contentLength) {
            // Return 200 since the upload is complete.
            response.setStatusCode(200);
            response.setContent("{\"foo\":\"somevalue\"}");
            response.setContentType(Json.MEDIA_TYPE);
          } else {
            // Return 308 and the range since the upload is incomplete.
            response.setStatusCode(308);
            response.addHeader("Range", bytesRange);
          }
          return response;
        }
      };
    }
  }

  public static class A {
    @Key String foo;
  }

  public void testMediaUpload() throws Exception {
    MediaTransport transport = new MediaTransport();
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                transport, TEST_RESUMABLE_REQUEST_URL, "", JSON_OBJECT_PARSER, null)
            .setApplicationName("Test Application")
            .build();
    InputStream is = new ByteArrayInputStream(new byte[MediaHttpUploader.DEFAULT_CHUNK_SIZE]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
    MockGoogleClientRequest<A> rq =
        new MockGoogleClientRequest<A>(client, "POST", "", null, A.class);
    rq.initializeMediaUpload(mediaContent);
    A result = rq.execute();
    assertEquals("somevalue", result.foo);
  }

  public void testMediaUpload_applicationNameAsUserAgent() throws Exception {
    MediaTransport fakeTransport = new MediaTransport();
    String applicationName = "Foo/1.0 (BAR:Baz/1.0) XYZ/1.0";
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                fakeTransport, TEST_RESUMABLE_REQUEST_URL, "", JSON_OBJECT_PARSER, null)
            .setApplicationName(applicationName)
            .build();
    InputStream is = new ByteArrayInputStream(new byte[MediaHttpUploader.DEFAULT_CHUNK_SIZE]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
    MockGoogleClientRequest<A> rq =
        new MockGoogleClientRequest<A>(client, "POST", "", null, A.class);

    rq.initializeMediaUpload(mediaContent);
    MediaHttpUploader mediaHttpUploader = rq.getMediaHttpUploader();
    mediaHttpUploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    assertEquals(1, fakeTransport.userAgentsRecorded.size());
    for (String userAgent : fakeTransport.userAgentsRecorded) {
      assertTrue(
          "UserAgent header does not have expected value in requests",
          userAgent.contains(applicationName));
    }
  }

  private static class GZipCheckerInitializer implements HttpRequestInitializer {

    private boolean gzipDisabled;

    public GZipCheckerInitializer(boolean gzipDisabled) {
      this.gzipDisabled = gzipDisabled;
    }

    public void initialize(HttpRequest request) {
      request.setInterceptor(new GZipCheckerInterceptor(gzipDisabled));
    }
  }

  private static class GZipCheckerInterceptor implements HttpExecuteInterceptor {

    private boolean gzipDisabled;

    public GZipCheckerInterceptor(boolean gzipDisabled) {
      this.gzipDisabled = gzipDisabled;
    }

    public void intercept(HttpRequest request) {
      assertEquals(
          !gzipDisabled && !(request.getContent() instanceof EmptyContent),
          request.getEncoding() != null);
    }
  }

  public void testMediaUpload_disableGZip() throws Exception {
    MediaTransport transport = new MediaTransport();
    transport.contentLengthNotSpecified = true;
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                transport,
                TEST_RESUMABLE_REQUEST_URL,
                "",
                JSON_OBJECT_PARSER,
                new GZipCheckerInitializer(true))
            .setApplicationName("Test Application")
            .build();
    InputStream is = new ByteArrayInputStream(new byte[MediaHttpUploader.DEFAULT_CHUNK_SIZE]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MockGoogleClientRequest<A> rq =
        new MockGoogleClientRequest<A>(client, "POST", "", null, A.class);
    rq.initializeMediaUpload(mediaContent);
    rq.setDisableGZipContent(true);
    A result = rq.execute();
    assertEquals("somevalue", result.foo);
  }

  public void testMediaUpload_enableGZip() throws Exception {
    MediaTransport transport = new MediaTransport();
    transport.contentLengthNotSpecified = true;
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                transport,
                TEST_RESUMABLE_REQUEST_URL,
                "",
                JSON_OBJECT_PARSER,
                new GZipCheckerInitializer(false))
            .setApplicationName("Test Application")
            .build();
    InputStream is = new ByteArrayInputStream(new byte[MediaHttpUploader.DEFAULT_CHUNK_SIZE]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MockGoogleClientRequest<A> rq =
        new MockGoogleClientRequest<A>(client, "POST", "", null, A.class);
    rq.initializeMediaUpload(mediaContent);
    rq.setDisableGZipContent(false);
    A result = rq.execute();
    assertEquals("somevalue", result.foo);
  }

  public void testMediaUpload_defaultGZip() throws Exception {
    MediaTransport transport = new MediaTransport();
    transport.contentLengthNotSpecified = true;
    AbstractGoogleClient client =
        new MockGoogleClient.Builder(
                transport,
                TEST_RESUMABLE_REQUEST_URL,
                "",
                JSON_OBJECT_PARSER,
                new GZipCheckerInitializer(false))
            .setApplicationName("Test Application")
            .build();
    InputStream is = new ByteArrayInputStream(new byte[MediaHttpUploader.DEFAULT_CHUNK_SIZE]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MockGoogleClientRequest<A> rq =
        new MockGoogleClientRequest<A>(client, "POST", "", null, A.class);
    rq.initializeMediaUpload(mediaContent);
    A result = rq.execute();
    assertEquals("somevalue", result.foo);
  }
}
