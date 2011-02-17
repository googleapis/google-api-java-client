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

import java.util.Collection;
import java.util.List;

/**
 * JSON-RPC 2.0 HTTP transport for RPC requests for Google API's, including both singleton and
 * batched requests.
 *
 * <p>
 * Warning: this is based on an undocumented experimental Google functionality that may stop working
 * or change in behavior at any time. Beware of this risk if running this in production code.
 * </p>
 * <p>
 * Warning: in prior version 1.2 of this library this was called {@code
 * com.google.api.client.json.rpc2.GoogleJsonRpcHttpTransport}.
 * </p>
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public final class GoogleJsonRpcHttpTransport {

  /** RPC server URL. */
  public GenericUrl rpcServerUrl;

  /** (REQUIRED) HTTP transport required for building requests. */
  public HttpTransport transport;

  /** (REQUIRED) JSON factory to use for building requests. */
  public JsonFactory jsonFactory;

  /**
   * Content type header to use for requests. By default this is {@code "application/json-rpc"}.
   */
  public String contentType = "application/json-rpc";

  /**
   * Accept header to use for requests. By default this is {@code "application/json-rpc"}.
   */
  public String accept = contentType;

  /**
   * Builds a POST HTTP request for the JSON-RPC requests objects specified in the given JSON-RPC
   * request object.
   * <p>
   * You may use
   * {@link JsonHttpParser#parserForResponse(com.google.api.client.json.JsonFactory, HttpResponse)
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
   * {@link JsonHttpParser#parserForResponse(com.google.api.client.json.JsonFactory, HttpResponse)
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
    HttpRequest httpRequest = transport.buildPostRequest();
    httpRequest.url = rpcServerUrl;
    JsonHttpContent content = new JsonHttpContent();
    content.jsonFactory = jsonFactory;
    content.contentType = contentType;
    httpRequest.headers.accept = accept;
    content.data = data;
    httpRequest.content = content;
    return httpRequest;
  }
}
