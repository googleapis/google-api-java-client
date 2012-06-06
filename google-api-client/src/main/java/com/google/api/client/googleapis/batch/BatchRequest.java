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

package com.google.api.client.googleapis.batch;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class represents a single batch of requests.
 *
 * <p>
 * Sample use:
 * </p>
 *
 * <pre>
   BatchRequest batch = new BatchRequest(transport, httpRequestInitializer);
   batch.queue(volumesList, Volumes.class, GoogleJsonErrorContainer.class,
       new BatchCallback&lt;Volumes, GoogleJsonErrorContainer&gt;() {

     public void onSuccess(Volumes volumes, GoogleHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, GoogleHeaders responseHeaders) {
       log(e.getError().getMessage());
     }
   });
   batch.queue(volumesList, Volumes.class, GoogleJsonErrorContainer.class,
       new BatchCallback&lt;Volumes, GoogleJsonErrorContainer&gt;() {

     public void onSuccess(Volumes volumes, GoogleHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, GoogleHeaders responseHeaders) {
       log(e.getError().getMessage());
     }
   });
   batch.execute();
 * </pre>
 *
 * <p>
 * The content of each individual response is stored in memory. There is thus a potential of
 * encountering an {@link OutOfMemoryError} for very large responses.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class BatchRequest {

  /** The URL where batch requests are sent. */
  private GenericUrl batchUrl = new GenericUrl("https://www.googleapis.com/batch");

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /** The list of queued request infos. */
  List<RequestInfo<?, ?>> requestInfos = new ArrayList<RequestInfo<?, ?>>();

  /** A container class used to hold callbacks and data classes. */
  static class RequestInfo<T, E> {
    final BatchCallback<T, E> callback;
    final Class<T> dataClass;
    final Class<E> errorClass;
    final HttpRequest request;

    RequestInfo(BatchCallback<T, E> callback,
        Class<T> dataClass,
        Class<E> errorClass,
        HttpRequest request) {
      this.callback = callback;
      this.dataClass = dataClass;
      this.errorClass = errorClass;
      this.request = request;
    }
  }

  /**
   * Construct the {@link BatchRequest}.
   *
   * @param transport The transport to use for requests
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   */
  public BatchRequest(HttpTransport transport, HttpRequestInitializer httpRequestInitializer) {
    this.requestFactory = httpRequestInitializer == null
        ? transport.createRequestFactory() : transport
        .createRequestFactory(httpRequestInitializer);
  }

  /**
   * Sets the URL that will be hit when {@link #execute()} is called. The default value is
   * {@code https://www.googleapis.com/batch}.
   */
  public BatchRequest setBatchUrl(GenericUrl batchUrl) {
    this.batchUrl = batchUrl;
    return this;
  }

  /** Returns the URL that will be hit when {@link #execute()} is called. */
  public GenericUrl getBatchUrl() {
    return batchUrl;
  }

  /**
   * Queues the specified {@link HttpRequest} for batched execution. Batched requests are executed
   * when {@link #execute()} is called.
   *
   * @param <T> destination class type
   * @param <E> error class type
   * @param httpRequest HTTP Request
   * @param dataClass Data class the response will be parsed into or {@code Void.class} to ignore
   *        the content
   * @param errorClass Data class the unsuccessful response will be parsed into or
   *        {@code Void.class} to ignore the content
   * @param callback Batch Callback
   * @return this Batch request
   * @throws IOException If building the HTTP Request fails
   */
  public <T, E> BatchRequest queue(HttpRequest httpRequest,
      Class<T> dataClass,
      Class<E> errorClass,
      BatchCallback<T, E> callback) throws IOException {
    Preconditions.checkNotNull(httpRequest);
    // TODO(rmistry): Add BatchUnparsedCallback with onResponse(InputStream content, GoogleHeaders).
    Preconditions.checkNotNull(callback);
    Preconditions.checkNotNull(dataClass);
    Preconditions.checkNotNull(errorClass);

    requestInfos.add(new RequestInfo<T, E>(callback, dataClass, errorClass, httpRequest));
    return this;
  }

  /**
   * Returns the number of queued requests in this batch request.
   */
  public int size() {
    return requestInfos.size();
  }

  /**
   * Executes all queued HTTP requests in a single call, parses the responses and invokes callbacks.
   *
   * <p>
   * Calling {@link #execute()} executes and clears the queued requests. This means that the
   * {@link BatchRequest} object can be reused to {@link #queue} and {@link #execute()} requests
   * again.
   * </p>
   */
  public void execute() throws IOException {
    execute(true);
  }

  private void execute(boolean retryUnsuccessfulRequests) throws IOException {
    HttpResponse response = executeUnparsed();
    BatchUnparsedResponse batchResponse;

    try {
      // Find the boundary from the Content-Type header.
      String contentType = response.getHeaders().getContentType();
      String[] parts = contentType.split(";");
      String boundary = null;
      for (String part : parts) {
        if (part.contains("boundary=")) {
          int boundaryStartIndex = part.indexOf("boundary=");
          boundary = "--" + part.substring(boundaryStartIndex + "boundary=".length());
          break;
        }
      }

      // Parse the content stream.
      InputStream contentStream = response.getContent();
      batchResponse = new BatchUnparsedResponse(contentStream, boundary, requestInfos);

      while (batchResponse.hasNext) {
        batchResponse.parseNextResponse();
      }
    } finally {
      response.disconnect();
    }

    List<RequestInfo<?, ?>> unsuccessfulRequestInfos = batchResponse.unsuccessfulRequestInfos;
    if (retryUnsuccessfulRequests && !unsuccessfulRequestInfos.isEmpty()) {
      requestInfos = unsuccessfulRequestInfos;
      execute(false);
    }

    requestInfos.clear();
  }

  /** Executes all queued requests in a single call without parsing individual responses. */
  HttpResponse executeUnparsed() throws IOException {
    // TODO(rmistry): Handle unsuccessful batch responses and add retries.
    HttpRequest batchRequest = buildHttpRequest();
    return batchRequest.execute();
  }

  /** Builds a HTTP multipart Request. */
  HttpRequest buildHttpRequest() throws IOException {
    Preconditions.checkState(!requestInfos.isEmpty());
    HttpContent content = new MultipartMixedContent(requestInfos, "__END_OF_PART__");
    HttpRequest request = requestFactory.buildPostRequest(this.batchUrl, content);
    request.setInterceptor(new BatchInterceptor());
    return request;
  }

  /**
   * Batch HTTP request execute interceptor that loops through all individual HTTP requests and runs
   * their interceptors.
   */
  class BatchInterceptor implements HttpExecuteInterceptor {

    public void intercept(HttpRequest batchRequest) throws IOException {
      for (RequestInfo<?, ?> requestInfo : requestInfos) {
        HttpExecuteInterceptor interceptor = requestInfo.request.getInterceptor();
        if (interceptor != null) {
          interceptor.intercept(requestInfo.request);
        }
      }
    }

  }
}
