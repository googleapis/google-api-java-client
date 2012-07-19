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

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethod;
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
import java.io.InputStream;

/**
 * Media HTTP Uploader, with support for both direct and resumable media uploads. Documentation is
 * available <a href='http://code.google.com/p/google-api-java-client/wiki/MediaUpload'>here</a>.
 *
 * <p>
 * If the provided {@link InputStream} has {@link InputStream#markSupported} as {@code false} then
 * it is wrapped in an {@link BufferedInputStream} to support the {@link InputStream#mark} and
 * {@link InputStream#reset} methods required for handling server errors.
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
public final class MediaHttpUploader {

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
   * The length of the HTTP media content or {@code 0} before it is lazily initialized in
   * {@link #getMediaContentLength()}.
   */
  private long mediaContentLength;

  /**
   * The HTTP method used for the initiation request. Can only be {@link HttpMethod#POST} (for media
   * upload) or {@link HttpMethod#PUT} (for media update). The default value is
   * {@link HttpMethod#POST}.
   */
  private HttpMethod initiationMethod = HttpMethod.POST;

  /** The HTTP headers used in the initiation request. */
  private GoogleHeaders initiationHeaders = new GoogleHeaders();

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

  /** The total number of bytes uploaded by this uploader. */
  private long bytesUploaded;

  /**
   * Maximum size of individual chunks that will get uploaded by single HTTP requests. The default
   * value is {@link #DEFAULT_CHUNK_SIZE}.
   */
  private int chunkSize = DEFAULT_CHUNK_SIZE;

  /**
   * Construct the {@link MediaHttpUploader}.
   *
   * @param mediaContent The Input stream content of the media to be uploaded. The input stream
   *        received by calling {@link AbstractInputStreamContent#getInputStream} is closed when the
   *        upload process is successfully completed. If the input stream has
   *        {@link InputStream#markSupported} as {@code false} then it is wrapped in an
   *        {@link BufferedInputStream} to support the {@link InputStream#mark} and
   *        {@link InputStream#reset} methods required for handling server errors.
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
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   * @return HTTP response
   */
  public HttpResponse upload(GenericUrl initiationRequestUrl) throws Exception {
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
          requestFactory.buildRequest(initiationMethod, initiationRequestUrl, content);
      request.setEnableGZipContent(true);
      addMethodOverride(request);
      HttpResponse response = request.execute();
      boolean responseProcessed = false;
      try {
        bytesUploaded = getMediaContentLength();
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
    GenericUrl uploadUrl;
    try {
      uploadUrl = new GenericUrl(initialResponse.getHeaders().getLocation());
    } finally {
      initialResponse.disconnect();
    }

    // Convert media content into a byte stream to upload in chunks.
    contentInputStream = mediaContent.getInputStream();
    if (!contentInputStream.markSupported()) {
      contentInputStream = new BufferedInputStream(contentInputStream);
    }

    HttpResponse response;
    // Upload the media content in chunks.
    while (true) {
      currentRequest = requestFactory.buildPutRequest(uploadUrl, null);
      new MethodOverride().intercept(currentRequest); // needed for PUT
      currentRequest.setAllowEmptyContent(false);
      setContentAndHeadersOnCurrentRequest(bytesUploaded);
      if (backOffPolicyEnabled) {
        // Set MediaExponentialBackOffPolicy as the BackOffPolicy of the HTTP Request which will
        // callback to this instance if there is a server error.
        currentRequest.setBackOffPolicy(new MediaUploadExponentialBackOffPolicy(this));
      }
      currentRequest.setThrowExceptionOnExecuteError(false);
      currentRequest.setRetryOnExecuteIOException(true);
      response = currentRequest.execute();
      boolean returningResponse = false;
      try {
        if (response.isSuccessStatusCode()) {
          bytesUploaded = mediaContentLength;
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
        updateStateAndNotifyListener(UploadState.MEDIA_IN_PROGRESS);
      } finally {
        if (!returningResponse) {
          response.disconnect();
        }
      }
    }
  }

  /** Uses lazy initialization to compute the media content length. */
  private long getMediaContentLength() throws Exception {
    if (mediaContentLength == 0) {
      mediaContentLength = mediaContent.getLength();
      Preconditions.checkArgument(mediaContentLength != -1);
    }
    return mediaContentLength;
  }

  /**
   * This method sends a POST request with empty content to get the unique upload URL.
   *
   * @param initiationRequestUrl The request URL where the initiation request will be sent
   */
  private HttpResponse executeUploadInitiation(GenericUrl initiationRequestUrl) throws Exception {
    updateStateAndNotifyListener(UploadState.INITIATION_STARTED);

    initiationRequestUrl.put("uploadType", "resumable");
    HttpRequest request =
        requestFactory.buildRequest(initiationMethod, initiationRequestUrl, metadata);
    addMethodOverride(request);
    initiationHeaders.setUploadContentType(mediaContent.getType());
    initiationHeaders.setUploadContentLength(getMediaContentLength());
    request.setHeaders(initiationHeaders);
    request.setAllowEmptyContent(false);
    request.setRetryOnExecuteIOException(true);
    request.setEnableGZipContent(true);
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
   * Wraps PUT HTTP requests inside of a POST request and uses {@code "X-HTTP-Method-Override"}
   * header to specify the actual HTTP method. This is done in case the HTTP transport does not
   * support PUT.
   *
   * @param request HTTP request
   */
  private void addMethodOverride(HttpRequest request) {
    new MethodOverride().intercept(request);
  }

  /**
   * Sets the HTTP media content chunk and the required headers that should be used in the upload
   * request.
   *
   * @param bytesWritten The number of bytes that have been successfully uploaded on the server
   */
  private void setContentAndHeadersOnCurrentRequest(long bytesWritten) throws Exception {
    int blockSize = (int) Math.min(chunkSize, getMediaContentLength() - bytesWritten);
    // TODO(rmistry): Add tests for LimitInputStream.
    InputStreamContent contentChunk =
        new InputStreamContent(mediaContent.getType(),
          new LimitInputStream(contentInputStream, blockSize));
    contentChunk.setCloseInputStream(false);
    contentChunk.setRetrySupported(true);
    contentChunk.setLength(blockSize);
    // Mark the current position in case we need to retry the request.
    contentInputStream.mark(blockSize);
    currentRequest.setContent(contentChunk);
    currentRequest.getHeaders().setContentRange("bytes " + bytesWritten + "-"
        + (bytesWritten + blockSize - 1) + "/" + getMediaContentLength());
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
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   */
  public void serverErrorCallback() throws Exception {
    Preconditions.checkNotNull(currentRequest, "The current request should not be null");

    // TODO(rmistry): Handle timeouts here similar to how server errors are handled.
    // Query the current status of the upload by issuing an empty POST request on the upload URI.
    HttpRequest request = requestFactory.buildPutRequest(currentRequest.getUrl(), null);
    new MethodOverride().intercept(request); // needed for PUT
    // The resumable media upload protocol requires Content-Length to be 0.
    request.setAllowEmptyContent(true);
    request.setContent(new ByteArrayContent(null, new byte[0]));

    request.getHeaders().setContentRange("bytes */" + getMediaContentLength());
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

      // The current position of the input stream is likely incorrect because the upload was
      // interrupted. Reset the position and skip ahead to the correct spot.
      contentInputStream.reset();
      long skipValue = bytesUploaded - bytesWritten;
      long actualSkipValue = contentInputStream.skip(skipValue);
      Preconditions.checkState(skipValue == actualSkipValue);

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
   * The minimum allowable value is {@link #MINIMUM_CHUNK_SIZE}.
   * </p>
   */
  public MediaHttpUploader setChunkSize(int chunkSize) {
    Preconditions.checkArgument(chunkSize >= MINIMUM_CHUNK_SIZE);
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
   * Sets the HTTP method used for the initiation request. Can only be {@link HttpMethod#POST} (for
   * media upload) or {@link HttpMethod#PUT} (for media update). The default value is
   * {@link HttpMethod#POST}.
   */
  public MediaHttpUploader setInitiationMethod(HttpMethod initiationMethod) {
    Preconditions.checkArgument(
        initiationMethod == HttpMethod.POST || initiationMethod == HttpMethod.PUT);
    this.initiationMethod = initiationMethod;
    return this;
  }

  /**
   * Returns the HTTP method used for the initiation request. The default value is
   * {@link HttpMethod#POST}.
   */
  public HttpMethod getInitiationMethod() {
    return initiationMethod;
  }

  /** Sets the HTTP headers used for the initiation request. */
  public MediaHttpUploader setInitiationHeaders(GoogleHeaders initiationHeaders) {
    this.initiationHeaders = initiationHeaders;
    return this;
  }

  /** Returns the HTTP headers used for the initiation request. */
  public GoogleHeaders getInitiationHeaders() {
    return initiationHeaders;
  }

  /**
   * Gets the total number of bytes uploaded by this uploader.
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
  private void updateStateAndNotifyListener(UploadState uploadState) throws Exception {
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
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   *
   * @return the upload progress
   */
  public double getProgress() throws Exception {
    return (double) bytesUploaded / getMediaContentLength();
  }
}
