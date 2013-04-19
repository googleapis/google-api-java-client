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

import com.google.api.client.http.BackOffPolicy;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.MultipartContent;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sleeper;

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

     public void onSuccess(Volumes volumes, HttpHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
       log(e.getError().getMessage());
     }
   });
   batch.queue(volumesList, Volumes.class, GoogleJsonErrorContainer.class,
       new BatchCallback&lt;Volumes, GoogleJsonErrorContainer&gt;() {

     public void onSuccess(Volumes volumes, HttpHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
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
 * Redirects are currently not followed in {@link BatchRequest}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * <p>
 * Note: When setting an {@link HttpUnsuccessfulResponseHandler} by calling to
 * {@link HttpRequest#setUnsuccessfulResponseHandler}, the handler is called for each unsuccessful
 * part. As a result it's not recommended to use {@link HttpBackOffUnsuccessfulResponseHandler} on a
 * batch request, since the back-off policy is invoked for each unsuccessful part.
 * </p>
 *
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
@SuppressWarnings("deprecation")
public final class BatchRequest {

  /** The URL where batch requests are sent. */
  private GenericUrl batchUrl = new GenericUrl("https://www.googleapis.com/batch");

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /** The list of queued request infos. */
  List<RequestInfo<?, ?>> requestInfos = new ArrayList<RequestInfo<?, ?>>();

  /** Sleeper. */
  private Sleeper sleeper = Sleeper.DEFAULT;

  /** A container class used to hold callbacks and data classes. */
  static class RequestInfo<T, E> {
    final BatchCallback<T, E> callback;
    final Class<T> dataClass;
    final Class<E> errorClass;
    final HttpRequest request;

    RequestInfo(BatchCallback<T, E> callback, Class<T> dataClass, Class<E> errorClass,
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
        ? transport.createRequestFactory() : transport.createRequestFactory(httpRequestInitializer);
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
  public BatchRequest setSleeper(Sleeper sleeper) {
    this.sleeper = Preconditions.checkNotNull(sleeper);
    return this;
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
  public <T, E> BatchRequest queue(HttpRequest httpRequest, Class<T> dataClass, Class<E> errorClass,
      BatchCallback<T, E> callback) throws IOException {
    Preconditions.checkNotNull(httpRequest);
    // TODO(rmistry): Add BatchUnparsedCallback with onResponse(InputStream content, HttpHeaders).
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
    boolean retryAllowed;
    Preconditions.checkState(!requestInfos.isEmpty());
    HttpRequest batchRequest = requestFactory.buildPostRequest(this.batchUrl, null);
    // NOTE: batch does not support gzip encoding
    HttpExecuteInterceptor originalInterceptor = batchRequest.getInterceptor();
    batchRequest.setInterceptor(new BatchInterceptor(originalInterceptor));
    int retriesRemaining = batchRequest.getNumberOfRetries();
    BackOffPolicy backOffPolicy = batchRequest.getBackOffPolicy();

    if (backOffPolicy != null) {
      // Reset the BackOffPolicy at the start of each execute.
      backOffPolicy.reset();
    }

    do {
      retryAllowed = retriesRemaining > 0;
      MultipartContent batchContent = new MultipartContent();
      batchContent.getMediaType().setSubType("mixed");
      int contentId = 1;
      for (RequestInfo<?, ?> requestInfo : requestInfos) {
        batchContent.addPart(new MultipartContent.Part(
            new HttpHeaders().setAcceptEncoding(null).set("Content-ID", contentId++),
            new HttpRequestContent(requestInfo.request)));
      }
      batchRequest.setContent(batchContent);
      HttpResponse response = batchRequest.execute();
      BatchUnparsedResponse batchResponse;
      try {
        // Find the boundary from the Content-Type header.
        String boundary = "--" + response.getMediaType().getParameter("boundary");

        // Parse the content stream.
        InputStream contentStream = response.getContent();
        batchResponse =
            new BatchUnparsedResponse(contentStream, boundary, requestInfos, retryAllowed);

        while (batchResponse.hasNext) {
          batchResponse.parseNextResponse();
        }
      } finally {
        response.disconnect();
      }

      List<RequestInfo<?, ?>> unsuccessfulRequestInfos = batchResponse.unsuccessfulRequestInfos;
      if (!unsuccessfulRequestInfos.isEmpty()) {
        requestInfos = unsuccessfulRequestInfos;
        // backOff if required.
        if (batchResponse.backOffRequired && backOffPolicy != null) {
          long backOffTime = backOffPolicy.getNextBackOffMillis();
          if (backOffTime != BackOffPolicy.STOP) {
            try {
              sleeper.sleep(backOffTime);
            } catch (InterruptedException exception) {
              // ignore
            }
          }
        }
      } else {
        break;
      }
      retriesRemaining--;
    } while (retryAllowed);
    requestInfos.clear();
  }

  /**
   * Batch HTTP request execute interceptor that loops through all individual HTTP requests and runs
   * their interceptors.
   */
  class BatchInterceptor implements HttpExecuteInterceptor {

    private HttpExecuteInterceptor originalInterceptor;

    BatchInterceptor(HttpExecuteInterceptor originalInterceptor) {
      this.originalInterceptor = originalInterceptor;
    }

    public void intercept(HttpRequest batchRequest) throws IOException {
      if (originalInterceptor != null) {
        originalInterceptor.intercept(batchRequest);
      }
      for (RequestInfo<?, ?> requestInfo : requestInfos) {
        HttpExecuteInterceptor interceptor = requestInfo.request.getInterceptor();
        if (interceptor != null) {
          interceptor.intercept(requestInfo.request);
        }
      }
    }

  }
}
