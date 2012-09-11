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

package com.google.api.client.googleapis;

import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.subscriptions.NotificationCallback;
import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.TypedNotificationCallback;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract Google client request for a {@link AbstractGoogleClient}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @param <T> type of the response
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleClientRequest<T> extends GenericData {

  /** Google client. */
  private final AbstractGoogleClient client;

  /** HTTP method. */
  private final String method;

  /** URI template for the path relative to the base URL. */
  private final String uriTemplate;

  /** HTTP content or {@code null} for none. */
  private final HttpContent content;

  /** HTTP headers used for the Google client request. */
  private HttpHeaders requestHeaders = new HttpHeaders();

  /** HTTP headers of the last response or {@code null} for none. */
  private HttpHeaders lastResponseHeaders;

  /** Whether to disable GZip compression of HTTP content. */
  private boolean disableGZipContent;

  /** Response class to parse into. */
  private Class<T> responseClass;

  /** Whether to subscribe to notifications. */
  private boolean isSubscribing;

  /** Callback for processing subscription notifications or {@code null} for none. */
  private NotificationCallback notificationCallback;

  /** Subscription details of the last response or {@code null} for none. */
  private Subscription lastSubscription;

  /** Media HTTP uploader or {@code null} for none. */
  private MediaHttpUploader uploader;

  /** Media HTTP downloader or {@code null} for none. */
  private MediaHttpDownloader downloader;

  /**
   * @param client Google client
   * @param method HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param content HTTP content or {@code null} for none
   * @param responseClass response class to parse into
   */
  protected AbstractGoogleClientRequest(AbstractGoogleClient client, String method,
      String uriTemplate, HttpContent content, Class<T> responseClass) {
    this.responseClass = Preconditions.checkNotNull(responseClass);
    this.client = Preconditions.checkNotNull(client);
    this.method = Preconditions.checkNotNull(method);
    this.uriTemplate = Preconditions.checkNotNull(uriTemplate);
    this.content = content;
  }

  /** Returns whether to disable GZip compression of HTTP content. */
  public final boolean getDisableGZipContent() {
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
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractGoogleClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    this.disableGZipContent = disableGZipContent;
    return this;
  }

  /** Returns the HTTP method. */
  public final String getMethod() {
    return method;
  }

  /** Returns the URI template for the path relative to the base URL. */
  public final String getUriTemplate() {
    return uriTemplate;
  }

  /** Returns the HTTP content or {@code null} for none. */
  public final HttpContent getContent() {
    return content;
  }

  /**
   * Returns the Google client.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractGoogleClient getClient() {
    return client;
  }

  /** Returns the HTTP headers used for the Google client request. */
  public final HttpHeaders getRequestHeaders() {
    return requestHeaders;
  }

  /**
   * Sets the HTTP headers used for the Google client request.
   *
   * <p>
   * These headers are set on the request after {@link #buildHttpRequest} is called, this means that
   * {@link HttpRequestInitializer#initialize} is called first.
   * </p>
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractGoogleClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    this.requestHeaders = headers;
    return this;
  }

  /** Gets the HTTP headers of the last response or {@code null} for none. */
  public final HttpHeaders getLastResponseHeaders() {
    return lastResponseHeaders;
  }

  /** Returns the response class to parse into. */
  public final Class<T> getResponseClass() {
    return responseClass;
  }

  /**
   * Returns whether to subscribe to notifications.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   */
  protected boolean isSubscribing() {
    return isSubscribing;
  }

  /**
   * Returns the callback for processing subscription notifications or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   */
  protected NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /**
   * Subscribes to notifications without specifying a notification callback.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   */
  protected AbstractGoogleClientRequest<T> subscribe() {
    isSubscribing = true;
    return this;
  }

  /**
   * Subscribes to notifications to be sent to the given notification callback.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   */
  @SuppressWarnings("unchecked")
  protected AbstractGoogleClientRequest<T> subscribe(NotificationCallback notificationCallback) {
    this.notificationCallback = Preconditions.checkNotNull(notificationCallback);
    if (notificationCallback instanceof TypedNotificationCallback<?>) {
      ((TypedNotificationCallback<T>) notificationCallback).setDataType(responseClass);
    }
    return subscribe();
  }

  /**
   * Returns the subscription details of the last response or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of changing visibility to public, but nothing
   * else.
   * </p>
   */
  protected Subscription getLastSubscription() {
    return lastSubscription;
  }

  /**
   * Sets the subscription details of the last response or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  protected AbstractGoogleClientRequest<T> setLastSubscription(Subscription lastSubscription) {
    this.lastSubscription = lastSubscription;
    return this;
  }

  /** Returns the media HTTP Uploader or {@code null} for none. */
  public final MediaHttpUploader getMediaHttpUploader() {
    return uploader;
  }

  /**
   * Initializes the media HTTP uploader based on the media content.
   *
   * @param mediaContent media content
   */
  protected final void initializeMediaUpload(AbstractInputStreamContent mediaContent) {
    HttpRequestFactory requestFactory = getClient().getRequestFactory();
    this.uploader = new MediaHttpUploader(
        mediaContent, requestFactory.getTransport(), requestFactory.getInitializer());
    this.uploader.setInitiationRequestMethod(method);
    if (content != null) {
      this.uploader.setMetadata(content);
    }
  }

  /** Returns the media HTTP downloader or {@code null} for none. */
  public final MediaHttpDownloader getMediaHttpDownloader() {
    return downloader;
  }

  /** Initializes the media HTTP downloader. */
  protected final void initializeMediaDownload() {
    HttpRequestFactory requestFactory = getClient().getRequestFactory();
    this.downloader =
        new MediaHttpDownloader(requestFactory.getTransport(), requestFactory.getInitializer());
  }

  /**
   * Creates a new instance of {@link GenericUrl} suitable for use against this service.
   *
   * @return newly created {@link GenericUrl}
   */
  public final GenericUrl buildHttpRequestUrl() {
    return new GenericUrl(UriTemplate.expand(getClient().getBaseUrl(), uriTemplate, this, true));
  }

  /** Create an {@link HttpRequest} suitable for use against this service. */
  public final HttpRequest buildHttpRequest() throws Exception {
    Preconditions.checkArgument(uploader == null);
    HttpRequest request = client.buildHttpRequest(method, buildHttpRequestUrl(), content);
    // Add specified headers (if any) to the headers in the request.
    request.getHeaders().putAll(getRequestHeaders());
    if (isSubscribing) {
      client.getSubscriptionManager().addSubscriptionRequestHeaders(request);
    }
    return request;
  }

  /**
   * Sends the request to the server and returns the raw {@link HttpResponse}. Subclasses may
   * override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for disconnecting the HTTP response by calling
   * {@link HttpResponse#disconnect}. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = request.executeUnparsed();
     try {
       // process response..
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * <p>
   * Subclasses may override by calling the super implementation.
   * </p>
   *
   * @return the {@link HttpResponse}
   */
  public HttpResponse executeUnparsed() throws Exception {
    if (uploader == null) {
      HttpRequest request = buildHttpRequest();
      request.setEnableGZipContent(!disableGZipContent);
      HttpResponse response = client.executeUnparsed(request);
      lastResponseHeaders = response.getHeaders();
      if (notificationCallback != null) {
        lastSubscription = client.getSubscriptionManager()
            .processSubscribeResponse(lastResponseHeaders, notificationCallback);
      }
      return response;
    }
    return uploader.upload(buildHttpRequestUrl());
  }

  /**
   * Sends the request to the server and returns the parsed response.
   *
   * @return parsed HTTP response
   */
  public final T execute() throws Exception {
    HttpResponse response = executeUnparsed();
    // TODO(yanivi): remove workaround when feature is implemented
    // workaround for http://code.google.com/p/google-http-java-client/issues/detail?id=110
    if (Void.class.equals(responseClass)) {
      response.ignore();
      return null;
    }
    return response.parseAs(responseClass);
  }

  /**
   * Sends the request to the server and returns the content input stream of {@link HttpResponse}.
   * Subclasses may override if specific behavior is required.
   *
   * <p>
   * Callers are responsible for closing the input stream after it is processed. Example sample:
   * </p>
   *
   * <pre>
     InputStream is = request.executeAsInputStream();
     try {
       // Process input stream..
     } finally {
       is.close();
     }
   * </pre>
   *
   * @return input stream of the response content
   */
  public final InputStream executeAsInputStream() throws Exception {
    HttpResponse response = executeUnparsed();
    return response.getContent();
  }

  /**
   * Sends the request to the server and writes the content input stream of {@link HttpResponse}
   * into the given destination output stream.
   *
   * <p>
   * This method closes the content of the HTTP response from {@link HttpResponse#getContent()}.
   * </p>
   *
   * @param outputStream destination output stream
   */
  public final void download(OutputStream outputStream) throws Exception {
    if (downloader == null) {
      HttpResponse response = executeUnparsed();
      response.download(outputStream);
    } else {
      downloader.download(buildHttpRequestUrl(), outputStream);
    }
  }

  /**
   * Queues the request into the specified batch request container using the specified error class.
   *
   * <p>
   * Batched requests are then executed when {@link BatchRequest#execute()} is called.
   * </p>
   *
   * @param batchRequest batch request container
   * @param errorClass data class the unsuccessful response will be parsed into or
   *        {@code Void.class} to ignore the content
   * @param callback batch callback
   */
  public final <E> void queue(
      BatchRequest batchRequest, Class<E> errorClass, BatchCallback<T, E> callback)
      throws Exception {
    batchRequest.queue(buildHttpRequest(), getResponseClass(), errorClass, callback);
  }
}
