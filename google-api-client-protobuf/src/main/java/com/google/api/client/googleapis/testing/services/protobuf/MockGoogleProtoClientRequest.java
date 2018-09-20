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

package com.google.api.client.googleapis.testing.services.protobuf;

import com.google.api.client.googleapis.services.protobuf.AbstractGoogleProtoClient;
import com.google.api.client.googleapis.services.protobuf.AbstractGoogleProtoClientRequest;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.UriTemplate;
import com.google.api.client.util.Beta;
import com.google.protobuf.MessageLite;

/**
 * {@link Beta} <br/>
 * Thread-safe mock Google protocol buffer request.
 *
 * @param <T> type of the response
 * @since 1.16
 * @author Yaniv Inbar
 */
@Beta
public class MockGoogleProtoClientRequest<T> extends AbstractGoogleProtoClientRequest<T> {

  /**
   * @param client Google client
   * @param method HTTP Method
   * @param uriTemplate URI template for the path relative to the base URL. If it starts with a "/"
   *        the base path from the base URL will be stripped out. The URI template can also be a
   *        full URL. URI template expansion is done using
   *        {@link UriTemplate#expand(String, String, Object, boolean)}
   * @param message message to serialize or {@code null} for none
   * @param responseClass response class to parse into
   */
  public MockGoogleProtoClientRequest(AbstractGoogleProtoClient client, String method,
      String uriTemplate, MessageLite message, Class<T> responseClass) {
    super(client, method, uriTemplate, message, responseClass);
  }

  @Override
  public MockGoogleProtoClient getAbstractGoogleClient() {
    return (MockGoogleProtoClient) super.getAbstractGoogleClient();
  }

  @Override
  public MockGoogleProtoClientRequest<T> setDisableGZipContent(boolean disableGZipContent) {
    return (MockGoogleProtoClientRequest<T>) super.setDisableGZipContent(disableGZipContent);
  }

  @Override
  public MockGoogleProtoClientRequest<T> setRequestHeaders(HttpHeaders headers) {
    return (MockGoogleProtoClientRequest<T>) super.setRequestHeaders(headers);
  }
}
