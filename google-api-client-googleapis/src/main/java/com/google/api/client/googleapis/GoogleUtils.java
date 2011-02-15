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

import com.google.api.client.http.HttpTransport;

/**
 * Utilities for the HTTP transport for Google API's.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public final class GoogleUtils {

  /**
   * Routes via HTTP POST all HTTP requests that don't use the GET or POST method to when running
   * behind a firewall that has restrictions on HTTP methods.
   *
   * <p>
   * Specifically, it add a method override intercepter {@link MethodOverrideIntercepter} as the
   * first HTTP execute intercepter. The set of HTTP methods to override via POST may be customized
   * in {@link MethodOverrideIntercepter#override}.
   * </p>
   * Sample usage:
   *
   * <pre>
   * <code>
  static HttpTransport createTransport() {
    HttpTransport transport = new HttpTransport();
    MethodOverrideIntercepter methodOverrider = GoogleUtils.useMethodOverride(transport);
    return transport;
  }
   * </code>
   * </pre>
   *
   * @return HTTP transport
   */
  public static MethodOverrideIntercepter useMethodOverride(HttpTransport transport) {
    transport.removeIntercepters(MethodOverrideIntercepter.class);
    MethodOverrideIntercepter result = new MethodOverrideIntercepter();
    transport.intercepters.add(0, result);
    return result;
  }

  private GoogleUtils() {
  }
}
