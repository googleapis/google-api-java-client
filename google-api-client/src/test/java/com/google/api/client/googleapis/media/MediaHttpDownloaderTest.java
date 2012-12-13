/*
 * Copyright (c) 2012 Google Inc.
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
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Tests {@link MediaHttpDownloader}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class MediaHttpDownloaderTest extends TestCase {

  private static final String TEST_REQUEST_URL = "http://www.test.com/request/url?alt=media";
  private static final int TEST_CHUNK_SIZE = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;

  private static class MediaTransport extends MockHttpTransport {

    int lowLevelExecCalls;
    int contentLength;
    int bytesDownloaded;
    int lastBytePos = -1;
    boolean testServerError;
    boolean testClientError;
    boolean directDownloadEnabled;

    protected MediaTransport(int contentLength) {
      this.contentLength = contentLength;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      assertEquals(TEST_REQUEST_URL, url);

      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() {
          lowLevelExecCalls++;
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();

          if (directDownloadEnabled) {
            if (bytesDownloaded != 0) {
              if (lastBytePos == -1) {
                assertEquals("bytes=" + bytesDownloaded + "-", getFirstHeaderValue("Range"));
              } else {
                assertEquals(
                    "bytes=" + bytesDownloaded + "-" + lastBytePos, getFirstHeaderValue("Range"));
              }
            }
            response.setStatusCode(200);
            response.addHeader("Content-Length", String.valueOf(contentLength));
            response.setContent(
                new ByteArrayInputStream(new byte[contentLength - bytesDownloaded]));
            return response;
          }

          // Assert that the required headers are set.
          long currentRequestLastBytePos = bytesDownloaded + TEST_CHUNK_SIZE - 1;
          if (lastBytePos != -1) {
            currentRequestLastBytePos = Math.min(lastBytePos, currentRequestLastBytePos);
          }
          assertEquals("bytes=" + bytesDownloaded + "-" + currentRequestLastBytePos,
                       getFirstHeaderValue("Range"));

          if (testServerError && lowLevelExecCalls == 2) {
            // Send a server error in the 2nd request.
            response.setStatusCode(500);
            return response;
          } else if (testClientError) {
            // Return a 404.
            response.setStatusCode(404);
            return response;
          }

          response.setStatusCode(206);
          int upper = Math.min(bytesDownloaded + TEST_CHUNK_SIZE, contentLength) - 1;
          response.addHeader(
              "Content-Range", "bytes " + bytesDownloaded + "-" + upper + "/" + contentLength);
          int bytesDownloadedCur = upper - bytesDownloaded + 1;
          response.setContent(new ByteArrayInputStream(new byte[bytesDownloadedCur]));
          bytesDownloaded += bytesDownloadedCur;
          return response;
        }
      };
    }
  }

  private static class ProgressListenerWithTwoDownloadCalls
      implements
        MediaHttpDownloaderProgressListener {

    int progressListenerCalls;

    public ProgressListenerWithTwoDownloadCalls() {
    }

    public void progressChanged(MediaHttpDownloader downloader) {
      progressListenerCalls++;

      switch (downloader.getDownloadState()) {
        case MEDIA_IN_PROGRESS:
          // Assert that the 1st call is media in progress and check the progress percent.
          assertTrue(progressListenerCalls == 1);
          assertEquals(0.5, downloader.getProgress());
          break;
        case MEDIA_COMPLETE:
          // Assert that the 2nd call is media complete.
          assertEquals(2, progressListenerCalls);
          assertEquals(1.0, downloader.getProgress());
          break;
      }
    }
  }

  public void testDownloadOneCallHalfChunkSize() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE / 2;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(contentLength, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDownloadOneCallMaxChunkSize() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
      assertEquals(TEST_CHUNK_SIZE, outputStream.size());
    } finally {
      outputStream.close();
    }

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testSetBytesDownloaded() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.bytesDownloaded = contentLength - 10000;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setBytesDownloaded(contentLength - 10000);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(10000, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDownloadMultipleCallsMaxChunkSize() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE * 3;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(contentLength, outputStream.size());

    // There should be 3 download calls made.
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testDownloadProgressListener() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE * 2;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setProgressListener(new ProgressListenerWithTwoDownloadCalls());
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
  }

  public void testDownloadServerErrorWithBackOffEnabled() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE * 2;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);

    // There should be 3 calls made: 1 download request with server error and 2 successful download
    // requests.
    assertEquals(3, fakeTransport.lowLevelExecCalls);
  }

  public void testDownloadServerErrorWithBackOffDisabled() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE * 2;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testServerError = true;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setBackOffPolicyEnabled(false);
    try {
      downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
      fail("Expected " + HttpResponseException.class);
    } catch (HttpResponseException e) {
      // Expected
    }

    // There should be 2 calls made: 1 successful download request and 1 download request with
    // server error.
    assertEquals(2, fakeTransport.lowLevelExecCalls);
  }

  public void testDownloadClientError() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE * 2;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.testClientError = true;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setBackOffPolicyEnabled(false);
    try {
      downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
      fail("Expected " + HttpResponseException.class);
    } catch (HttpResponseException e) {
      // Expected
    }

    // There should be only 1 call made: 1 download request that returned a 404.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testDirectMediaDownload() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directDownloadEnabled = true;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setDirectDownloadEnabled(true);
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(TEST_CHUNK_SIZE, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testSetBytesDownloadedWithIllegalArguments() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    try {
      downloader.setBytesDownloaded(-1);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Anything >= 0 should be accepted.
    downloader.setBytesDownloaded(0);
    downloader.setBytesDownloaded(1);
  }

  public void testSetContentRangeWithIllegalArguments() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);

    try {
      downloader.setContentRange(-1, 0);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    try {
      downloader.setContentRange(1, 0);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    try {
      downloader.setContentRange(200, 199);
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // The following should be accepted
    downloader.setContentRange(0, 0);
    downloader.setContentRange(0, 1);
    downloader.setContentRange(1, 1);
    downloader.setContentRange(199, 200);
    downloader.setContentRange(200, 200);
  }

  public void testSetBytesDownloadedWithDirectDownload() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directDownloadEnabled = true;
    fakeTransport.bytesDownloaded = contentLength - 10000;
    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setDirectDownloadEnabled(true);
    downloader.setBytesDownloaded(contentLength - 10000);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(10000, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testSetContentRangeWithResumableDownload() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.bytesDownloaded = contentLength - 10000;
    fakeTransport.lastBytePos = contentLength;

    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setContentRange(contentLength - 10000, contentLength);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(10000, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }

  public void testSetContentRangeWithDirectDownload() throws Exception {
    int contentLength = MediaHttpDownloader.MAXIMUM_CHUNK_SIZE;
    MediaTransport fakeTransport = new MediaTransport(contentLength);
    fakeTransport.directDownloadEnabled = true;
    fakeTransport.bytesDownloaded = contentLength - 10000;
    fakeTransport.lastBytePos = contentLength;

    MediaHttpDownloader downloader = new MediaHttpDownloader(fakeTransport, null);
    downloader.setDirectDownloadEnabled(true);
    downloader.setContentRange(contentLength - 10000, contentLength);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    downloader.download(new GenericUrl(TEST_REQUEST_URL), outputStream);
    assertEquals(10000, outputStream.size());

    // There should be 1 download call made.
    assertEquals(1, fakeTransport.lowLevelExecCalls);
  }
}
