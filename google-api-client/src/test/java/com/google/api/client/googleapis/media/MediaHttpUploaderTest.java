/*
 * Copyright (c) 2011 Google Inc.
 *
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

package com.google.api.client.googleapis.media;

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tests {@link MediaHttpUploader}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class MediaHttpUploaderTest extends TestCase {

  private static final String TEST_RESUMABLE_REQUEST_URL =
      "http://www.test.com/request/url?uploadType=resumable";
  private static final String TEST_DIRECT_REQUEST_URL =
      "http://www.test.com/request/url?uploadType=media";
  private static final String TEST_MULTIPART_REQUEST_URL =
      "http://www.test.com/request/url?uploadType=multipart";
  private static final String TEST_UPLOAD_URL = "http://www.test.com/media/upload/location";
  private static final String TEST_CONTENT_TYPE = "image/jpeg";

  private static class MockHttpContent extends AbstractHttpContent {

    public MockHttpContent() {
      super("mock/type");
    }

    public void writeTo(OutputStream out) {
    }
  }

  private static class MediaTransport extends MockHttpTransport {

    int lowLevelExecCalls;
    int bytesUploaded;
    int contentLength;
    boolean testServerError;
    boolean testClientError;
    boolean testAuthenticationError;
    boolean directUploadEnabled;
    boolean directUploadWithMetadata;
    boolean contentLengthNotSpecified;
    boolean assertTestHeaders;

    protected MediaTransport(int contentLength, boolean testServerError, boolean testClientError,
        boolean directUploadEnabled) {
      this.contentLength = contentLength;
      this.testServerError = testServerError;
      this.testClientError = testClientError;
      this.directUploadEnabled = directUploadEnabled;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      if (name.equals("POST")) {
        if (directUploadEnabled) {
          if (directUploadWithMetadata) {
            assertEquals(TEST_MULTIPART_REQUEST_URL, url);
          } else {
            assertEquals(TEST_DIRECT_REQUEST_URL, url);
          }
        } else {
          assertEquals(TEST_RESUMABLE_REQUEST_URL, url);
        }

        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            lowLevelExecCalls++;
            if (!directUploadEnabled) {
              // Assert that the required headers are set.
              if (!contentLengthNotSpecified) {
                assertEquals(Integer.toString(contentLength),
                    getFirstHeaderValue("x-upload-content-length"));
              }
              assertEquals(TEST_CONTENT_TYPE, getFirstHeaderValue("x-upload-content-type"));
            }
            if (assertTestHeaders) {
              assertEquals("test-header-value", getFirstHeaderValue("test-header-name"));
            }

            // This is the initiation call.
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            if (testAuthenticationError) {
              // Return 404.
              response.setStatusCode(404);
            } else {
              // Return 200 with the upload URI.
              response.setStatusCode(200);
              response.addHeader("Location", TEST_UPLOAD_URL);
            }
            return response;
          }
        };
      }
      assertEquals(TEST_UPLOAD_URL, url);

      return new MockLowLevelHttpRequest() {
          @Override
        public LowLevelHttpResponse execute() {
          lowLevelExecCalls++;
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          if (testServerError) {
            if (lowLevelExecCalls == 3) {
              // Send a server error in the 3rd request.
              response.setStatusCode(500);
              return response;
            } else if (lowLevelExecCalls == 4) {
              // Assert that the 4th request is a range query request.
              if (contentLengthNotSpecified) {
                assertEquals("bytes */*", getFirstHeaderValue("Content-Range"));
              } else {
                assertEquals("bytes */" + contentLength, getFirstHeaderValue("Content-Range"));
              }
              // Return 308 and the Range of bytes uploaded so far.
              response.setStatusCode(308);
              response.addHeader("Range", "0-" + (MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1));
              return response;
            }
          } else if (testClientError) {
            // Return a 411.
            response.setStatusCode(411);
            return response;
          }

          String bytesRange;
          if (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE > contentLength) {
            bytesRange = bytesUploaded + "-" + bytesUploaded;
          } else {
            bytesRange =
                bytesUploaded + "-" + (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1);
          }
          String expectedContentRange;
          if (contentLengthNotSpecified && ((
              bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE) < contentLength
                  || testServerError)) {
            expectedContentRange = "bytes " + bytesRange + "/*";
          } else {
            expectedContentRange = "bytes " + bytesRange + "/" + contentLength;
          }

          assertEquals(expectedContentRange, getFirstHeaderValue("Content-Range"));
          bytesUploaded += MediaHttpUploader.DEFAULT_CHUNK_SIZE;

          if (bytesUploaded >= contentLength) {
            // Return 200 since the upload is complete.
            response.setStatusCode(200);
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

  private static class ResumableProgressListenerWithTwoUploadCalls
      implements
        MediaHttpUploaderProgressListener {

    int progressListenerCalls;
    boolean contentLengthNotSpecified;

    public ResumableProgressListenerWithTwoUploadCalls() {
    }

    public void progressChanged(MediaHttpUploader uploader) throws IOException {
      progressListenerCalls++;

      switch (uploader.getUploadState()) {
        case INITIATION_STARTED:
          // Assert that the first call is initiation started.
          assertEquals(1, progressListenerCalls);
          break;
        case INITIATION_COMPLETE:
          // Assert that the 2nd call is initiation complete.
          assertEquals(2, progressListenerCalls);
          break;
        case MEDIA_IN_PROGRESS:
          // Assert that the 3rd call is media in progress and check the progress percent.
          assertTrue(progressListenerCalls == 3);
          if (contentLengthNotSpecified) {
            try {
              uploader.getProgress();
              fail("Expected " + IllegalArgumentException.class);
            } catch (IllegalArgumentException iae) {
              // Expected.
            }
          } else {
            assertEquals(0.5, uploader.getProgress());
          }
          break;
        case MEDIA_COMPLETE:
          // Assert that the 4th call is media complete.
          assertEquals(4, progressListenerCalls);
          if (contentLengthNotSpecified) {
            try {
              uploader.getProgress();
              fail("Expected " + IllegalArgumentException.class);
            } catch (IllegalArgumentException iae) {
              // Expected.
            }
          } else {
            assertEquals(1.0, uploader.getProgress());
          }
          break;
      }
    }
  }

  private static class DirectProgressListener implements MediaHttpUploaderProgressListener {

    int progressListenerCalls;
    boolean contentLengthNotSpecified;

    public DirectProgressListener() {
    }

    public void progressChanged(MediaHttpUploader uploader) throws IOException {
      progressListenerCalls++;

      switch (uploader.getUploadState()) {
        case MEDIA_IN_PROGRESS:
          // Assert that the first call is media in progress.
          assertEquals(1, progressListenerCalls);
          if (contentLengthNotSpecified) {
            try {
              uploader.getProgress();
              fail("Expected " + IllegalArgumentException.class);
            } catch (IllegalArgumentException iae) {
              // Expected.
            }
          } else {
            assertEquals(0.0, uploader.getProgress());
          }
          break;
        case MEDIA_COMPLETE:
          // Assert that the 2nd call is media complete.
          assertEquals(2, progressListenerCalls);
          if (contentLengthNotSpecified) {
            try {
              uploader.getProgress();
              fail("Expected " + IllegalArgumentException.class);
            } catch (IllegalArgumentException iae) {
              //Expected.
            }
          } else {
            assertEquals(1.0, uploader.getProgress());
          }
          break;
      }
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

    @SuppressWarnings("deprecation")
    public void intercept(HttpRequest request) {
      assertEquals(!gzipDisabled, request.getEncoding() != null);
    }
  }

  public void testUploadOneCall() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithGZipDisabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // Disable GZip content.
    MediaHttpUploader uploader = new MediaHttpUploader(
        mediaContent, fakeTransport, new GZipCheckerInitializer(true));
    uploader.setDisableGZipContent(true);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithGZipEnabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // Enable GZip content.
    MediaHttpUploader uploader = new MediaHttpUploader(
        mediaContent, fakeTransport, new GZipCheckerInitializer(false));
    uploader.setDisableGZipContent(false);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithDefaultGzip() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // GZip content must be disabled by default.
    MediaHttpUploader uploader = new MediaHttpUploader(
        mediaContent, fakeTransport, new GZipCheckerInitializer(false));
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithSpecifiedHeader() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.assertTestHeaders = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.getInitiationHeaders().set("test-header-name", "test-header-value");
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithNoContentSizeProvided_WithExtraByte() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5 + 1;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 7 calls made. 1 initiation request and 6 upload requests.
    assertEquals(7, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadProgressListener() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setProgressListener(new ResumableProgressListenerWithTwoUploadCalls());
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
  }

  public void testUploadProgressListener_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    ResumableProgressListenerWithTwoUploadCalls listener =
        new ResumableProgressListenerWithTwoUploadCalls();
    listener.contentLengthNotSpecified = true;
    uploader.setProgressListener(listener);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
  }

  public void testUploadServerErrorWithBackOffEnabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 5 calls made. 1 initiation request, 1 upload request with server error, 1
    // call to query the range and 2 upload requests.
    assertEquals(5, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadServerErrorWithBackOffEnabled_WithNoContentSizeProvided()
      throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 5 calls made. 1 initiation request, 1 upload request with server error, 1
    // call to query the range and 2 upload requests.
    assertEquals(5, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadServerErrorWithBackOffDisabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setBackOffPolicyEnabled(false);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(500, response.getStatusCode());

    // There should be 3 calls made. 1 initiation request, 1 successful upload request and 1 upload
    // request with server error.
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadServerErrorWithBackOffDisabled_WithNoContentSizeProvided()
      throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setBackOffPolicyEnabled(false);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(500, response.getStatusCode());

    // There should be 3 calls made. 1 initiation request, 1 successful upload request and 1 upload
    // request with server error.
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadAuthenticationError() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, false);
    fakeTransport.testAuthenticationError = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(404, response.getStatusCode());

    // There should be only 1 initiation request made with a 404.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadClientErrorInUploadCalls() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, true, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(411, response.getStatusCode());

    // There should be 2 calls made. 1 initiation request and 1 upload request that returned a 411.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadClientErrorInUploadCalls_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, true, false);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(411, response.getStatusCode());

    // There should be 2 calls made. 1 initiation request and 1 upload request that returned a 411.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUpload() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setProgressListener(new DirectProgressListener());
    // Enable direct media upload.
    uploader.setDirectUploadEnabled(true);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUpload_WithSpecifiedHeader() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    fakeTransport.assertTestHeaders = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.getInitiationHeaders().set("test-header-name", "test-header-value");
    uploader.setProgressListener(new DirectProgressListener());
    // Enable direct media upload.
    uploader.setDirectUploadEnabled(true);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUpload_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    DirectProgressListener listener = new DirectProgressListener();
    listener.contentLengthNotSpecified = true;
    uploader.setProgressListener(listener);
    // Enable direct media upload.
    uploader.setDirectUploadEnabled(true);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUploadWithMetadata() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    fakeTransport.directUploadWithMetadata = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setProgressListener(new DirectProgressListener());
    // Enable direct media upload.
    uploader.setDirectUploadEnabled(true);
    // Set Metadata
    uploader.setMetadata(new MockHttpContent());

    HttpResponse response = uploader.upload(new GenericUrl(TEST_MULTIPART_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUploadWithMetadata_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    fakeTransport.contentLengthNotSpecified = true;
    fakeTransport.directUploadWithMetadata = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    DirectProgressListener listener = new DirectProgressListener();
    listener.contentLengthNotSpecified = true;
    uploader.setProgressListener(listener);
    // Enable direct media upload.
    uploader.setDirectUploadEnabled(true);
    // Set Metadata
    uploader.setMetadata(new MockHttpContent());

    HttpResponse response = uploader.upload(new GenericUrl(TEST_MULTIPART_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testSetChunkSize() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false, true);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    // None of the below should throw Exceptions.
    uploader.setChunkSize(MediaHttpUploader.DEFAULT_CHUNK_SIZE);
    uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
    uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 2);
    uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 3);
    uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 100);

    // Assert that specifying invalid chunk sizes throws an Exception.
    try {
      uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE - 1);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException iae) {
      // Expected.
    }
    try {
      uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE + 1);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException iae) {
      // Expected.
    }
    try {
      uploader.setChunkSize((int) (MediaHttpUploader.MINIMUM_CHUNK_SIZE * 2.5));
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException iae) {
      // Expected.
    }
  }
}
