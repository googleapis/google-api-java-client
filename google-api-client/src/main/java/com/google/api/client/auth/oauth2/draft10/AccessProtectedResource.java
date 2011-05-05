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

import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.RefreshTokenGrant;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.GenericData;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Thread-safe OAuth 2.0 (draft 10) method for specifying and refreshing the access token parameter
 * as a request parameter as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-5">Accessing a Protected
 * Resource</a>.
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactoryNoRefresh(HttpTransport transport,
      JsonFactory jsonFactory, AccessTokenResponse accessTokenResponse) {
    return transport.createRequestFactory(new AccessProtectedResource(
        accessTokenResponse.accessToken, Method.AUTHORIZATION_HEADER));
  }

  public static HttpRequestFactory createRequestFactory(HttpTransport transport,
      JsonFactory jsonFactory, AccessTokenResponse accessTokenResponse) {
    return transport.createRequestFactory(new AccessProtectedResource(
        accessTokenResponse.accessToken, Method.AUTHORIZATION_HEADER, transport, jsonFactory,
        "https://server.example.com/authorize", "s6BhdRkqt3", "gX1fBat3bV",
        accessTokenResponse.refreshToken));
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, override {@link #onAccessToken(String)}.
 * </p>
 *
 * <p>
 * If you have a custom request initializer, request execute interceptor, or unsuccessful response
 * handler, take a look at the sample usage for {@link HttpExecuteInterceptor} and
 * {@link HttpUnsuccessfulResponseHandler}, which are interfaces that this class also implements.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.4
 */
public class AccessProtectedResource
    implements HttpExecuteInterceptor, HttpRequestInitializer, HttpUnsuccessfulResponseHandler {

  /** Authorization header prefix. */
  static final String HEADER_PREFIX = "OAuth ";

  static final Logger LOGGER = Logger.getLogger(AccessProtectedResource.class.getName());

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

  /** Lock on the token. */
  private final Lock tokenLock = new ReentrantLock();

  /** Access token or {@code null} for none. */
  private String accessToken;

  /** Method of accessing protected resources. */
  private final Method method;

  /** HTTP transport for executing refresh token request or {@code null} for none. */
  private final HttpTransport transport;

  /** JSON factory to use for parsing response for refresh token request or {@code null} for none. */
  private final JsonFactory jsonFactory;

  /** Encoded authorization server URL or {@code null} for none. */
  private final String authorizationServerUrl;

  /** Client identifier or {@code null} for none. */
  private final String clientId;

  /** Client secret or {@code null} for none. */
  private final String clientSecret;

  /** Refresh token associated with the access token to be refreshed or {@code null} for none. */
  private final String refreshToken;

  /**
   * @param accessToken access token or {@code null} for none (does not call
   *        {@link #setAccessToken(String)})
   * @param method method of accessing protected resources
   */
  public AccessProtectedResource(String accessToken, Method method) {
    this.accessToken = accessToken;
    this.method = Preconditions.checkNotNull(method);
    this.transport = null;
    this.jsonFactory = null;
    this.authorizationServerUrl = null;
    this.clientId = null;
    this.clientSecret = null;
    this.refreshToken = null;
  }

  /**
   * Constructor to use to be able to refresh token when an access token expires.
   *
   * @param accessToken access token or {@code null} for none (does not call
   *        {@link #setAccessToken(String)})
   * @param method method of accessing protected resources
   * @param transport HTTP transport for executing refresh token request
   * @param jsonFactory JSON factory to use for parsing response for refresh token request
   * @param authorizationServerUrl encoded authorization server URL
   * @param clientId client identifier
   * @param clientSecret client secret
   * @param refreshToken refresh token associated with the access token to be refreshed
   */
  public AccessProtectedResource(String accessToken,
      Method method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      String authorizationServerUrl,
      String clientId,
      String clientSecret,
      String refreshToken) {
    this.accessToken = accessToken;
    this.method = Preconditions.checkNotNull(method);
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.authorizationServerUrl = Preconditions.checkNotNull(authorizationServerUrl);
    this.clientId = Preconditions.checkNotNull(clientId);
    this.clientSecret = Preconditions.checkNotNull(clientSecret);
    this.refreshToken = Preconditions.checkNotNull(refreshToken);
  }

  /** Returns the access token or {@code null} for none. */
  public final String getAccessToken() {
    tokenLock.lock();
    try {
      return accessToken;
    } finally {
      tokenLock.unlock();
    }
  }

  /**
   * Sets the access token.
   *
   * @param accessToken access token or {@code null} for none
   */
  public final void setAccessToken(String accessToken) {
    tokenLock.lock();
    try {
      this.accessToken = accessToken;
      onAccessToken(accessToken);
    } finally {
      tokenLock.unlock();
    }
  }

  /** Returns the method of accessing protected resources. */
  public final Method getMethod() {
    return method;
  }

  /** Return the HTTP transport for executing refresh token request or {@code null} for none. */
  public HttpTransport getTransport() {
    return transport;
  }

  /**
   * Returns the JSON factory to use for parsing response for refresh token request or {@code null}
   * for none.
   */
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the encoded authorization server URL or {@code null} for none. */
  public String getAuthorizationServerUrl() {
    return authorizationServerUrl;
  }

  /** Returns the client identifier or {@code null} for none. */
  public String getClientId() {
    return clientId;
  }

  /** Returns the client secret or {@code null} for none. */
  public String getClientSecret() {
    return clientSecret;
  }

  /**
   * Returns the refresh token associated with the access token to be refreshed or {@code null} for
   * none.
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Request a new access token from the authorization endpoint, acquiring a lock on the access
   * token so other threads calling {@link #getAccessToken()} must wait until the new access token
   * has been retrieved.
   *
   * @return whether a new access token was retrieved
   */
  public final boolean refreshToken() throws IOException {
    tokenLock.lock();
    try {
      return executeRefreshToken();
    } finally {
      tokenLock.unlock();
    }
  }

  public final void initialize(HttpRequest request) throws IOException {
    request.interceptor = this;
    request.unsuccessfulResponseHandler = this;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default implementation checks if there is an access token and sets the access token parameter
   * using the appropriate method. Subclasses may override.
   * </p>
   */
  public void intercept(HttpRequest request) throws IOException {
    String accessToken = getAccessToken();
    if (accessToken == null) {
      return;
    }
    switch (method) {
      case AUTHORIZATION_HEADER:
        request.headers.authorization = HEADER_PREFIX + accessToken;
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

  private String getAccessTokenFromRequest(HttpRequest request) {
    switch (method) {
      case AUTHORIZATION_HEADER:
        String header = request.headers.authorization;
        if (header != null && header.startsWith(HEADER_PREFIX)) {
          return header.substring(HEADER_PREFIX.length());
        }
        return null;
      case QUERY_PARAMETER:
        Object param = request.url.get("oauth_token");
        return param == null ? null : param.toString();
      default:
        // URL-encoded content (cast exception if not the right class)
        UrlEncodedContent content = (UrlEncodedContent) request.content;
        // Generic data (cast exception if not the right class)
        GenericData data = (GenericData) content.data;
        Object bodyParam = data.get("oauth_token");
        return bodyParam == null ? null : bodyParam.toString();
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default implementation checks for a 401 error code and calls {@link #refreshToken()}. If
   * {@link #executeRefreshToken()} throws an I/O exception, this implementation will log the
   * exception and return {@code false}. Subclasses may override.
   * </p>
   */
  public boolean handleResponse(
      HttpRequest request, HttpResponse response, boolean retrySupported) {
    if (response.statusCode == 401) {
      try {
        try {
          tokenLock.lock();
          try {
            // need to check if another thread has already refreshed the token
            return !Objects.equal(accessToken, getAccessTokenFromRequest(request)) || refreshToken();
          } finally {
            tokenLock.unlock();
          }
        } catch (HttpResponseException e) {
          LOGGER.severe(e.response.parseAsString());
        }
      } catch (IOException exception) {
        LOGGER.severe(exception.toString());
      }
    }
    return false;
  }

  /**
   * Request a new access token from the authorization endpoint.
   *
   * <p>
   * Default implementation executes the refresh token grant parameter passed to the constructor or
   * {@code false} if it was {@code null}. Subclasses may override. If a new access token was
   * retrieved, implementations must call {@link #setAccessToken(String)}. Implementations can
   * assume proper thread synchronization is already taken care of inside {@link #refreshToken()},
   * where this is called from.
   * </p>
   *
   * @return whether a new access token was retrieved
   * @throws IOException I/O exception
   */
  protected boolean executeRefreshToken() throws IOException {
    if (refreshToken != null) {
      RefreshTokenGrant request = new RefreshTokenGrant(transport,
          jsonFactory,
          authorizationServerUrl,
          clientId,
          clientSecret,
          refreshToken);
      setAccessToken(request.execute().accessToken);
      return true;
    }
    return false;
  }

  /**
   * Notifies of a new access token.
   *
   * <p>
   * Default implementation does nothing, but subclasses may override in order to provide
   * functionality like persisting the access token in a data store. Implementations can assume
   * proper thread synchronization is already taken care of inside {@link #setAccessToken(String)},
   * where this is called from.
   * </p>
   *
   * @param accessToken access token or {@code null} for none
   */
  protected void onAccessToken(String accessToken) {
  }
}
