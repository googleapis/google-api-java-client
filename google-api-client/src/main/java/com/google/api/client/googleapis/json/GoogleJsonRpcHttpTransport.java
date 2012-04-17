/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.CustomizeJsonParser;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * JSON-RPC 2.0 HTTP transport for RPC requests for Google API's, including both singleton and
 * batched requests.
 *
 * <p>
 * This implementation is thread-safe, as long as you don't change the values of the public fields
 * after construction. These fields are deprecated for this reason.
 * </p>
 *
 * <p>
 * Warning: this is based on an undocumented experimental Google functionality that may stop working
 * or change in behavior at any time. Beware of this risk if running this in production code.
 * </p>
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public final class GoogleJsonRpcHttpTransport {

  private static final String JSON_RPC_CONTENT_TYPE = "application/json-rpc";

  /**
   * RPC server URL.
   *
   * @deprecated (scheduled to be made private final in 1.10) Use
   *             {@link Builder#setRpcServerUrl(String)}.
   */
  // TODO(jasonhall): When this becomes final, make this field a String for immutability.
  @Deprecated
  public GenericUrl rpcServerUrl;

  /**
   * (REQUIRED) HTTP transport required for building requests.
   *
   * @deprecated (scheduled to be made private final in 1.10) Use {@link Builder}.
   */
  @Deprecated
  public HttpTransport transport;

  /**
   * (REQUIRED) JSON factory to use for building requests.
   *
   * @deprecated (scheduled to be made private final in 1.10) Use {@link Builder}.
   */
  @Deprecated
  public JsonFactory jsonFactory;

  /**
   * Content type header to use for requests. By default this is {@code "application/json-rpc"}.
   *
   * @deprecated (scheduled to be made private final in 1.10) Use
   *             {@link Builder#setContentType(String)}.
   */
  @Deprecated
  public String contentType = JSON_RPC_CONTENT_TYPE;

  /**
   * Accept header to use for requests. By default this is {@code "application/json-rpc"}.
   *
   * @deprecated (scheduled to be made private final in 1.10) Use {@link Builder#setAccept(String)}.
   */
  @Deprecated
  public String accept = contentType;

  /**
   * Creates a new {@link GoogleJsonRpcHttpTransport} with default values for RPC server, and
   * Content type and Accept headers.
   *
   * @param httpTransport HTTP transport required for building requests.
   * @param jsonFactory JSON factory to use for building requests.
   *
   * @since 1.9
   */
  public GoogleJsonRpcHttpTransport(HttpTransport httpTransport, JsonFactory jsonFactory) {
    this(Preconditions.checkNotNull(httpTransport), Preconditions.checkNotNull(jsonFactory),
         Builder.DEFAULT_SERVER_URL.build(), JSON_RPC_CONTENT_TYPE, JSON_RPC_CONTENT_TYPE);
  }

  /**
   * Creates a new {@link GoogleJsonRpcHttpTransport}.
   *
   * @param httpTransport HTTP transport required for building requests.
   * @param jsonFactory JSON factory to use for building requests.
   * @param rpcServerUrl RPC server URL.
   * @param contentType Content type header to use for requests.
   * @param accept Accept header to use for requests.
   *
   * @since 1.9
   */
  protected GoogleJsonRpcHttpTransport(HttpTransport httpTransport, JsonFactory jsonFactory,
      String rpcServerUrl, String contentType, String accept) {
    this.transport = httpTransport;
    this.jsonFactory = jsonFactory;
    this.rpcServerUrl = new GenericUrl(rpcServerUrl);
    this.contentType = contentType;
    this.accept = accept;
  }

  /**
   * Returns the HTTP transport used for building requests.
   *
   * @since 1.9
   */
  public final HttpTransport getHttpTransport() {
    return transport;
  }

  /**
   * Returns the JSON factory used for building requests.
   *
   * @since 1.9
   */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns the RPC server URL.
   *
   * @since 1.9
   */
  public final String getRpcServerUrl() {
    return rpcServerUrl.build();
  }

  /**
   * Returns the Content type header used for requests.
   *
   * @since 1.9
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Returns the Accept header used for requests.
   *
   * @since 1.9
   */
  public final String getAccept() {
    return accept;
  }

  /**
   * {@link GoogleJsonRpcHttpTransport} Builder.
   *
   * <p>
   * Implementation is not thread safe.
   * </p>
   *
   * @since 1.9
   */
  public static class Builder {

    /** Default RPC server URL. */
    private static final GenericUrl DEFAULT_SERVER_URL =
        new GenericUrl("https://www.googleapis.com");

    /** HTTP transport required for building requests. */
    private final HttpTransport httpTransport;

    /** JSON factory to use for building requests. */
    private final JsonFactory jsonFactory;

    /** RPC server URL. */
    private GenericUrl rpcServerUrl = DEFAULT_SERVER_URL;

    /** Content type header to use for requests. */
    private String contentType = JSON_RPC_CONTENT_TYPE;

    /** Accept header to use for requests. */
    private String accept = contentType;

    /**
     * @param httpTransport HTTP transport required for building requests.
     * @param jsonFactory JSON factory to use for building requests.
     */
    public Builder(HttpTransport httpTransport, JsonFactory jsonFactory) {
      this.httpTransport = Preconditions.checkNotNull(httpTransport);
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    }

    /**
     * Sets the RPC server URL.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param rpcServerUrl RPC server URL.
     */
    protected Builder setRpcServerUrl(GenericUrl rpcServerUrl) {
      this.rpcServerUrl = Preconditions.checkNotNull(rpcServerUrl);
      return this;
    }

    /**
     * Sets the Content type header to use for requests. By default this is
     * {@code "application/json-rpc"}.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param contentType Content type header to use for requests.
     */
    protected Builder setContentType(String contentType) {
      this.contentType = Preconditions.checkNotNull(contentType);
      return this;
    }

    /**
     * Sets the Accept header to use for requests. By default this is {@code "application/json-rpc"}
     * .
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param accept Accept header to use for requests.
     */
    protected Builder setAccept(String accept) {
      this.accept = Preconditions.checkNotNull(accept);
      return this;
    }

    /**
     * Returns a new {@link GoogleJsonRpcHttpTransport} instance.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    protected GoogleJsonRpcHttpTransport build() {
      return new GoogleJsonRpcHttpTransport(
          httpTransport, jsonFactory, rpcServerUrl.build(), contentType, accept);
    }

    /**
     * Returns the HTTP transport used for building requests.
     */
    public final HttpTransport getHttpTransport() {
      return httpTransport;
    }

    /**
     * Returns the JSON factory used for building requests.
     */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Returns the RPC server.
     */
    public final GenericUrl getRpcServerUrl() {
      return rpcServerUrl;
    }

    /**
     * Returns the Content type header used for requests.
     */
    public final String getContentType() {
      return contentType;
    }

    /**
     * Returns the Accept header used for requests.
     */
    public final String getAccept() {
      return accept;
    }
  }

  /**
   * Builds a POST HTTP request for the JSON-RPC requests objects specified in the given JSON-RPC
   * request object.
   * <p>
   * You may use {@link JsonHttpParser#parserForResponse(JsonFactory, HttpResponse)
   * JsonHttpParser.parserForResponse}({@link #buildPostRequest(JsonRpcRequest) execute} (request))
   * to get the {@link JsonParser}, and {@link JsonParser#parseAndClose(Class, CustomizeJsonParser)}
   * .
   * </p>
   *
   * @param request JSON-RPC request object
   * @return HTTP request
   */
  public HttpRequest buildPostRequest(JsonRpcRequest request) {
    return internalExecute(request);
  }

  /**
   * Builds a POST HTTP request for the JSON-RPC requests objects specified in the given JSON-RPC
   * request objects.
   * <p>
   * Note that the request will always use batching -- i.e. JSON array of requests -- even if there
   * is only one request. You may use
   * {@link JsonHttpParser#parserForResponse(JsonFactory, HttpResponse)
   * JsonHttpParser.parserForResponse}({@link #buildPostRequest(List) execute} (requests)) to get
   * the {@link JsonParser}, and
   * {@link JsonParser#parseArrayAndClose(Collection, Class, CustomizeJsonParser)} .
   * </p>
   *
   * @param requests JSON-RPC request objects
   * @return HTTP request
   */
  public HttpRequest buildPostRequest(List<JsonRpcRequest> requests) {
    return internalExecute(requests);
  }

  private HttpRequest internalExecute(Object data) {
    JsonHttpContent content = new JsonHttpContent(jsonFactory, data);
    content.setType(contentType);
    HttpRequest httpRequest;
    try {
      httpRequest = transport.createRequestFactory().buildPostRequest(rpcServerUrl, content);
      httpRequest.getHeaders().setAccept(accept);
      return httpRequest;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
