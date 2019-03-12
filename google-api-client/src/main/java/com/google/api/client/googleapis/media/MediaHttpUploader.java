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

import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GZipEncoding;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.util.Beta;
import com.google.api.client.util.ByteStreams;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Media HTTP Uploader, with support for both direct and resumable media uploads. Documentation is
 * available <a href='http://code.google.com/p/google-api-java-client/wiki/MediaUpload'>here</a>.
 *
 * <p>
 * For resumable uploads, when the media content length is known, if the provided
 * {@link InputStream} has {@link InputStream#markSupported} as {@code false} then it is wrapped in
 * an {@link BufferedInputStream} to support the {@link InputStream#mark} and
 * {@link InputStream#reset} methods required for handling server errors. If the media content
 * length is unknown then each chunk is stored temporarily in memory. This is required to determine
 * when the last chunk is reached.
 * </p>
 *
 * <p>
 * See {@link #setDisableGZipContent(boolean)} for information on when content is gzipped and how to
 * control that behavior.
 * </p>
 *
 * <p>
 * Back-off is disabled by default. To enable it for an abnormal HTTP response and an I/O exception
 * you should call {@link HttpRequest#setUnsuccessfulResponseHandler} with a new
 * {@link HttpBackOffUnsuccessfulResponseHandler} instance and
 * {@link HttpRequest#setIOExceptionHandler} with {@link HttpBackOffIOExceptionHandler}.
 * </p>
 *
 * <p>
 * Upgrade warning: in prior version 1.14 exponential back-off was enabled by default for an
 * abnormal HTTP response and there was a regular retry (without back-off) when I/O exception was
 * thrown. Starting with version 1.15 back-off is disabled and there is no retry on I/O exception by
 * default.
 * </p>
 *
 * <p>
 * Upgrade warning: in prior version 1.16 {@link #serverErrorCallback} was public but starting with
 * version 1.17 it has been removed from the public API, and changed to be package private.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 *
 * @author rmistry@google.com (Ravi Mistry)
 * @author peleyal@google.com (Eyal Peled)
 */
@SuppressWarnings("deprecation")
public final class MediaHttpUploader {

  /**
   * Upload content type header.
   *
   * @since 1.13
   */
  public static final String CONTENT_LENGTH_HEADER = "X-Upload-Content-Length";

  /**
   * Upload content length header.
   *
   * @since 1.13
   */
  public static final String CONTENT_TYPE_HEADER = "X-Upload-Content-Type";

  /**
   * Upload state associated with the Media HTTP uploader.
   */
  public enum UploadState {
    /** The upload process has not started yet. */
    NOT_STARTED,

    /** Set before the initiation request is sent. */
    INITIATION_STARTED,

    /** Set after the initiation request completes. */
    INITIATION_COMPLETE,

    /** Set after a media file chunk is uploaded. */
    MEDIA_IN_PROGRESS,

    /** Set after the complete media file is successfully uploaded. */
    MEDIA_COMPLETE
  }

  /** The current state of the uploader. */
  private UploadState uploadState = UploadState.NOT_STARTED;

  static final int MB = 0x100000;
  private static final int KB = 0x400;

  /**
   * Minimum number of bytes that can be uploaded to the server (set to 256KB).
   */
  public static final int MINIMUM_CHUNK_SIZE = 256 * KB;

  /**
   * Default maximum number of bytes that will be uploaded to the server in any single HTTP request
   * (set to 10 MB).
   */
  public static final int DEFAULT_CHUNK_SIZE = 10 * MB;

  /** The HTTP content of the media to be uploaded. */
  private final AbstractInputStreamContent mediaContent;

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /** The transport to use for requests. */
  private final HttpTransport transport;

  /** HTTP content metadata of the media to be uploaded or {@code null} for none. */
  private HttpContent metadata;

  /**
   * The length of the HTTP media content.
   *
   * <p>
   * {@code 0} before it is lazily initialized in {@link #getMediaContentLength()} after which it
   * could still be {@code 0} for empty media content. Will be {@code < 0} if the media content
   * length has not been specified.
   * </p>
   */
  private long mediaContentLength;

  /**
   * Determines if media content length has been calculated yet in {@link #getMediaContentLength()}.
   */
  private boolean isMediaContentLengthCalculated;

  /**
   * The HTTP method used for the initiation request.
   *
   * <p>
   * Can only be {@link HttpMethods#POST} (for media upload) or {@link HttpMethods#PUT} (for media
   * update). The default value is {@link HttpMethods#POST}.
   * </p>
   */
  private String initiationRequestMethod = HttpMethods.POST;

  /** The HTTP headers used in the initiation request. */
  private HttpHeaders initiationHeaders = new HttpHeaders();

  /**
   * The HTTP request object that is currently used to send upload requests or {@code null} before
   * {@link #upload}.
   */
  private HttpRequest currentRequest;

  /** An Input stream of the HTTP media content or {@code null} before {@link #upload}. */
  private InputStream contentInputStream;

  /**
   * Determines whether direct media upload is enabled or disabled. If value is set to {@code true}
   * then a direct upload will be done where the whole media content is uploaded in a single request
   * If value is set to {@code false} then the upload uses the resumable media upload protocol to
   * upload in data chunks. Defaults to {@code false}.
   */
  private boolean directUploadEnabled;

  /**
   * Progress listener to send progress notifications to or {@code null} for none.
   */
  private MediaHttpUploaderProgressListener progressListener;

  /**
   * The media content length is used in the "Content-Range" header. If we reached the end of the
   * stream, this variable will be set with the length of the stream. This value is used only in
   * resumable media upload.
   */
  String mediaContentLengthStr = "*";

  /**
   * The number of bytes the server received so far. This value will not be calculated for direct
   * uploads when the content length is not known in advance.
   */
  // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
  private long totalBytesServerReceived;

  /**
   * Maximum size of individual chunks that will get uploaded by single HTTP requests. The default
   * value is {@link #DEFAULT_CHUNK_SIZE}.
   */
  private int chunkSize = DEFAULT_CHUNK_SIZE;

  /**
   * Used to cache a single byte when the media content length is unknown or {@code null} for none.
   */
  private Byte cachedByte;

  /**
   * The number of bytes the client had sent to the server so far or {@code 0} for none. It is used
   * for resumable media upload when the media content length is not specified.
   */
  private long totalBytesClientSent;

  /**
   * The number of bytes of the current chunk which was sent to the server or {@code 0} for none.
   * This value equals to chunk size for each chunk the client send to the server, except for the
   * ending chunk.
   */
  private int currentChunkLength;

  /**
   * The content buffer of the current request or {@code null} for none. It is used for resumable
   * media upload when the media content length is not specified. It is instantiated for every
   * request in {@link #buildContentChunk()} and is set to {@code null} when the
   * request is completed in {@link #upload}.
   */
  private byte currentRequestContentBuffer[];

  /**
   * Whether to disable GZip compression of HTTP content.
   *
   * <p>
   * The default value is {@code false}.
   * </p>
   */
  private boolean disableGZipContent;

  /** Sleeper. **/
  Sleeper sleeper = Sleeper.DEFAULT;

  /**
   * Construct the {@link MediaHttpUploader}.
   *
   * <p>
   * The input stream received by calling {@link AbstractInputStreamContent#getInputStream} is
   * closed when the upload process is successfully completed. For resumable uploads, when the media
   * content length is known, if the input stream has {@link InputStream#markSupported} as
   * {@code false} then it is wrapped in an {@link BufferedInputStream} to support the
   * {@link InputStream#mark} and {@link InputStream#reset} methods required for handling server
   * errors. If the media content length is unknown then each chunk is stored temporarily in memory.
   * This is required to determine when the last chunk is reached.
   * </p>
   *
   * @param mediaContent The Input stream content of the media to be uploaded
   * @param transport The transport to use for requests
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   */
  public MediaHttpUploader(AbstractInputStreamContent mediaContent, HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer) {
    this.mediaContent = Preconditions.checkNotNull(mediaContent);
    this.transport = Preconditions.checkNotNull(transport);
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
  }

  /**
   * Executes a direct media upload or resumable media upload conforming to the specifications
   * listed <a href='https://developers.google.com/api-client-library/java/google-api-java-client/media-upload'>here.</a>
   *
   * <p>
   * This method is not reentrant. A new instance of {@link MediaHttpUploader} must be instantiated
   * before upload called be called again.
   * </p>
   *
   * <p>
   * If an error is encountered during the request execution the caller is responsible for parsing
   * the response correctly. For example for JSON errors:
   * </p>
   *
   * <pre>
    if (!response.isSuccessStatusCode()) {
      throw GoogleJsonResponseException.from(jsonFactory, response);
    }
   * </pre>
   *
   * <p>
   * Callers should call {@link HttpResponse#disconnect} when the returned HTTP response object is
   * no longer needed. However, {@link HttpResponse#disconnect} does not have to be called if the
   * response stream is properly closed. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = batch.upload(initiationRequestUrl);
     try {
       // process the HTTP response object
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   * @return HTTP response
   */
  public HttpResponse upload(GenericUrl initiationRequestUrl) throws IOException {
    Preconditions.checkArgument(uploadState == UploadState.NOT_STARTED);

    if (directUploadEnabled) {
      return directUpload(initiationRequestUrl);
    }
    return resumableUpload(initiationRequestUrl);
  }

  /**
   * Direct Uploads the media.
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   * @return HTTP response
   */
  private HttpResponse directUpload(GenericUrl initiationRequestUrl) throws IOException {
    updateStateAndNotifyListener(UploadState.MEDIA_IN_PROGRESS);

    HttpContent content = mediaContent;
    if (metadata != null) {
      content = new MultipartContent().setContentParts(Arrays.asList(metadata, mediaContent));
      initiationRequestUrl.put("uploadType", "multipart");
    } else {
      initiationRequestUrl.put("uploadType", "media");
    }
    HttpRequest request =
        requestFactory.buildRequest(initiationRequestMethod, initiationRequestUrl, content);
    request.getHeaders().putAll(initiationHeaders);
    // We do not have to do anything special here if media content length is unspecified because
    // direct media upload works even when the media content length == -1.
    HttpResponse response = executeCurrentRequest(request);
    boolean responseProcessed = false;
    try {
      if (isMediaLengthKnown()) {
        totalBytesServerReceived = getMediaContentLength();
      }
      updateStateAndNotifyListener(UploadState.MEDIA_COMPLETE);
      responseProcessed = true;
    } finally {
      if (!responseProcessed) {
        response.disconnect();
      }
    }
    return response;
  }

  /**
   * Uploads the media in a resumable manner.
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   * @return HTTP response
   */
  private HttpResponse resumableUpload(GenericUrl initiationRequestUrl) throws IOException {
    // Make initial request to get the unique upload URL.
    HttpResponse initialResponse = executeUploadInitiation(initiationRequestUrl);
    if (!initialResponse.isSuccessStatusCode()) {
      // If the initiation request is not successful return it immediately.
      return initialResponse;
    }
    GenericUrl uploadUrl;
    try {
      uploadUrl = new GenericUrl(initialResponse.getHeaders().getLocation());
    } finally {
      initialResponse.disconnect();
    }

    // Convert media content into a byte stream to upload in chunks.
    contentInputStream = mediaContent.getInputStream();
    if (!contentInputStream.markSupported() && isMediaLengthKnown()) {
      // If we know the media content length then wrap the stream into a Buffered input stream to
      // support the {@link InputStream#mark} and {@link InputStream#reset} methods required for
      // handling server errors.
      contentInputStream = new BufferedInputStream(contentInputStream);
    }

    HttpResponse response;
    // Upload the media content in chunks.
    while (true) {
      ContentChunk contentChunk = buildContentChunk();
      currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
      currentRequest.setContent(contentChunk.getContent());
      currentRequest.getHeaders().setContentRange(contentChunk.getContentRange());

      // set mediaErrorHandler as I/O exception handler and as unsuccessful response handler for
      // calling to serverErrorCallback on an I/O exception or an abnormal HTTP response
      new MediaUploadErrorHandler(this, currentRequest);

      if (isMediaLengthKnown()) {
        // TODO(rmistry): Support gzipping content for the case where media content length is
        // known (https://github.com/googleapis/google-api-java-client/issues/691).
        response = executeCurrentRequestWithoutGZip(currentRequest);
      } else {
        response = executeCurrentRequest(currentRequest);
      }
      boolean returningResponse = false;
      try {
        if (response.isSuccessStatusCode()) {
          totalBytesServerReceived = getMediaContentLength();
          if (mediaContent.getCloseInputStream()) {
            contentInputStream.close();
          }
          updateStateAndNotifyListener(UploadState.MEDIA_COMPLETE);
          returningResponse = true;
          return response;
        }

        if (response.getStatusCode() != 308) {
          returningResponse = true;
          return response;
        }

        // Check to see if the upload URL has changed on the server.
        String updatedUploadUrl = response.getHeaders().getLocation();
        if (updatedUploadUrl != null) {
          uploadUrl = new GenericUrl(updatedUploadUrl);
        }

        // we check the amount of bytes the server received so far, because the server may process
        // fewer bytes than the amount of bytes the client had sent
        long newBytesServerReceived = getNextByteIndex(response.getHeaders().getRange());
        // the server can receive any amount of bytes from 0 to current chunk length
        long currentBytesServerReceived = newBytesServerReceived - totalBytesServerReceived;
        Preconditions.checkState(
            currentBytesServerReceived >= 0 && currentBytesServerReceived <= currentChunkLength);
        long copyBytes = currentChunkLength - currentBytesServerReceived;
        if (isMediaLengthKnown()) {
          if (copyBytes > 0) {
            // If the server didn't receive all the bytes the client sent the current position of
            // the input stream is incorrect. So we should reset the stream and skip those bytes
            // that the server had already received.
            // Otherwise (the server got all bytes the client sent), the stream is in its right
            // position, and we can continue from there
            contentInputStream.reset();
            long actualSkipValue = contentInputStream.skip(currentBytesServerReceived);
            Preconditions.checkState(currentBytesServerReceived == actualSkipValue);
          }
        } else if (copyBytes == 0) {
          // server got all the bytes, so we don't need to use this buffer. Otherwise, we have to
          // keep the buffer and copy part (or all) of its bytes to the stream we are sending to the
          // server
          currentRequestContentBuffer = null;
        }
        totalBytesServerReceived = newBytesServerReceived;

        updateStateAndNotifyListener(UploadState.MEDIA_IN_PROGRESS);
      } finally {
        if (!returningResponse) {
          response.disconnect();
        }
      }
    }
  }

  /**
   * @return {@code true} if the media length is known, otherwise {@code false}
   */
  private boolean isMediaLengthKnown() throws IOException {
    return getMediaContentLength() >= 0;
  }

  /**
   * Uses lazy initialization to compute the media content length.
   *
   * <p>
   * This is done to avoid throwing an {@link IOException} in the constructor.
   * </p>
   */
  private long getMediaContentLength() throws IOException {
    if (!isMediaContentLengthCalculated) {
      mediaContentLength = mediaContent.getLength();
      isMediaContentLengthCalculated = true;
    }
    return mediaContentLength;
  }

  /**
   * This method sends a POST request with empty content to get the unique upload URL.
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   */
  private HttpResponse executeUploadInitiation(GenericUrl initiationRequestUrl) throws IOException {
    updateStateAndNotifyListener(UploadState.INITIATION_STARTED);

    initiationRequestUrl.put("uploadType", "resumable");
    HttpContent content = metadata == null ? new EmptyContent() : metadata;
    HttpRequest request =
        requestFactory.buildRequest(initiationRequestMethod, initiationRequestUrl, content);
    initiationHeaders.set(CONTENT_TYPE_HEADER, mediaContent.getType());
    if (isMediaLengthKnown()) {
      initiationHeaders.set(CONTENT_LENGTH_HEADER, getMediaContentLength());
    }
    request.getHeaders().putAll(initiationHeaders);
    HttpResponse response = executeCurrentRequest(request);
    boolean notificationCompleted = false;

    try {
      updateStateAndNotifyListener(UploadState.INITIATION_COMPLETE);
      notificationCompleted = true;
    } finally {
      if (!notificationCompleted) {
        response.disconnect();
      }
    }
    return response;
  }

  /**
   * Executes the current request with some minimal common code.
   *
   * @param request current request
   * @return HTTP response
   */
  private HttpResponse executeCurrentRequestWithoutGZip(HttpRequest request) throws IOException {
    // method override for non-POST verbs
    new MethodOverride().intercept(request);
    // don't throw an exception so we can let a custom Google exception be thrown
    request.setThrowExceptionOnExecuteError(false);
    // execute the request
    HttpResponse response = request.execute();
    return response;
  }

  /**
   * Executes the current request with some common code that includes exponential backoff and GZip
   * encoding.
   *
   * @param request current request
   * @return HTTP response
   */
  private HttpResponse executeCurrentRequest(HttpRequest request) throws IOException {
    // enable GZip encoding if necessary
    if (!disableGZipContent && !(request.getContent() instanceof EmptyContent)) {
      request.setEncoding(new GZipEncoding());
    }
    // execute request
    HttpResponse response = executeCurrentRequestWithoutGZip(request);
    return response;
  }

  /**
   * Sets the HTTP media content chunk and the required headers that should be used in the upload
   * request.
   */
  private ContentChunk buildContentChunk() throws IOException {
    int blockSize;
    if (isMediaLengthKnown()) {
      // We know exactly what the blockSize will be because we know the media content length.
      blockSize = (int) Math.min(chunkSize, getMediaContentLength() - totalBytesServerReceived);
    } else {
      // Use the chunkSize as the blockSize because we do know what what it is yet.
      blockSize = chunkSize;
    }

    AbstractInputStreamContent contentChunk;
    int actualBlockSize = blockSize;
    if (isMediaLengthKnown()) {
      // Mark the current position in case we need to retry the request.
      contentInputStream.mark(blockSize);

      InputStream limitInputStream = ByteStreams.limit(contentInputStream, blockSize);
      contentChunk = new InputStreamContent(
          mediaContent.getType(), limitInputStream).setRetrySupported(true)
          .setLength(blockSize).setCloseInputStream(false);
      mediaContentLengthStr = String.valueOf(getMediaContentLength());
    } else {
      // If the media content length is not known we implement a custom buffered input stream that
      // enables us to detect the length of the media content when the last chunk is sent. We
      // accomplish this by always trying to read an extra byte further than the end of the current
      // chunk.
      int actualBytesRead;
      int bytesAllowedToRead;

      // amount of bytes which need to be copied from last chunk buffer
      int copyBytes = 0;
      if (currentRequestContentBuffer == null) {
        bytesAllowedToRead = cachedByte == null ? blockSize + 1 : blockSize;
        currentRequestContentBuffer = new byte[blockSize + 1];
        if (cachedByte != null) {
          currentRequestContentBuffer[0] = cachedByte;
        }
      } else {
        // currentRequestContentBuffer is not null that means one of the following:
        // 1. This is a request to recover from a server error (e.g. 503)
        // or
        // 2. The server received less bytes than the amount of bytes the client had sent. For
        // example, the client sends bytes 100-199, but the server returns back status code 308,
        // and its "Range" header is "bytes=0-150".
        // In that case, the new request will be constructed from the previous request's byte buffer
        // plus new bytes from the stream.
        copyBytes = (int) (totalBytesClientSent - totalBytesServerReceived);
        // shift copyBytes bytes to the beginning - those are the bytes which weren't received by
        // the server in the last chunk.
        System.arraycopy(currentRequestContentBuffer, currentChunkLength - copyBytes,
            currentRequestContentBuffer, 0, copyBytes);
        if (cachedByte != null) {
          // add the last cached byte to the buffer
          currentRequestContentBuffer[copyBytes] = cachedByte;
        }

        bytesAllowedToRead = blockSize - copyBytes;
      }

      actualBytesRead = ByteStreams.read(
          contentInputStream, currentRequestContentBuffer, blockSize + 1 - bytesAllowedToRead,
          bytesAllowedToRead);

      if (actualBytesRead < bytesAllowedToRead) {
        actualBlockSize = copyBytes + Math.max(0, actualBytesRead);
        if (cachedByte != null) {
          actualBlockSize++;
          cachedByte = null;
        }

        if (mediaContentLengthStr.equals("*")) {
          // At this point we know we reached the media content length because we either read less
          // than the specified chunk size or there is no more data left to be read.
          mediaContentLengthStr = String.valueOf(totalBytesServerReceived + actualBlockSize);
        }
      } else {
        cachedByte = currentRequestContentBuffer[blockSize];
      }

      contentChunk = new ByteArrayContent(
          mediaContent.getType(), currentRequestContentBuffer, 0, actualBlockSize);
      totalBytesClientSent = totalBytesServerReceived + actualBlockSize;
    }

    currentChunkLength = actualBlockSize;

    String contentRange;
    if (actualBlockSize == 0) {
      // No bytes to upload. Either zero content media being uploaded, or a server failure on the
      // last write, even though the write actually succeeded. Either way,
      // mediaContentLengthStr will contain the actual media length.
      contentRange = "bytes */" + mediaContentLengthStr;
    } else {
      contentRange = "bytes " + totalBytesServerReceived + "-"
              + (totalBytesServerReceived + actualBlockSize - 1) + "/" + mediaContentLengthStr;
    }
    return new ContentChunk(contentChunk, contentRange);
  }

  private static class ContentChunk {
    private final AbstractInputStreamContent content;
    private final String contentRange;

    ContentChunk(AbstractInputStreamContent content, String contentRange) {
      this.content = content;
      this.contentRange = contentRange;
    }

    AbstractInputStreamContent getContent() {
      return content;
    }

    String getContentRange() {
      return contentRange;
    }
  }

  /**
   * {@link Beta} <br/>
   * The call back method that will be invoked on a server error or an I/O exception during
   * resumable upload inside {@link #upload}.
   *
   * <p>
   * This method changes the current request to query the current status of the upload to find how
   * many bytes were successfully uploaded before the server error occurred.
   * </p>
   */
  @Beta
  void serverErrorCallback() throws IOException {
    Preconditions.checkNotNull(currentRequest, "The current request should not be null");

    // Query the current status of the upload by issuing an empty PUT request on the upload URI.
    currentRequest.setContent(new EmptyContent());
    currentRequest.getHeaders().setContentRange("bytes */" + mediaContentLengthStr);
  }

  /**
   * Returns the next byte index identifying data that the server has not yet received, obtained
   * from the HTTP Range header (E.g a header of "Range: 0-55" would cause 56 to be returned).
   * <code>null</code> or malformed headers cause 0 to be returned.
   *
   * @param rangeHeader in the HTTP response
   * @return the byte index beginning where the server has yet to receive data
   */
  private long getNextByteIndex(String rangeHeader) {
    if (rangeHeader == null) {
      return 0L;
    }
    return Long.parseLong(rangeHeader.substring(rangeHeader.indexOf('-') + 1)) + 1;
  }

  /** Returns HTTP content metadata for the media request or {@code null} for none. */
  public HttpContent getMetadata() {
    return metadata;
  }

  /** Sets HTTP content metadata for the media request or {@code null} for none. */
  public MediaHttpUploader setMetadata(HttpContent metadata) {
    this.metadata = metadata;
    return this;
  }

  /** Returns the HTTP content of the media to be uploaded. */
  public HttpContent getMediaContent() {
    return mediaContent;
  }

  /** Returns the transport to use for requests. */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Sets whether direct media upload is enabled or disabled.
   *
   * <p>
   * If value is set to {@code true} then a direct upload will be done where the whole media content
   * is uploaded in a single request. If value is set to {@code false} then the upload uses the
   * resumable media upload protocol to upload in data chunks.
   * </p>
   *
   * <p>
   * Direct upload is recommended if the content size falls below a certain minimum limit. This is
   * because there's minimum block write size for some Google APIs, so if the resumable request
   * fails in the space of that first block, the client will have to restart from the beginning
   * anyway.
   * </p>
   *
   * <p>
   * Defaults to {@code false}.
   * </p>
   *
   * @since 1.9
   */
  public MediaHttpUploader setDirectUploadEnabled(boolean directUploadEnabled) {
    this.directUploadEnabled = directUploadEnabled;
    return this;
  }

  /**
   * Returns whether direct media upload is enabled or disabled. If value is set to {@code true}
   * then a direct upload will be done where the whole media content is uploaded in a single
   * request. If value is set to {@code false} then the upload uses the resumable media upload
   * protocol to upload in data chunks. Defaults to {@code false}.
   *
   * @since 1.9
   */
  public boolean isDirectUploadEnabled() {
    return directUploadEnabled;
  }

  /**
   * Sets the progress listener to send progress notifications to or {@code null} for none.
   */
  public MediaHttpUploader setProgressListener(MediaHttpUploaderProgressListener progressListener) {
    this.progressListener = progressListener;
    return this;
  }

  /**
   * Returns the progress listener to send progress notifications to or {@code null} for none.
   */
  public MediaHttpUploaderProgressListener getProgressListener() {
    return progressListener;
  }

  /**
   * Sets the maximum size of individual chunks that will get uploaded by single HTTP requests. The
   * default value is {@link #DEFAULT_CHUNK_SIZE}.
   *
   * <p>
   * The minimum allowable value is {@link #MINIMUM_CHUNK_SIZE} and the specified chunk size must be
   * a multiple of {@link #MINIMUM_CHUNK_SIZE}.
   * </p>
   */
  public MediaHttpUploader setChunkSize(int chunkSize) {
    Preconditions.checkArgument(chunkSize > 0 && chunkSize % MINIMUM_CHUNK_SIZE == 0, "chunkSize"
        + " must be a positive multiple of " + MINIMUM_CHUNK_SIZE + ".");
    this.chunkSize = chunkSize;
    return this;
  }

  /**
   * Returns the maximum size of individual chunks that will get uploaded by single HTTP requests.
   * The default value is {@link #DEFAULT_CHUNK_SIZE}.
   */
  public int getChunkSize() {
    return chunkSize;
  }

  /**
   * Returns whether to disable GZip compression of HTTP content.
   *
   * @since 1.13
   */
  public boolean getDisableGZipContent() {
    return disableGZipContent;
  }

  /**
   * Sets whether to disable GZip compression of HTTP content.
   *
   * <p>
   * By default it is {@code false}.
   * </p>
   *
   * <p>
   * If {@link #setDisableGZipContent(boolean)} is set to false (the default value) then content is
   * gzipped for direct media upload and resumable media uploads when content length is not known.
   * Due to a current limitation, content is not gzipped for resumable media uploads when content
   * length is known; this limitation will be removed in the future.
   * </p>
   *
   * @since 1.13
   */
  public MediaHttpUploader setDisableGZipContent(boolean disableGZipContent) {
    this.disableGZipContent = disableGZipContent;
    return this;
  }

  /**
   * Returns the sleeper.
   *
   * @since 1.15
   */
  public Sleeper getSleeper() {
    return sleeper;
  }

  /**
   * Sets the sleeper. The default value is {@link Sleeper#DEFAULT}.
   *
   * @since 1.15
   */
  public MediaHttpUploader setSleeper(Sleeper sleeper) {
    this.sleeper = sleeper;
    return this;
  }

  /**
   * Returns the HTTP method used for the initiation request.
   *
   * <p>
   * The default value is {@link HttpMethods#POST}.
   * </p>
   *
   * @since 1.12
   */
  public String getInitiationRequestMethod() {
    return initiationRequestMethod;
  }

  /**
   * Sets the HTTP method used for the initiation request.
   *
   * <p>
   * Can only be {@link HttpMethods#POST} (for media upload) or {@link HttpMethods#PUT} (for media
   * update). The default value is {@link HttpMethods#POST}.
   * </p>
   *
   * @since 1.12
   */
  public MediaHttpUploader setInitiationRequestMethod(String initiationRequestMethod) {
    Preconditions.checkArgument(initiationRequestMethod.equals(HttpMethods.POST)
        || initiationRequestMethod.equals(HttpMethods.PUT)
        || initiationRequestMethod.equals(HttpMethods.PATCH));
    this.initiationRequestMethod = initiationRequestMethod;
    return this;
  }

  /** Sets the HTTP headers used for the initiation request. */
  public MediaHttpUploader setInitiationHeaders(HttpHeaders initiationHeaders) {
    this.initiationHeaders = initiationHeaders;
    return this;
  }

  /** Returns the HTTP headers used for the initiation request. */
  public HttpHeaders getInitiationHeaders() {
    return initiationHeaders;
  }

  /**
   * Gets the total number of bytes the server received so far or {@code 0} for direct uploads when
   * the content length is not known.
   *
   * @return the number of bytes the server received so far
   */
  public long getNumBytesUploaded() {
    return totalBytesServerReceived;
  }

  /**
   * Sets the upload state and notifies the progress listener.
   *
   * @param uploadState value to set to
   */
  private void updateStateAndNotifyListener(UploadState uploadState) throws IOException {
    this.uploadState = uploadState;
    if (progressListener != null) {
      progressListener.progressChanged(this);
    }
  }

  /**
   * Gets the current upload state of the uploader.
   *
   * @return the upload state
   */
  public UploadState getUploadState() {
    return uploadState;
  }

  /**
   * Gets the upload progress denoting the percentage of bytes that have been uploaded, represented
   * between 0.0 (0%) and 1.0 (100%).
   *
   * <p>
   * Do not use if the specified {@link AbstractInputStreamContent} has no content length specified.
   * Instead, consider using {@link #getNumBytesUploaded} to denote progress.
   * </p>
   *
   * @throws IllegalArgumentException if the specified {@link AbstractInputStreamContent} has no
   *         content length
   * @return the upload progress
   */
  public double getProgress() throws IOException {
    Preconditions.checkArgument(isMediaLengthKnown(), "Cannot call getProgress() if "
        + "the specified AbstractInputStreamContent has no content length. Use "
        + " getNumBytesUploaded() to denote progress instead.");
    return getMediaContentLength() == 0 ? 0 : (double) totalBytesServerReceived
        / getMediaContentLength();
  }
}
