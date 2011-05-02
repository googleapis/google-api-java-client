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

package com.google.api.client.auth.oauth2.draft10;

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.EnumSet;

/**
 * OAuth 2.0 (draft 10) methods for specifying the access token parameter as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5">Accessing a Protected
 * Resource</a>.
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    return transport.createRequestFactory(new AccessProtectedResource(...));
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, take a look at the sample usage for
 * {@link HttpExecuteInterceptor}, which this class also implements.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.4
 */
public final class AccessProtectedResource
    implements HttpExecuteInterceptor, HttpRequestInitializer {

  /**
   * Method of accessing protected resources.
   * <p>
   * The only method required to be implemented by the specification is
   * {@link #AUTHORIZATION_HEADER}.
   * </p>
   */
  public enum Method {
    /**
     * Uses the "Authorization" header, as specified in <a
     * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5.1.1">Section 5.1.1</a>.
     */
    AUTHORIZATION_HEADER,

    /**
     * Uses the query parameter, as specified in <a
     * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5.1.2">Section 5.1.2</a>.
     */
    QUERY_PARAMETER,

    /**
     * Uses a form-encoded body parameter, as specified in <a
     * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5.1.3">Section 5.1.3</a>.
     */
    FORM_ENCODED_BODY
  }

  private static final EnumSet<HttpMethod> ALLOWED_METHODS =
      EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

  // TODO(yanivi): use a read-write lock on the access token instead?

  /** Access token. */
  private volatile String accessToken;

  /** Method of accessing protected resources. */
  private final Method method;

  /**
   * Uses the default method {@link Method#AUTHORIZATION_HEADER}.
   *
   * @param accessToken access token
   */
  public AccessProtectedResource(String accessToken) {
    this(accessToken, Method.AUTHORIZATION_HEADER);
  }

  /**
   * @param accessToken access token
   * @param method method of accessing protected resources
   */
  public AccessProtectedResource(String accessToken, Method method) {
    setAccessToken(accessToken);
    this.method = method;
  }

  /** Returns the access token. */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token.
   *
   * @param accessToken access token
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = Preconditions.checkNotNull(accessToken);
  }

  public void initialize(HttpRequest request) throws IOException {
    request.interceptor = this;
  }

  public void intercept(HttpRequest request) throws IOException {
    switch (method) {
      case AUTHORIZATION_HEADER:
        request.headers.authorization = "OAuth " + accessToken;
        break;
      case QUERY_PARAMETER:
        request.url.set("oauth_token", accessToken);
        break;
      case FORM_ENCODED_BODY:
        Preconditions.checkArgument(ALLOWED_METHODS.contains(request.method),
            "expected one of these HTTP methods: %s", ALLOWED_METHODS);
        // URL-encoded content (cast exception if not the right class)
        UrlEncodedContent content = (UrlEncodedContent) request.content;
        if (content == null) {
          content = new UrlEncodedContent();
          request.content = content;
        }
        // Generic data (cast exception if not the right class)
        GenericData data = (GenericData) content.data;
        if (data == null) {
          data = new GenericData();
          content.data = data;
        }
        data.put("oauth_token", accessToken);
        break;
    }
  }
}
