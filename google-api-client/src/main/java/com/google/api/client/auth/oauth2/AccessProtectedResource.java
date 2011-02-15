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

package com.google.api.client.auth.oauth2;

import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.GenericData;

import java.util.EnumSet;

/**
 * OAuth 2.0 methods for specifying the access token parameter as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5">Accessing a Protected
 * Resource</a>.
 *
 * @author Yaniv Inbar
 * @since 1.2
 */
public final class AccessProtectedResource {

  /**
   * Sets the {@code "Authorization"} header using the given access token for every executed HTTP
   * request for the given HTTP transport.
   * <p>
   * Any existing HTTP request execute intercepters for setting the OAuth 2 access token will be
   * removed.
   * </p>
   *
   * @param transport HTTP transport
   * @param accessToken access token
   */
  public static void usingAuthorizationHeader(HttpTransport transport, String accessToken) {
    new UsingAuthorizationHeader().authorize(transport, accessToken);
  }

  /**
   * Sets the {@code "oauth_token"} URI query parameter using the given access token for every
   * executed HTTP request for the given HTTP transport.
   * <p>
   * Any existing HTTP request execute intercepters for setting the OAuth 2 access token will be
   * removed.
   *
   * @param transport HTTP transport
   * @param accessToken access token
   */
  public static void usingQueryParameter(HttpTransport transport, String accessToken) {
    new UsingQueryParameter().authorize(transport, accessToken);
  }

  /**
   * Sets the {@code "oauth_token"} parameter in the form-encoded HTTP body using the given access
   * token for every executed HTTP request for the given HTTP transport.
   * <p>
   * Any existing HTTP request execute intercepters for setting the OAuth 2 access token will be
   * removed. Requirements:
   * <ul>
   * <li>The HTTP method must be "POST", "PUT", or "DELETE".</li>
   * <li>The HTTP content must be {@code null} or {@link UrlEncodedContent}.</li>
   * <li>The {@link UrlEncodedContent#data} must be {@code null} or {@link GenericData}.</li>
   * </ul>
   *
   * @param transport HTTP transport
   * @param accessToken access token
   */
  public static void usingFormEncodedBody(HttpTransport transport, String accessToken) {
    new UsingFormEncodedBody().authorize(transport, accessToken);
  }

  /**
   * Abstract class to inject an access token parameter for every executed HTTP request .
   */
  static abstract class AccessTokenIntercepter implements HttpExecuteIntercepter {

    /** Access token to use. */
    String accessToken;

    void authorize(HttpTransport transport, String accessToken) {
      this.accessToken = accessToken;
      transport.removeIntercepters(AccessTokenIntercepter.class);
      transport.intercepters.add(this);
    }
  }

  static final class UsingAuthorizationHeader extends AccessTokenIntercepter {

    public void intercept(HttpRequest request) {
      request.headers.authorization = "OAuth " + accessToken;
    }
  }

  static final class UsingQueryParameter extends AccessTokenIntercepter {

    public void intercept(HttpRequest request) {
      request.url.set("oauth_token", accessToken);
    }
  }

  static final class UsingFormEncodedBody extends AccessTokenIntercepter {

    private static final EnumSet<HttpMethod> ALLOWED_METHODS =
        EnumSet.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

    public void intercept(HttpRequest request) {
      if (!ALLOWED_METHODS.contains(request.method)) {
        throw new IllegalArgumentException(
            "expected one of these HTTP methods: " + ALLOWED_METHODS);
      }
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
    }
  }

  private AccessProtectedResource() {
  }
}
