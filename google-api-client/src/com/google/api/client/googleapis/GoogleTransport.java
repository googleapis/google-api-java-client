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

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpTransport;

/**
 * HTTP transport for Google API's. It's only purpose is to allow for method
 * overriding when the firewall does not accept DELETE, PATCH or PUT methods.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleTransport {

  /**
   * Creates and returns a new HTTP transport with basic default behaviors for
   * working with Google API's.
   * <p>
   * Includes:
   * <ul>
   * <li>Setting the {@link HttpTransport#defaultHeaders} to a new instance of
   * {@link GoogleHeaders}.</li>
   * <li>Adding a {@link MethodOverrideIntercepter} as the first HTTP execute
   * intercepter to use HTTP method override for unsupported HTTP methods (calls
   * {@link MethodOverrideIntercepter#setAsFirstFor(HttpTransport)}.</li>
   * </ul>
   * <p>
   * Sample usage:
   * 
   * <pre>
   * <code>
  static HttpTransport createTransport() {
    HttpTransport transport = GoogleTransport.create();
    GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
    headers.setApplicationName("acme-rocket-2");
    headers.gdataVersion = "2";
    return transport;
  }
   * </code>
   * </pre>
   * 
   * @return HTTP transport
   */
  public static HttpTransport create() {
    HttpTransport transport = new HttpTransport();
    MethodOverrideIntercepter.setAsFirstFor(transport);
    transport.defaultHeaders = new GoogleHeaders();
    return transport;
  }

  private GoogleTransport() {
  }
}
