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
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpTransport;

import java.util.HashSet;

/**
 * HTTP request execute intercepter for Google API's that wraps HTTP requests -- other than GET or
 * POST -- inside of a POST request and uses {@code "X-HTTP-Method-Override"} header to specify the
 * actual HTTP method.
 * <p>
 * It is useful when a firewall only allows the GET and POST methods, or if the underlying HTTP
 * library ({@link LowLevelHttpTransport}) does not support the HTTP method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class MethodOverrideIntercepter implements HttpExecuteIntercepter {

  /**
   * HTTP methods that need to be overridden.
   * <p>
   * Any HTTP method not supported by the low level HTTP transport returned by
   * {@link HttpTransport#useLowLevelHttpTransport()} is automatically added.
   */
  public static final HashSet<String> overriddenMethods = new HashSet<String>();
  static {
    if (!HttpTransport.useLowLevelHttpTransport().supportsPatch()) {
      overriddenMethods.add("PATCH");
    }
    if (!HttpTransport.useLowLevelHttpTransport().supportsHead()) {
      overriddenMethods.add("HEAD");
    }
  }

  public void intercept(HttpRequest request) {
    String method = request.method;
    if (overriddenMethods.contains(method)) {
      request.method = "POST";
      request.headers.set("X-HTTP-Method-Override", method);
    }
  }

  /**
   * Sets this as the first HTTP request execute intercepter for the given HTTP transport.
   */
  public static void setAsFirstFor(HttpTransport transport) {
    transport.removeIntercepters(MethodOverrideIntercepter.class);
    transport.intercepters.add(0, new MethodOverrideIntercepter());
  }
}
