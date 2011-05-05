/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.http;

import java.io.IOException;

/**
 * HTTP request initializer.
 *
 * <p>
 * For example, this might be used to disable request timeouts:
 * </p>
 *
 * <pre>
  public class DisableTimeout implements HttpRequestInitializer {
    public void initialize(HttpRequest request) {
      request.connectTimeout = request.readTimeout = 0;
    }
  }
 * </pre>
 *
 * <p>
 * Sample usage with a request factory:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    return transport.createRequestFactory(new DisableTimeout());
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, use this more complex example:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    final DisableTimeout disableTimeout = new DisableTimeout();
    return transport.createRequestFactory(new HttpRequestInitializer() {
      public void initialize(HttpRequest request) {
        disableTimeout.initialize(request);
      }
    });
  }
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public interface HttpRequestInitializer {

  /**
   * Initializes a request.
   *
   * @param request HTTP request
   */
  void initialize(HttpRequest request) throws IOException;
}
