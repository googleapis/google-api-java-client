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

import com.google.api.client.http.GenericUrl;
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

/**
 * Tests {@link MediaHttpUploader}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class MediaHttpUploaderTest extends TestCase {

  private static final String TEST_REQUEST_URL =
      "http://www.test.com/request/url?uploadType=resumable";
  private static final String TEST_UPLOAD_URL = "http://www.test.com/media/upload/location";
  private static final String TEST_CONTENT_TYPE = "image/jpeg";

  private static class MediaTransport extends MockHttpTransport {

    int lowLevelExecCalls;
    int bytesUploaded;
    int contentLength;
    boolean testServerError;
    boolean testClientError;

    protected MediaTransport(int contentLength, boolean testServerError, boolean testClientError) {
      this.contentLength = contentLength;
      this.testServerError = testServerError;
      this.testClientError = testClientError;
    }

    @Override
    public LowLevelHttpRequest buildPostRequest(String url) {
      assertEquals(TEST_REQUEST_URL, url);

      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() {
          lowLevelExecCalls++;
          // Assert that the required headers are set.
          assertEquals(
              Integer.toString(contentLength), getHeaders().get("X-Upload-Content-Length").get(0));
          assertEquals(TEST_CONTENT_TYPE, getHeaders().get("X-Upload-Content-Type").get(0));
          // This is the initiation call. Return 200 with the upload URI.
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setStatusCode(200);
          response.addHeader("Location", TEST_UPLOAD_URL);
          return response;
        }
      };
    }

    @Override
    public LowLevelHttpRequest buildPutRequest(String url) {
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
              assertEquals("bytes */" + contentLength, getHeaders().get("Content-Range").get(0));
              // Return 308 and the Range of bytes uploaded so far.
              response.setStatusCode(308);
              response.addHeader("Range", "0-" + (MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1));
              return response;
            }
          } else if (testClientError) {
            // Return a 404.
            response.setStatusCode(404);
            return response;
          }

          String bytesRange =
              bytesUploaded + "-" + (bytesUploaded + MediaHttpUploader.DEFAULT_CHUNK_SIZE - 1);
          String expectedContentRange = "bytes " + bytesRange + "/" + contentLength;
          assertEquals(expectedContentRange, getHeaders().get("Content-Range").get(0));
          bytesUploaded += MediaHttpUploader.DEFAULT_CHUNK_SIZE;

          if (bytesUploaded == contentLength) {
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

  private static class ProgressListenerWithTwoUploadCalls
      implements
        MediaHttpUploaderProgressListener {

    int progressListenerCalls;

    public ProgressListenerWithTwoUploadCalls() {
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
          assertEquals(0.5, uploader.getProgress());
          break;
        case MEDIA_COMPLETE:
          // Assert that the 4th call is media complete.
          assertEquals(4, progressListenerCalls);
          assertEquals(1.0, uploader.getProgress());
          break;
      }
    }
  }

  public void testUploadOneCall() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_REQUEST_URL));

    // There should be 2 calls made. 1 initiation request and 1 upload request.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadMultipleCalls() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 5;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_REQUEST_URL));

    // There should be 5 calls made. 1 initiation request and 5 upload requests.
    assertEquals(6, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadProgressListener() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setProgressListener(new ProgressListenerWithTwoUploadCalls());
    uploader.upload(new GenericUrl(TEST_REQUEST_URL));
  }

  public void testUploadServerErrorWithBackOffEnabled() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.upload(new GenericUrl(TEST_REQUEST_URL));

    // There should be 5 calls made. 1 initiation request, 1 upload request with server error, 1
    // call to query the range and 2 upload requests.
    assertEquals(5, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadServerErrorWithBackOffDisabled() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, true, false);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);
    uploader.setBackOffPolicyEnabled(false);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_REQUEST_URL));
    assertEquals(500, response.getStatusCode());

    // There should be 3 calls made. 1 initiation request, 1 successful upload request and 1 upload
    // request with server error.
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testUploadClientError() throws IOException {
    int contentLength = MediaHttpUploader.DEFAULT_CHUNK_SIZE * 2;
    MediaTransport fakeTransport = new MediaTransport(contentLength, false, true);
    InputStream is = new ByteArrayInputStream(new byte[contentLength]);
    InputStreamContent mediaContent = new InputStreamContent(TEST_CONTENT_TYPE, is);
    mediaContent.setLength(contentLength);
    MediaHttpUploader uploader = new MediaHttpUploader(mediaContent, fakeTransport, null);

    HttpResponse response = uploader.upload(new GenericUrl(TEST_REQUEST_URL));
    assertEquals(404, response.getStatusCode());

    // There should be 2 calls made. 1 initiation request and 1 upload request that returned a 404.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }
}
