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
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Thread-safe HTTP request execute interceptor for Google API's that wraps HTTP requests -- other
 * than GET or POST -- inside of a POST request and uses {@code "X-HTTP-Method-Override"} header to
 * specify the actual HTTP method.
 *
 * <p>
 * Use this for an HTTP transport that doesn't support PATCH like {@code NetHttpTransport} or
 * {@code UrlFetchTransport}. By default, only the methods not supported by the transport will be
 * overridden. When running behind a firewall that does not support certain verbs like PATCH, use
 * the {@link MethodOverride.Builder#setOverrideAllMethods(boolean)} constructor instead to specify
 * to override all methods. GET and POST are never overridden.
 * </p>
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
@SuppressWarnings("deprecation")
public final class MethodOverride implements HttpExecuteInterceptor, HttpRequestInitializer {

  /** HTTP methods supported by the HTTP transport that nevertheless need to be overridden. */
  private final EnumSet<HttpMethod> override;

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
    this(overrideAllMethods, EnumSet.noneOf(HttpMethod.class));
  }

  MethodOverride(boolean overrideAllMethods, EnumSet<HttpMethod> override) {
    this.overrideAllMethods = overrideAllMethods;
    this.override = override.clone();
  }

  /**
   * Specifies the HTTP methods to override.
   *
   * @param override HTTP methods supported by the HTTP transport that nevertheless need to be
   *        overridden
   * @deprecated (scheduled to be removed in 1.13) Use
   *             {@link MethodOverride.Builder#setOverrideAllMethods(boolean)} instead
   */
  @Deprecated
  public MethodOverride(EnumSet<HttpMethod> override) {
    this(false, override);
  }

  public void initialize(HttpRequest request) {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    if (overrideThisMethod(request)) {
      String requestMethod = request.getRequestMethod();
      request.setRequestMethod(HttpMethods.POST);
      request.getHeaders().set("X-HTTP-Method-Override", requestMethod);
      // Google servers will fail to process a POST unless the Content-Length header is specified
      if (request.getContent() == null) {
        request.setContent(new EmptyContent());
      }
    }
  }

  private boolean overrideThisMethod(HttpRequest request) throws IOException {
    String requestMethod = request.getRequestMethod();
    boolean supportsMethod = request.getTransport().supportsMethod(requestMethod);
    if (requestMethod.equals(HttpMethods.GET) || requestMethod.equals(HttpMethods.POST)) {
      Preconditions.checkArgument(supportsMethod);
      return false;
    }
    if (overrideAllMethods || override.contains(request.getMethod())) {
      return true;
    }
    if (requestMethod.equals("PATCH")) {
      return !request.getTransport().supportsPatch();
    }
    if (requestMethod.equals(HttpMethods.HEAD)) {
      return !request.getTransport().supportsHead();
    }
    return !supportsMethod;
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
