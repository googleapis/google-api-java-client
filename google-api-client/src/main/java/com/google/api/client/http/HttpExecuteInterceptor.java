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

package com.google.api.client.http;

import java.io.IOException;

/**
 * HTTP request execute interceptor to intercept the start of {@link HttpRequest#execute()} before
 * executing the HTTP request.
 *
 * <p>
 * For example, this might be used to sign a request for OAuth:
 * </p>
 *
 * <pre>
  public class OAuthSigner implements HttpExecuteInterceptor {
    public void intercept(HttpRequest request) throws IOException {
      // sign request...
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
    final OAuthSigner signer = new OAuthSigner(...);
    return transport.createRequestFactory(new HttpRequestInitializer() {
      public void initialize(HttpRequest request) {
        request.interceptor = signer;
      }
    });
  }
 * </pre>
 *
 * <p>
 * If you have a custom request execute interceptor, use this more complex example:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    final OAuthSigner signer = new OAuthSigner(...);
    return transport.createRequestFactory(new HttpRequestInitializer() {
      public void initialize(HttpRequest request) {
        request.interceptor = new HttpExecuteInterceptor() {
          public void intercept(HttpRequest request) throws IOException {
            signer.intercept(request);
          }
        };
      }
    });
  }
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface HttpExecuteInterceptor {

  /** Invoked at the start of {@link HttpRequest#execute()} before executing the HTTP request. */
  void intercept(HttpRequest request) throws IOException;
}
