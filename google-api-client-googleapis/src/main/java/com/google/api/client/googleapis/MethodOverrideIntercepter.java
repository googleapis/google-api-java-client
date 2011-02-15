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

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;

import java.util.EnumSet;

/**
 * HTTP request execute intercepter for Google API's that wraps HTTP requests -- other than GET or
 * POST -- inside of a POST request and uses {@code "X-HTTP-Method-Override"} header to specify the
 * actual HTTP method.
 * <p>
 * It is useful when a firewall only allows the GET and POST methods, or if the underlying HTTP
 * library ({@link HttpTransport}) does not support the HTTP method.
 * </p>
 * <p>
 * Upgrade warning: prior to version 1.3, there was a global static field {@code overriddenMethods}
 * that allowed the set of HTTP methods to override to be specified globally. Now, it must be
 * specified for each instance of this class via {@link #override}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class MethodOverrideIntercepter implements HttpExecuteIntercepter {

  /**
   * HTTP methods supported by the HTTP transport that nevertheless need to be overridden.
   * <p>
   * Any HTTP method not supported by the HTTP transport is automatically overridden, so it doesn't
   * matter if those HTTP methods are specified here. By default, the methods DELETE, HEAD, PATCH,
   * and PUT are all overridden. GET and POST are never overridden.
   * </p>
   *
   * @since 1.3
   */
  public EnumSet<HttpMethod> override =
      EnumSet.of(HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.PUT);

  // TODO(yanivi): or should we be less conservative and not override PUT, PATCH, DELETE, and HEAD?

  public void intercept(HttpRequest request) {
    if (overrideThisMethod(request)) {
      HttpMethod method = request.method;
      request.method = HttpMethod.POST;
      request.headers.set("X-HTTP-Method-Override", method.name());
      // Google servers will fail to process a POST without the Content-Length header
      if (request.content == null) {
        InputStreamContent content = new InputStreamContent();
        content.setByteArrayInput(new byte[0]);
        request.content = content;
      }
    }
  }

  private boolean overrideThisMethod(HttpRequest request) {
    HttpMethod method = request.method;
    if (method != HttpMethod.GET && method != HttpMethod.POST && override.contains(method)) {
      return true;
    }
    switch (method) {
      case PATCH:
        return !request.transport.supportsPatch();
      case HEAD:
        return !request.transport.supportsHead();
    }
    return false;
  }

  /**
   * Sets this as the first HTTP request execute intercepter for the given HTTP transport.
   *
   * @deprecated (scheduled to be removed in 1.4) Use
   *             {@link GoogleUtils#useMethodOverride(HttpTransport)}.
   */
  @Deprecated
  public static void setAsFirstFor(HttpTransport transport) {
    transport.removeIntercepters(MethodOverrideIntercepter.class);
    transport.intercepters.add(0, new MethodOverrideIntercepter());
  }
}
