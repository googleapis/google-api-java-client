/*
 * Copyright 2011 Google Inc.
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
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.util.TestableByteArrayInputStream;
import com.google.api.client.util.BackOff;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

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

  private static Logger LOGGER = Logger.getLogger(HttpTransport.class.getName());
  private Level oldLevel;

  @Override
  public void setUp() {
    // suppress logging warnings to the console
    oldLevel = LOGGER.getLevel();
    LOGGER.setLevel(Level.SEVERE);
  }

  @Override
  public void tearDown() {
    // back to the standard logging level for console
    LOGGER.setLevel(oldLevel);
  }

  private static class MockHttpContent extends AbstractHttpContent {

    public MockHttpContent() {
      super("mock/type");
    }

    public void writeTo(OutputStream out) {
    }
  }

  static class MediaTransport extends MockHttpTransport {

    int lowLevelExecCalls;
    int bytesUploaded;
    final int contentLength;
    boolean testServerError;
    boolean testClientError;
    boolean testAuthenticationError;
    boolean directUploadEnabled;
    boolean directUploadWithMetadata;
    boolean contentLengthNotSpecified;
    boolean assertTestHeaders;
    boolean testIOException;
    boolean testMethodOverride;
    boolean force308OnRangeQueryResponse;
    int maxByteIndexUploadedOnError = MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1;

    /**
     * Bytes received by this server or {@code null} for none. To enable test content use the
     * following constructor {@link #MediaTransport(int, boolean)}.
     */
    byte[] bytesReceived;

    MediaTransport(int contentLength) {
      this.contentLength = contentLength;
    }

    MediaTransport(int contentLength, boolean testContent) {
      this(contentLength);
      if (testContent) {
        bytesReceived = new byte[contentLength];
      }
    }

    @Override
    public boolean supportsMethod(String method) throws IOException {
      return method.equals(HttpMethods.POST)
        || method.equals(HttpMethods.PUT)
        || method.equals(HttpMethods.GET);
    }

    @Override
    public LowLevelHttpRequest buildRequest(final String name, String url) {
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
            if (testMethodOverride) {
              assertEquals("PATCH", getFirstHeaderValue("X-HTTP-Method-Override"));
            }
            // This is the initiation call.
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            assertFalse(directUploadEnabled && testIOException);
            if (directUploadEnabled && testServerError && lowLevelExecCalls == 1) {
              // send a server error in the 1st request of a direct upload
              response.setStatusCode(500);
            } else if (testAuthenticationError) {
              // Return 404.
              response.setStatusCode(404);
            } else {
              // Return 200 with the upload URI.
              response.setStatusCode(200);
              if (!directUploadEnabled) {
                response.addHeader("Location", TEST_UPLOAD_URL);
              }
            }
            return response;
          }
        };
      }
      assertEquals(TEST_UPLOAD_URL, url);
      assertEquals("PUT", name);
      return new MockLowLevelHttpRequest() {
          @Override
        public LowLevelHttpResponse execute() throws IOException {
          lowLevelExecCalls++;
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          String contentRangeHeader = getFirstHeaderValue("Content-Range");
          if (testServerError || testIOException) {
            // TODO(peleyal): add test with two different failures in a row
            switch (lowLevelExecCalls) {
              case 3:
                // This request is where we simulate the error. Read into the bytesReceived array
                // up to maxByteIndexUploadedOnError to simulate the successfully-written bytes.
                int bytesToRead = maxByteIndexUploadedOnError + 1 - bytesUploaded;
                copyBytesToBytesReceivedArray(bytesToRead);
                bytesUploaded += bytesToRead;
                // Send a server error or throw IOException in response to the 3rd request.
                if (testIOException) {
                  throw new IOException();
                }
                response.setStatusCode(500);
                return response;
              case 4:
                // This request follows the error. Client should be asking server for its range.

                // Assert that the client sent the correct range query request header.
                if (!contentLengthNotSpecified
                    || (2 * MediaHttpUploader.DEFAULT_CHUNK_SIZE >= contentLength)) {
                  // Client should send */length if it knows the content length.
                  assertEquals("bytes */" + contentLength, contentRangeHeader);
                } else {
                  // Client should send */* if it does not know the content length.
                  assertEquals("bytes */*", contentRangeHeader);
                }
                // Return 308 if there are more bytes to upload or 308 is forced, else return 200.
                int statusCode = 200;
                if (contentLength != (maxByteIndexUploadedOnError + 1)
                    || force308OnRangeQueryResponse) {
                  statusCode = 308;
                }
                response.setStatusCode(statusCode);
                // Set the Range header with the bytes uploaded so far.
                response.addHeader("Range", "bytes=0-" + maxByteIndexUploadedOnError);
                return response;
              case 5:
                // If the file finished uploading, but we forced a 308 on request 4, validate
                // response and return 200.
                if (force308OnRangeQueryResponse
                    && (contentLength == (maxByteIndexUploadedOnError + 1))) {
                  assertEquals("bytes */" + contentLength, contentRangeHeader);
                  response.setStatusCode(200);
                  response.addHeader("Range", "bytes=0-" + contentLength);
                  return response;
                }
                break;
              default:
                break;
            }
          } else if (testClientError) {
            // Return a 411.
            response.setStatusCode(411);
            return response;
          }

          String bytesRange;
          if (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE > contentLength) {
            bytesRange = bytesUploaded + "-" + (contentLength - 1);
          } else {
            bytesRange =
                bytesUploaded + "-" + (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1);
          }
          String expectedContentRange;
          if (contentLength == 0) {
            expectedContentRange = "bytes */0";
          } else if (contentLengthNotSpecified
              && ((bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE) < contentLength)) {
            expectedContentRange = "bytes " + bytesRange + "/*";
          } else {
            expectedContentRange = "bytes " + bytesRange + "/" + contentLength;
          }

          assertEquals(expectedContentRange, contentRangeHeader);

          copyBytesToBytesReceivedArray(-1);

          bytesUploaded += MediaHttpUploader.DEFAULT_CHUNK_SIZE;

          if (bytesUploaded >= contentLength) {
            // Return 200 since the upload is complete.
            response.setStatusCode(200);
          } else {
            // Return 308 and the range since the upload is incomplete.
            response.setStatusCode(308);
            response.addHeader("Range", "bytes=" + bytesRange);
          }
          return response;
        }

        void copyBytesToBytesReceivedArray(int length) throws IOException {
          if (bytesReceived == null || length == 0) {
            return;
          }
          ByteArrayOutputStream stream = new ByteArrayOutputStream();
          this.getStreamingContent().writeTo(stream);
          byte[] currentRequest = stream.toByteArray();
          System.arraycopy(currentRequest, 0, bytesReceived, bytesUploaded,
              length == -1 ? currentRequest.length : length);
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
            assertEquals(0.5, uploader.getProgress(), 0.0);
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
            assertEquals(1.0, uploader.getProgress(), 0.0);
          }
          break;
        default:
          // TODO(b/18683919): go/enum-switch-lsc
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
            assertEquals(0.0, uploader.getProgress(), 0.0);
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
              // Expected.
            }
          } else {
            assertEquals(1.0, uploader.getProgress(), 0.0);
          }
          break;
        default:
          // TODO(b/18683919): go/enum-switch-lsc
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

    public void intercept(HttpRequest request) {
      assertEquals(!gzipDisabled && !(request.getContent() instanceof EmptyContent),
          request.getEncoding() != null);
    }
  }

  // TODO(peleyal): ZeroBackOffRequestInitializer can go into http testing package?

  static class ZeroBackOffRequestInitializer implements HttpRequestInitializer {
    public void initialize(HttpRequest request) {
      request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(BackOff.ZERO_BACKOFF));
      request.setUnsuccessfulResponseHandler(
          new HttpBackOffUnsuccessfulResponseHandler(BackOff.ZERO_BACKOFF));
    }
  }

  public void testUploadOneCall() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithPatch() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testMethodOverride = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setInitiationRequestMethod(HttpMethods.PATCH);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithGZipDisabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // Disable GZip content.
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new GZipCheckerInitializer(true));
    uploader.setDisableGZipContent(true);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithGZipEnabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // Enable GZip content.
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new GZipCheckerInitializer(false));
    uploader.setDisableGZipContent(false);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithDefaultGzip() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    fakeTransport.contentLengthNotSpecified = true;

    // GZip content must be disabled by default.
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new GZipCheckerInitializer(false));
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadOneCall_WithContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new GZipCheckerInitializer(true));
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithPatch() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testMethodOverride = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setInitiationRequestMethod(HttpMethods.PATCH);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithSpecifiedHeader() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithNoContentSizeProvidedChunkedInput() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]) {
        @Override
      public synchronized int read(byte[] b, int off, int len) {
        return super.read(b, off, Math.min(len, MediaHttpUploader.DEFAULT_CHUNK_SIZE / 10));
      }
    };
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 6 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls_WithNoContentSizeProvided_WithExtraByte() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5 + 1;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setProgressListener(new ResumableProgressListenerWithTwoUploadCalls());
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
  }

  public void testUploadProgressListener_WithNoContentSizeProvided() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
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

  public void testUploadServerError_WithoutUnsuccessfulHandler() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(500, response.getStatusCode());

    // There should be 3 calls made. 1 initiation request, 1 successful upload request and 1 upload
    // request with server error
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testUpload_ResumableIOException_WithIOExceptionHandler() throws Exception {
    subtestUpload_ResumableWithError(ErrorType.IO_EXCEPTION);
  }

  public void testUpload_ResumableServerError_WithoutUnsuccessfulHandler() throws Exception {
    subtestUpload_ResumableWithError(ErrorType.SERVER_UNAVAILABLE);
  }

  private void subtestUpload_ResumableWithError(ErrorType error) throws Exception {
    // no bytes were uploaded on the 2nd chunk. Client sends the following 3 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], DEFAULT-[2*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1, 3, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1, 3, false);

    // no bytes were uploaded on the 2nd chunk. Client sends the following 4 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], DEFAULT-[2*DEFAULT-1], 2*DEFAULT-[3*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1, 4, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1, 4, false);

    // all bytes were uploaded in the 2nd chunk, and the server sends a 200 on the client's resume.
    // Client sends the following 2 media chunks:
    //    0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 2, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 2, false);

    // all bytes were uploaded in the 2nd chunk, and we force the server to send a 308 on the
    // client's Range query of '*/<LENGTH>' instead of sending a 200 as in previous.
    // Client sends the following 3 media chunks:
    //    0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], empty (as */[2*DEFAULT])
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 3, true /* force 308 */);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 3, true /* force 308 */);

    // all bytes were uploaded in the 2nd chunk. Client sends the following 3 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], 2*DEFAULT-[3*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 3, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 - 1, 3, false);

    // part of the bytes were uploaded in the 2nd chunk. Client sends the following 3 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], DEFAULT*4/3-[2*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 4 / 3, 3, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE * 4 / 3, 3, false);

    // part of the bytes were uploaded in the 2nd chunk. Client sends the following 3 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], DEFAULT+5/[2*DEFAULT+2]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 + 3, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE + 4, 3, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2 + 3, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE + 4, 3, false);

    // only 1 byte were uploaded in the 2nd chunk. Client sends the following 3 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], [DEFAULT+1]-[2*DEFAULT-1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE, 3, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE, 3, false);

    // only 1 byte were uploaded in the 2nd chunk. Client sends the following 5 media chunks:
    // 0-[DEFAULT-1], DEFAULT-[2*DEFAULT-1], [DEFAULT+1]-[2*DEFAULT], [2*DEFAULT+1]-[3*DEFAULT],
    // [3*DEFAULT+1]-[3*DEFAULT+1]
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3 + 2, false,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE, 5, false);
    subtestUpload_ResumableWithError(error, MediaHttpUploader.DEFAULT_CHUNK_SIZE * 3 + 2, true,
        MediaHttpUploader.DEFAULT_CHUNK_SIZE, 5, false);
  }

  /**
   * Error type represents an error (I/O exception or server unavailable error) which will be raise
   * when uploading media to the server.
   */
  enum ErrorType {
    IO_EXCEPTION, SERVER_UNAVAILABLE
  }

  public void subtestUpload_ResumableWithError(ErrorType error, int contentLength,
      boolean contentLengthKnown, int maxByteIndexUploadedOnError, int chunks,
      boolean force308OnRangeQueryResponse) throws Exception {
    MediaTransport fakeTransport = new MediaTransport(contentLength, true);
    if (error == ErrorType.IO_EXCEPTION) {
      fakeTransport.testIOException = true;
    } else if (error == ErrorType.SERVER_UNAVAILABLE) {
      fakeTransport.testServerError = true;
    }
    fakeTransport.contentLengthNotSpecified = !contentLengthKnown;
    fakeTransport.maxByteIndexUploadedOnError = maxByteIndexUploadedOnError;
    fakeTransport.force308OnRangeQueryResponse = force308OnRangeQueryResponse;
    byte[] testedData = new byte[contentLength];
    new Random().nextBytes(testedData);
    InputStream is = new ByteArrayInputStream(testedData);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    if (contentLengthKnown) {
      mediaContent.setLength(contentLength);
    }
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new ZeroBackOffRequestInitializer());

    // disable GZip - so we would be able to test byte received by server.
    uploader.setDisableGZipContent(true);
    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(200, response.getStatusCode());

    // There should be the following number of calls made:
    // 1 initiation request + 1 call to query the range + chunks
    int calls = 2 + chunks;
    assertEquals(calls, fakeTransport.lowLevelExecCalls);

    assertTrue(Arrays.equals(testedData, fakeTransport.bytesReceived));
  }

  public void testUploadIOException_WithoutIOExceptionHandler() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testIOException = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    try {
      uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
      fail("expected " + IOException.class);
    } catch (IOException e) {
      // There should be 3 calls made. 1 initiation request, 1 successful upload request,
      // and 1 upload request with server error
      assertEquals(3, fakeTransport.lowLevelExecCalls);
    }
  }

  public void testUploadServerErrorWithBackOffDisabled_WithNoContentSizeProvided()
      throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertEquals(500, response.getStatusCode());

    // There should be 3 calls made. 1 initiation request, 1 successful upload request and 1 upload
    // request with server error
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadAuthenticationError() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testClientError = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testClientError = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
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

  public void testDirectUploadServerErrorWithBackOffEnabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    fakeTransport.directUploadEnabled = true;
    ByteArrayContent mediaContent =
        new ByteArrayContent(TEST_CONTENT_TYPE, new byte[contentLength]);
    MediaHttpUploader uploader =
        new MediaHttpUploader(mediaContent, fakeTransport, new ZeroBackOffRequestInitializer());
    uploader.setDirectUploadEnabled(true);
    uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));

    // should be 2 calls made: 1 upload request with server error, 1 successful upload request
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectUploadServerErrorWithBackOffDisabled() throws Exception {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    fakeTransport.directUploadEnabled = true;
    ByteArrayContent mediaContent =
        new ByteArrayContent(TEST_CONTENT_TYPE, new byte[contentLength]);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setDirectUploadEnabled(true);
    uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));

    // should be 1 call made: 1 upload request with server error
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaUploadWithZeroContent() throws Exception {
    int contentLength = 0;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directUploadEnabled = true;
    ByteArrayContent mediaContent =
        new ByteArrayContent(TEST_CONTENT_TYPE, new byte[contentLength]);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setDirectUploadEnabled(true);
    uploader.upload(new GenericUrl(TEST_DIRECT_REQUEST_URL));

    // There should be only 1 call made for direct media upload.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testResumableMediaUploadWithZeroContent() throws Exception {
    int contentLength = 0;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    ByteArrayContent mediaContent =
        new ByteArrayContent(TEST_CONTENT_TYPE, new byte[contentLength]);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testResumableMediaUploadWithZeroContentOfUnknownLength() throws Exception {
    int contentLength = 0;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.contentLengthNotSpecified = true;
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testResumableMediaUploadWithContentClose() throws Exception {
    int contentLength = 0;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    TestableByteArrayInputStream inputStream =
        new TestableByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent =
        new InputStreamContent(TEST_CONTENT_TYPE, inputStream).setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertTrue(inputStream.isClosed());
  }

  public void testResumableMediaUploadWithoutContentClose() throws Exception {
    int contentLength = 0;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    TestableByteArrayInputStream inputStream =
        new TestableByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(
        TEST_CONTENT_TYPE, inputStream).setLength(contentLength).setCloseInputStream(false);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
    assertFalse(inputStream.isClosed());
  }

  class SlowWriter implements Runnable {
    final private OutputStream outputStream;
    final private int contentLength;

    SlowWriter(OutputStream outputStream, int contentLength) {
      this.outputStream = outputStream;
      this.contentLength = contentLength;
    }

    @Override
    public void run() {
      try {
        for (int i = 0; i < contentLength; i++) {
          outputStream.write(i);
          Thread.sleep(1000);
        }
        outputStream.close();
      } catch (IOException e) {
        // ignore
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  class TimeoutRequestInitializer implements HttpRequestInitializer {
    class TimingInterceptor implements HttpExecuteInterceptor {
      private long initTime;

      TimingInterceptor() {
        initTime = System.currentTimeMillis();
      }

      @Override
      public void intercept(HttpRequest request) {
        assertTrue(
                "Request initialization to execute should be fast",
                System.currentTimeMillis() - initTime < 100L
                );
      }
    }

    @Override
    public void initialize(HttpRequest request) {
      request.setInterceptor(new TimingInterceptor());
    }
  }

  public void testResumableSlowUpload() throws Exception {
    int contentLength = 3;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.contentLengthNotSpecified = true;
    PipedOutputStream outputStream = new PipedOutputStream();
    InputStream inputStream = new PipedInputStream(outputStream);

    Thread thread = new Thread(new SlowWriter(outputStream, contentLength));
    thread.start();

    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, inputStream);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, new TimeoutRequestInitializer());
    uploader.setDirectUploadEnabled(false);
    uploader.upload(new GenericUrl(TEST_RESUMABLE_REQUEST_URL));
  }
}
