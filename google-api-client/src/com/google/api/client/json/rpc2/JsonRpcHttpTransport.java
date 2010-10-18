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

package com.google.api.client.json.rpc2;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.CustomizeJsonParser;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonHttpContent;
import com.google.api.client.json.JsonHttpParser;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Strings;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * JSON-RPC 2.0 HTTP transport for RPC requests, including both singleton and batched requests.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class JsonRpcHttpTransport {

  /** RPC server URL. */
  public GenericUrl rpcServerUrl;

  /**
   * HTTP transport to use for executing HTTP requests. By default this is an unmodified new
   * instance of {@link HttpTransport}.
   */
  public HttpTransport transport = new HttpTransport();

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
   * You may use {@link JsonHttpParser#parserForResponse(HttpResponse)
   * JsonHttpParser.parserForResponse}({@link #buildPostRequest(JsonRpcRequest) execute} (request))
   * to get the {@link JsonParser}, and
   * {@link Json#parseAndClose(JsonParser, Class, CustomizeJsonParser)} .
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
   * is only one request. You may use {@link JsonHttpParser#parserForResponse(HttpResponse)
   * JsonHttpParser.parserForResponse}({@link #buildPostRequest(List) execute} (requests)) to get
   * the {@link JsonParser}, and
   * {@link Json#parseArrayAndClose(JsonParser, Collection, Class, CustomizeJsonParser)} .
   * </p>
   *
   * @param requests JSON-RPC request objects
   * @return HTTP request
   */
  public HttpRequest buildPostRequest(List<JsonRpcRequest> requests) {
    return internalExecute(requests);
  }

  /**
   * Builds a GET HTTP request for the JSON-RPC requests objects specified in the given JSON-RPC
   * request object.
   * <p>
   * You may use {@link JsonHttpParser#parserForResponse(HttpResponse)
   * JsonHttpParser.parserForResponse}( {@link #buildGetRequest(JsonRpcRequest) executeUsingGet}
   * (request)) to get the {@link JsonParser}, and
   * {@link Json#parseAndClose(JsonParser, Class, CustomizeJsonParser)} .
   * </p>
   *
   * @param request JSON-RPC request object
   * @return HTTP response
   * @throws IOException I/O exception
   */
  public HttpRequest buildGetRequest(JsonRpcRequest request) throws IOException {
    HttpTransport transport = this.transport;
    HttpRequest httpRequest = transport.buildGetRequest();
    GenericUrl url = httpRequest.url = rpcServerUrl.clone();
    url.set("method", request.method);
    url.set("id", request.id);
    // base64 encode the params
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    JsonGenerator generator = Json.JSON_FACTORY.createJsonGenerator(byteStream, JsonEncoding.UTF8);
    try {
      Json.serialize(generator, request.params);
    } finally {
      generator.close();
    }
    url.set("params", Strings.fromBytesUtf8(Base64.encode(byteStream.toByteArray())));
    return httpRequest;
  }

  private HttpRequest internalExecute(Object data) {
    HttpTransport transport = this.transport;
    HttpRequest httpRequest = transport.buildPostRequest();
    httpRequest.url = rpcServerUrl;
    JsonHttpContent content = new JsonHttpContent();
    content.contentType = contentType;
    httpRequest.headers.accept = accept;
    content.data = data;
    httpRequest.content = content;
    return httpRequest;
  }
}
