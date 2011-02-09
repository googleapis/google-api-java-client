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

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.json.DiscoveryDocument.APIDefinition;
import com.google.api.client.googleapis.json.DiscoveryDocument.ServiceDefinition;
import com.google.api.client.googleapis.json.DiscoveryDocument.ServiceMethod;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Manages HTTP requests for a version of a Google API service with a simple interface based on the
 * new experimental Discovery API.
 *
 * @since 1.1
 * @author Yaniv Inbar
 */
public final class GoogleApi {

  /**
   * (Required) Name of the Google API, for example <code>buzz</code>.
   */
  public String name;

  /**
   * (Required) Version of the Google API, for example <code>v1</code>.
   */
  public String version;

  /**
   * HTTP transport required for building requests in {@link #buildRequest(String, Object)}.
   */
  public HttpTransport transport;

  /**
   * Service definition, normally set by {@link #load()}.
   */
  public ServiceDefinition serviceDefinition;

  /**
   * URL for the discovery endpoint.
   *
   * <p>
   * URL must be compatible with behavior for Discovery version 0.1.
   * </p>
   *
   * @since 1.3
   */
  public GoogleUrl discoveryUrl =
      new GoogleUrl("https://www.googleapis.com/discovery/0.1/describe");

  /**
   * HTTP transport required for loading the discovery document in {@link #load()}.
   *
   * @since 1.3
   */
  public HttpTransport discoveryTransport;

  /**
   * JSON factory to use.
   *
   * @since 1.3
   */
  public JsonFactory jsonFactory;

  /**
   * Forces the discovery document to be loaded, even if the service definition has already been
   * loaded.
   */
  public void load() throws IOException {
    GoogleUrl url = discoveryUrl.clone();
    url.put("api", name);
    HttpRequest request = discoveryTransport.buildGetRequest();
    request.url = url;
    JsonParser parser = JsonCParser.parserForResponse(jsonFactory, request.execute());
    parser.skipToKey(name);
    DiscoveryDocument doc = new DiscoveryDocument();
    APIDefinition apiDefinition = doc.apiDefinition;
    parser.parseAndClose(apiDefinition, null);
    serviceDefinition = doc.apiDefinition.get(version);
    Preconditions.checkNotNull(serviceDefinition, "version not found: %s", version);
  }

  /**
   * Creates an HTTP request based on the given method name and parameters.
   * <p>
   * If the discovery document has not yet been loaded, it will call {@link #load()}.
   * </p>
   *
   * @param fullyQualifiedMethodName name of method as defined in Discovery document of format
   *        "resourceName.methodName"
   * @param parameters user defined key / value data mapping or {@code null} for none
   * @return HTTP request
   * @throws IOException I/O exception reading
   */
  public HttpRequest buildRequest(String fullyQualifiedMethodName, Object parameters)
      throws IOException {
    // load service method
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(version);
    Preconditions.checkNotNull(discoveryTransport);
    Preconditions.checkNotNull(fullyQualifiedMethodName);
    if (serviceDefinition == null) {
      load();
    }
    ServiceMethod method = serviceDefinition.getResourceMethod(fullyQualifiedMethodName);
    Preconditions.checkNotNull(method, "method not found: %s", fullyQualifiedMethodName);
    // Create request for specified method
    HttpRequest request = discoveryTransport.buildRequest();
    request.method = HttpMethod.valueOf(method.httpMethod);
    request.url = GoogleUrl.create(serviceDefinition.baseUrl, method.pathUrl, parameters);
    return request;
  }
}
