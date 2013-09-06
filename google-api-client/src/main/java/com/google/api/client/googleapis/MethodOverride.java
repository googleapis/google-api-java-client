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

import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;

import java.io.IOException;

/**
 * Thread-safe HTTP request execute interceptor for Google API's that wraps HTTP requests inside of
 * a POST request and uses {@link #HEADER} header to specify the actual HTTP method.
 *
 * <p>
 * Use this for example for an HTTP transport that doesn't support PATCH like
 * {@code NetHttpTransport} or {@code UrlFetchTransport}. By default, only the methods not supported
 * by the transport will be overridden. When running behind a firewall that does not support certain
 * verbs like PATCH, use the {@link MethodOverride.Builder#setOverrideAllMethods(boolean)}
 * constructor instead to specify to override all methods. POST is never overridden.
 * </p>
 *
 * <p>
 * This class also allows GET requests with a long URL (> 2048 chars) to be instead sent using
 * method override as a POST request.
 * </p>
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    return transport.createRequestFactory(new MethodOverride());
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, take a look at the sample usage for
 * {@link HttpExecuteInterceptor}, which this class also implements.
 * </p>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public final class MethodOverride implements HttpExecuteInterceptor, HttpRequestInitializer {

  /**
   * Name of the method override header.
   *
   * @since 1.13
   */
  public static final String HEADER = "X-HTTP-Method-Override";

  /** Maximum supported URL length. */
  static final int MAX_URL_LENGTH = 2048;

  /**
   * Whether to allow all methods (except GET and POST) to be overridden regardless of whether the
   * transport supports them.
   */
  private final boolean overrideAllMethods;

  /** Only overrides HTTP methods that the HTTP transport does not support. */
  public MethodOverride() {
    this(false);
  }

  MethodOverride(boolean overrideAllMethods) {
    this.overrideAllMethods = overrideAllMethods;
  }

  public void initialize(HttpRequest request) {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    if (overrideThisMethod(request)) {
      String requestMethod = request.getRequestMethod();
      request.setRequestMethod(HttpMethods.POST);
      request.getHeaders().set(HEADER, requestMethod);
      if (requestMethod.equals(HttpMethods.GET)) {
        // take the URI query part and put it into the HTTP body
        request.setContent(new UrlEncodedContent(request.getUrl().clone()));
        // remove query parameters from URI
        request.getUrl().clear();
      } else if (request.getContent() == null) {
        // Google servers will fail to process a POST unless the Content-Length header is specified
        request.setContent(new EmptyContent());
      }
    }
  }

  private boolean overrideThisMethod(HttpRequest request) throws IOException {
    String requestMethod = request.getRequestMethod();
    if (requestMethod.equals(HttpMethods.POST)) {
      return false;
    }
    if (requestMethod.equals(HttpMethods.GET)
        ? request.getUrl().build().length() > MAX_URL_LENGTH : overrideAllMethods) {
      return true;
    }
    return !request.getTransport().supportsMethod(requestMethod);
  }

  /**
   * Builder for {@link MethodOverride}.
   *
   * @since 1.12
   * @author Yaniv Inbar
   */
  public static final class Builder {

    /**
     * Whether to allow all methods (except GET and POST) to be overridden regardless of whether the
     * transport supports them.
     */
    private boolean overrideAllMethods;

    /** Builds the {@link MethodOverride}. */
    public MethodOverride build() {
      return new MethodOverride(overrideAllMethods);
    }

    /**
     * Returns whether to allow all methods (except GET and POST) to be overridden regardless of
     * whether the transport supports them.
     */
    public boolean getOverrideAllMethods() {
      return overrideAllMethods;
    }

    /**
     * Sets whether to allow all methods (except GET and POST) to be overridden regardless of
     * whether the transport supports them.
     *
     * <p>
     * Default is {@code false}.
     * </p>
     */
    public Builder setOverrideAllMethods(boolean overrideAllMethods) {
      this.overrideAllMethods = overrideAllMethods;
      return this;
    }
  }
}
