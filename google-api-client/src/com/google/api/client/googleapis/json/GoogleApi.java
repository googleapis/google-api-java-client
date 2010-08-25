/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.escape.CharEscapers;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.json.DiscoveryDocument.ServiceDefinition;
import com.google.api.client.googleapis.json.DiscoveryDocument.ServiceMethod;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.util.DataUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages HTTP requests for a version of a Google API service with a simple
 * interface based on the new experimental Discovery API.
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
   * HTTP transport required for building requests in
   * {@link #buildRequest(String, Object)}.
   * <p>
   * It is initialized using {@link GoogleTransport#create()}.
   */
  public HttpTransport transport = GoogleTransport.create();

  /**
   * Service definition, normally set by {@link #load()}.
   */
  public ServiceDefinition serviceDefinition;

  /**
   * Forces the discovery document to be loaded, even if the service definition
   * has already been loaded.
   */
  public void load() throws IOException {
    DiscoveryDocument doc = DiscoveryDocument.load(this.name);
    this.serviceDefinition = doc.apiDefinition.get(this.version);
    Preconditions.checkNotNull(
        this.serviceDefinition, "version not found: %s", this.version);
  }

  /**
   * Creates an HTTP request based on the given method name and parameters.
   * <p>
   * If the discovery document has not yet been loaded, it will call
   * {@link #load()}.
   * </p>
   *
   * @param fullyQualifiedMethodName name of method as defined in Discovery
   *        document of format "resourceName.methodName"
   * @param parameters user defined key / value data mapping or {@code null} for
   *        none
   * @return HTTP request
   * @throws IOException I/O exception reading
   */
  public HttpRequest buildRequest(
      String fullyQualifiedMethodName, Object parameters) throws IOException {
    // load service method
    String name = this.name;
    String version = this.version;
    HttpTransport transport = this.transport;
    ServiceDefinition serviceDefinition = this.serviceDefinition;
    Preconditions.checkNotNull(name);
    Preconditions.checkNotNull(version);
    Preconditions.checkNotNull(transport);
    Preconditions.checkNotNull(fullyQualifiedMethodName);
    if (serviceDefinition == null) {
      load();
    }
    ServiceMethod method =
        serviceDefinition.getResourceMethod(fullyQualifiedMethodName);
    Preconditions.checkNotNull(
        method, "method not found: %s", fullyQualifiedMethodName);
    // Create request for specified method
    HttpRequest request = transport.buildRequest();
    request.method = method.httpMethod;
    HashMap<String, String> requestMap = new HashMap<String, String>();
    for (Map.Entry<String, Object> entry :
        DataUtil.mapOf(parameters).entrySet()) {
      Object value = entry.getValue();
      if (value != null) {
        requestMap.put(entry.getKey(), value.toString());
      }
    }
    GenericUrl url = new GenericUrl(serviceDefinition.baseUrl);
    // parse path URL
    String pathUrl = method.pathUrl;
    StringBuilder pathBuf = new StringBuilder();
    int cur = 0;
    int length = pathUrl.length();
    while (cur < length) {
      int next = pathUrl.indexOf('{', cur);
      if (next == -1) {
        pathBuf.append(pathUrl.substring(cur));
        break;
      }
      pathBuf.append(pathUrl.substring(cur, next));
      int close = pathUrl.indexOf('}', next + 2);
      String varName = pathUrl.substring(next + 1, close);
      cur = close + 1;
      String value = requestMap.remove(varName);
      if (value == null) {
        throw new IllegalArgumentException(
            "missing required path parameter: " + varName);
      }
      pathBuf.append(CharEscapers.escapeUriPath(value));
    }
    url.appendRawPath(pathBuf.toString());
    // all other parameters are assumed to be query parameters
    url.putAll(requestMap);
    request.url = url;
    return request;
  }
}
