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

import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GZipEncoding;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.MultipartRelatedContent;
import com.google.common.base.Preconditions;
import com.google.common.io.LimitInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
 * See {@link #setDisableGZipContent(boolean)} for information on when content is gzipped
 * and how to control that behavior.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 *
 * @author rmistry@google.com (Ravi Mistry)
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
   * Determines whether the back off policy is enabled or disabled. If value is set to {@code false}
   * then server errors are not handled and the upload process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  private boolean backOffPolicyEnabled = true;

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
   * The total number of bytes uploaded by this uploader. This value will not be calculated for
   * direct uploads when the content length is not known in advance.
   */
  // TODO(rmistry): Figure out a way to compute the content length using CountingInputStream.
  private long bytesUploaded;

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
   * The content buffer of the current request or {@code null} for none. It is used for resumable
   * media upload when the media content length is not specified. It is instantiated for every
   * request in {@link #setContentAndHeadersOnCurrentRequest} and is set to {@code null} when the
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

  /**
   * Construct the {@link MediaHttpUploader}.
   *
   * <p>
   * The input stream received by calling {@link AbstractInputStreamContent#getInputStream} is
   * closed when the upload process is successfully completed. For resumable uploads, when the
   * media content length is known, if the input stream has {@link InputStream#markSupported} as
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
   * listed <a href='http://code.google.com/apis/gdata/docs/resumable_upload.html'>here.</a>
   *
   * <p>
   * This method is not reentrant. A new instance of {@link MediaHttpUploader} must be instantiated
   * before upload called be called again.
   * </p>
   *
   * <p>
   * If an error is encountered during the request execution the caller is responsible for parsing
   * the response correctly. For example for JSON errors:
   *
   * <pre>
    if (!response.isSuccessStatusCode()) {
      throw GoogleJsonResponseException.from(jsonFactory, response);
    }
   * </pre>
   * </p>
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
      updateStateAndNotifyListener(UploadState.MEDIA_IN_PROGRESS);

      HttpContent content = mediaContent;
      if (metadata != null) {
        content = new MultipartRelatedContent(metadata, mediaContent);
        initiationRequestUrl.put("uploadType", "multipart");
      } else {
        initiationRequestUrl.put("uploadType", "media");
      }
      HttpRequest request =
          requestFactory.buildRequest(initiationRequestMethod, initiationRequestUrl, content);
      request.getHeaders().putAll(initiationHeaders);
      request.setThrowExceptionOnExecuteError(false);
      if (!disableGZipContent) {
        request.setEncoding(new GZipEncoding());
      }
      addMethodOverride(request);

      // We do not have to do anything special here if media content length is unspecified because
      // direct media upload works even when the media content length == -1.
      HttpResponse response = request.execute();
      boolean responseProcessed = false;
      try {
        if (getMediaContentLength() >= 0) {
          bytesUploaded = getMediaContentLength();
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
    if (!contentInputStream.markSupported() && getMediaContentLength() >= 0) {
      // If we know the media content length then wrap the stream into a Buffered input stream to
      // support the {@link InputStream#mark} and {@link InputStream#reset} methods required for
      // handling server errors.
      contentInputStream = new BufferedInputStream(contentInputStream);
    }

    HttpResponse response;
    // Upload the media content in chunks.
    while (true) {
      currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
      addMethodOverride(currentRequest); // needed for PUT
      setContentAndHeadersOnCurrentRequest(bytesUploaded);
      if (backOffPolicyEnabled) {
        // Set MediaExponentialBackOffPolicy as the BackOffPolicy of the HTTP Request which will
        // callback to this instance if there is a server error.
        currentRequest.setBackOffPolicy(new MediaUploadExponentialBackOffPolicy(this));
      }
      currentRequest.setThrowExceptionOnExecuteError(false);
      currentRequest.setRetryOnExecuteIOException(true);
      if (getMediaContentLength() >= 0) {
        // TODO(rmistry): Support gzipping content for the case where media content length is
        // known (https://code.google.com/p/google-api-java-client/issues/detail?id=691).
      } else if (!disableGZipContent) {
        currentRequest.setEncoding(new GZipEncoding());
      }
      response = currentRequest.execute();
      boolean returningResponse = false;
      try {
        if (response.isSuccessStatusCode()) {
          bytesUploaded = getMediaContentLength();
          contentInputStream.close();
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
        bytesUploaded = getNextByteIndex(response.getHeaders().getRange());
        currentRequestContentBuffer = null;
        updateStateAndNotifyListener(UploadState.MEDIA_IN_PROGRESS);
      } finally {
        if (!returningResponse) {
          response.disconnect();
        }
      }
    }
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
    addMethodOverride(request);
    initiationHeaders.set(CONTENT_TYPE_HEADER, mediaContent.getType());
    if (getMediaContentLength() >= 0) {
      initiationHeaders.set(CONTENT_LENGTH_HEADER, getMediaContentLength());
    }
    request.getHeaders().putAll(initiationHeaders);
    request.setThrowExceptionOnExecuteError(false);
    request.setRetryOnExecuteIOException(true);
    if (!disableGZipContent) {
      request.setEncoding(new GZipEncoding());
    }
    HttpResponse response = request.execute();
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
   * Wraps PUT HTTP requests inside of a POST request and uses {@link MethodOverride#HEADER} header
   * to specify the actual HTTP method. This is done in case the HTTP transport does not support
   * PUT.
   *
   * @param request HTTP request
   */
  private void addMethodOverride(HttpRequest request) throws IOException {
    new MethodOverride().intercept(request);
  }

  /**
   * Sets the HTTP media content chunk and the required headers that should be used in the upload
   * request.
   *
   * @param bytesWritten The number of bytes that have been successfully uploaded on the server
   */
  private void setContentAndHeadersOnCurrentRequest(long bytesWritten) throws IOException {
    int blockSize;
    if (getMediaContentLength() >= 0) {
      // We know exactly what the blockSize will be because we know the media content length.
      blockSize = (int) Math.min(chunkSize, getMediaContentLength() - bytesWritten);
    } else {
      // Use the chunkSize as the blockSize because we do know what what it is yet.
      blockSize = chunkSize;
    }

    AbstractInputStreamContent contentChunk;
    int actualBlockSize = blockSize;
    String mediaContentLengthStr;
    if (getMediaContentLength() >= 0) {
      // Mark the current position in case we need to retry the request.
      contentInputStream.mark(blockSize);

      // TODO(rmistry): Add tests for LimitInputStream.
      InputStream limitInputStream = new LimitInputStream(contentInputStream, blockSize);
      contentChunk = new InputStreamContent(mediaContent.getType(), limitInputStream)
          .setRetrySupported(true)
          .setLength(blockSize)
          .setCloseInputStream(false);
      mediaContentLengthStr = String.valueOf(getMediaContentLength());
    } else {
      // If the media content length is not known we implement a custom buffered input stream that
      // enables us to detect the length of the media content when the last chunk is sent. We
      // accomplish this by always trying to read an extra byte further than the end of the current
      // chunk.
      int actualBytesRead;
      int bytesAllowedToRead;
      int contentBufferStartIndex = 0;
      if (currentRequestContentBuffer == null) {
        bytesAllowedToRead = cachedByte == null ? blockSize + 1 : blockSize;
        InputStream limitInputStream = new LimitInputStream(contentInputStream, bytesAllowedToRead);

        currentRequestContentBuffer = new byte[blockSize + 1];
        if (cachedByte != null) {
          currentRequestContentBuffer[0] = cachedByte;
        }

        actualBytesRead = limitInputStream.read(currentRequestContentBuffer, blockSize + 1 -
            bytesAllowedToRead, bytesAllowedToRead);
      } else {
        // currentRequestContentBuffer is not null that means this is a request to recover from a
        // server error. The new request will be constructed from the previous request's byte
        // buffer.
        bytesAllowedToRead = (int) (chunkSize - (bytesWritten - bytesUploaded) + 1);
        contentBufferStartIndex = (int) (bytesWritten - bytesUploaded);
        actualBytesRead = bytesAllowedToRead;
      }

      if (actualBytesRead < bytesAllowedToRead) {
        actualBlockSize = Math.max(0, actualBytesRead);
        if (cachedByte != null) {
          actualBlockSize++;
        }
        // At this point we know we reached the media content length because we either read less
        // than the specified chunk size or there is no more data left to be read.
        mediaContentLengthStr = String.valueOf(bytesWritten + actualBlockSize);
      } else {
        cachedByte = currentRequestContentBuffer[blockSize];
        mediaContentLengthStr = "*";
      }

      contentChunk = new ByteArrayContent(
          mediaContent.getType(), currentRequestContentBuffer, contentBufferStartIndex,
          actualBlockSize);
    }

    currentRequest.setContent(contentChunk);

    currentRequest.getHeaders().setContentRange("bytes " + bytesWritten + "-" +
        (bytesWritten + actualBlockSize - 1) + "/" + mediaContentLengthStr);
  }

  /**
   * The call back method that will be invoked by
   * {@link MediaUploadExponentialBackOffPolicy#getNextBackOffMillis} if it encounters a server
   * error. This method should only be used as a call back method after {@link #upload} is invoked.
   *
   * <p>
   * This method will query the current status of the upload to find how many bytes were
   * successfully uploaded before the server error occurred. It will then adjust the HTTP Request
   * object used by the BackOffPolicy to contain the correct range header and media content chunk.
   * </p>
   */
  public void serverErrorCallback() throws IOException {
    Preconditions.checkNotNull(currentRequest, "The current request should not be null");

    // TODO(rmistry): Handle timeouts here similar to how server errors are handled.
    // Query the current status of the upload by issuing an empty POST request on the upload URI.
    HttpRequest request = requestFactory.buildPutRequest(currentRequest.getUrl(), null);
    addMethodOverride(request); // needed for PUT

    request.getHeaders().setContentRange("bytes */" + (
        getMediaContentLength() >= 0 ? getMediaContentLength() : "*"));
    request.setContent(new EmptyContent());
    request.setThrowExceptionOnExecuteError(false);
    request.setRetryOnExecuteIOException(true);
    HttpResponse response = request.execute();

    try {
      long bytesWritten = getNextByteIndex(response.getHeaders().getRange());

      // Check to see if the upload URL has changed on the server.
      String updatedUploadUrl = response.getHeaders().getLocation();
      if (updatedUploadUrl != null) {
        currentRequest.setUrl(new GenericUrl(updatedUploadUrl));
      }

      if (getMediaContentLength() >= 0) {
        // The current position of the input stream is likely incorrect because the upload was
        // interrupted. Reset the position and skip ahead to the correct spot.
        // We do not need to do this when the media content length is unknown because we store the
        // last chunk in a byte buffer.
        contentInputStream.reset();
        long skipValue = bytesUploaded - bytesWritten;
        long actualSkipValue = contentInputStream.skip(skipValue);
        Preconditions.checkState(skipValue == actualSkipValue);
      }

      // Adjust the HTTP request that encountered the server error with the correct range header
      // and media content chunk.
      setContentAndHeadersOnCurrentRequest(bytesWritten);
    } finally {
      response.disconnect();
    }
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
   * Sets whether the back off policy is enabled or disabled. If value is set to {@code false} then
   * server errors are not handled and the upload process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  public MediaHttpUploader setBackOffPolicyEnabled(boolean backOffPolicyEnabled) {
    this.backOffPolicyEnabled = backOffPolicyEnabled;
    return this;
  }

  /**
   * Returns whether the back off policy is enabled or disabled. If value is set to {@code false}
   * then server errors are not handled and the upload process will fail if a server error is
   * encountered. Defaults to {@code true}.
   */
  public boolean isBackOffPolicyEnabled() {
    return backOffPolicyEnabled;
  }

  /**
   * Sets whether direct media upload is enabled or disabled. If value is set to {@code true} then a
   * direct upload will be done where the whole media content is uploaded in a single request. If
   * value is set to {@code false} then the upload uses the resumable media upload protocol to
   * upload in data chunks. Defaults to {@code false}.
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
   * The minimum allowable value is {@link #MINIMUM_CHUNK_SIZE} and the specified chunk size must
   * be a multiple of {@link #MINIMUM_CHUNK_SIZE}.
   * </p>
   *
   * <p>
   * Upgrade warning: Prior to version 1.13.0-beta {@link #setChunkSize} accepted any chunk size
   * above {@link #MINIMUM_CHUNK_SIZE}, it now accepts only multiples of
   * {@link #MINIMUM_CHUNK_SIZE}.
   * </p>
   */
  public MediaHttpUploader setChunkSize(int chunkSize) {
    Preconditions.checkArgument(chunkSize > 0 && chunkSize % MINIMUM_CHUNK_SIZE == 0);
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
        || initiationRequestMethod.equals(HttpMethods.PUT));
    this.initiationRequestMethod = initiationRequestMethod;
    return this;
  }

  /**
   * Sets the HTTP headers used for the initiation request.
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the initiation headers were of type
   * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
   * {@link HttpHeaders}.
   * </p>
   */
  public MediaHttpUploader setInitiationHeaders(HttpHeaders initiationHeaders) {
    this.initiationHeaders = initiationHeaders;
    return this;
  }

  /**
   * Returns the HTTP headers used for the initiation request.
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the initiation headers were of type
   * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
   * {@link HttpHeaders}.
   * </p>
   */
  public HttpHeaders getInitiationHeaders() {
    return initiationHeaders;
  }

  /**
   * Gets the total number of bytes uploaded by this uploader or {@code 0} for direct uploads when
   * the content length is not known.
   *
   * @return the number of bytes uploaded
   */
  public long getNumBytesUploaded() {
    return bytesUploaded;
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
    Preconditions.checkArgument(getMediaContentLength() >= 0, "Cannot call getProgress() if " +
        "the specified AbstractInputStreamContent has no content length. Use " +
        " getNumBytesUploaded() to denote progress instead.");
    return getMediaContentLength() == 0 ? 0 : (double) bytesUploaded / getMediaContentLength();
  }
}
