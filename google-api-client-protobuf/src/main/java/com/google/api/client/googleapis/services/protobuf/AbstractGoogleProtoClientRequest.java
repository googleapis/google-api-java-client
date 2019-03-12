/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.services.protobuf;

import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.http.protobuf.ProtoHttpContent;
import com.google.api.client.util.Beta;
import com.google.protobuf.MessageLite;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Google protocol buffer request for a {@link AbstractGoogleProtoClient}.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @param <T> type of the response
 * @since 1.16
 * @author Yaniv Inbar
 */
@Beta
public abstract class AbstractGoogleProtoClientRequest<T> extends AbstractGoogleClientRequest<T> {

  /** Message to serialize or {@code null} for none. */
  private final MessageLite message;

  /**
   * @param abstractGoogleProtoClient Google protocol buffer client
   * @param requestMethod HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param message message to serialize or {@code null} for none
   * @param responseClass response class to parse into
   */
  protected AbstractGoogleProtoClientRequest(AbstractGoogleProtoClient abstractGoogleProtoClient,
      String requestMethod, String uriTemplate, MessageLite message, Class<T> responseClass) {
    super(abstractGoogleProtoClient, requestMethod, uriTemplate, message == null
        ? null : new ProtoHttpContent(message), responseClass);
    this.message = message;
  }

  @Override
  public AbstractGoogleProtoClient getAbstractGoogleClient() {
    return (AbstractGoogleProtoClient) super.getAbstractGoogleClient();
  }

  @Override
  public AbstractGoogleProtoClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    return (AbstractGoogleProtoClientRequest<T>) super.setDisableGZipContent(disableGZipContent);
  }

  @Override
  public AbstractGoogleProtoClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    return (AbstractGoogleProtoClientRequest<T>) super.setRequestHeaders(headers);
  }

  /**
   * Queues the request into the specified batch request container.
   *
   * <p>
   * Batched requests are then executed when {@link BatchRequest#execute()} is called.
   * </p>
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
   *
    request.queue(batchRequest, new BatchCallback{@literal <}SomeResponseType, Void{@literal >}() {

      public void onSuccess(SomeResponseType content, HttpHeaders responseHeaders) {
        log("Success");
      }

      public void onFailure(Void unused, HttpHeaders responseHeaders) {
        log(e.getMessage());
      }
    });
   * </pre>
   *
   *
   * @param batchRequest batch request container
   * @param callback batch callback
   */
  public final void queue(BatchRequest batchRequest, BatchCallback<T, Void> callback)
      throws IOException {
    super.queue(batchRequest, Void.class, callback);
  }

  /** Returns the message to serialize or {@code null} for none. */
  public Object getMessage() {
    return message;
  }

  @Override
  public AbstractGoogleProtoClientRequest<T> set(String fieldName, Object value) {
    return (AbstractGoogleProtoClientRequest<T>) super.set(fieldName, value);
  }
}
