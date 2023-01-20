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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests {@link AbstractGoogleClient}.
 *
 * @author Yaniv Inbar
 */
public class AbstractGoogleClientTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final JsonObjectParser JSON_OBJECT_PARSER = new JsonObjectParser(JSON_FACTORY);
  private static final HttpTransport TRANSPORT = new MockHttpTransport();

  private static class TestRemoteRequestInitializer implements GoogleClientRequestInitializer {

    boolean isCalled;

    TestRemoteRequestInitializer() {}

    public void initialize(AbstractGoogleClientRequest<?> request) {
      isCalled = true;
    }
  }

  public void testGoogleClientBuilder() {
    String rootUrl = "http://www.testgoogleapis.com/test/";
    String servicePath = "path/v1/";
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
