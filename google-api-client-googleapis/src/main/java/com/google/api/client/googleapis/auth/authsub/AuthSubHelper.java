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

package com.google.api.client.googleapis.auth.authsub;

import com.google.api.client.googleapis.auth.AuthKeyValueParser;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.security.PrivateKey;

/**
 * AuthSub token manager for a single user.
 * <p>
 * To properly initialize, set:
 * <ul>
 * <li>{@link #setToken}: single-use or session token (required)</li>
 * <li>{@link #transport}: Google transport (recommended)</li>
 * <li>{@link #privateKey}: private key for secure AuthSub (recommended)</li>
 * </ul>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AuthSubHelper {

  /** Private key for secure AuthSub or {@code null} for non-secure AuthSub. */
  private PrivateKey privateKey;

  /**
   * Google transport whose authorization header to set or {@code null} to ignore (for example if
   * using an alternative HTTP library).
   */
  private HttpTransport transport;

  /**
   * HTTP transport required for AuthSub requests.
   *
   * @since 1.3
   */
  public HttpTransport authSubTransport;

  /** Token (may be single use or session). */
  private String token;

  public AuthSubHelper() {
    authSubTransport.addParser(AuthKeyValueParser.INSTANCE);
  }

  /**
   * Key/value data to parse a success response for an AuthSubSessionToken request.
   */
  public static final class SessionTokenResponse {

    @Key("Token")
    public String sessionToken;
  }

  /**
   * Key/value data to parse a success response for an AuthSubTokenInfo request.
   */
  public static final class TokenInfoResponse {

    @Key("Secure")
    public boolean secure;

    @Key("Target")
    public String target;

    @Key("Scope")
    public String scope;
  }

  /**
   * Sets to the given private key for secure AuthSub or {@code null} for non-secure AuthSub.
   * <p>
   * Updates the authorization header of the Google transport (set using
   * {@link #setTransport(HttpTransport)}).
   */
  public void setPrivateKey(PrivateKey privateKey) {
    if (privateKey != this.privateKey) {
      this.privateKey = privateKey;
      updateAuthorizationHeaders();
    }
  }

  /**
   * Sets to the given Google transport whose authorization header to set or {@code null} to ignore
   * (for example if using an alternative HTTP library).
   * <p>
   * Updates the authorization header of the Google transport.
   */
  public void setTransport(HttpTransport transport) {
    if (transport != this.transport) {
      this.transport = transport;
      updateAuthorizationHeaders();
    }
  }

  /**
   * Sets to the given single-use or session token (or resets any existing token if {@code null}).
   * <p>
   * Any previous stored single-use or session token will be forgotten. Updates the authorization
   * header of the Google transport (set using {@link #setTransport(HttpTransport)}).
   */
  public void setToken(String token) {
    if (token != this.token) {
      this.token = token;
      updateAuthorizationHeaders();
    }
  }

  /**
   * Exchanges the single-use token for a session token as described in <a href=
   * "http://code.google.com/apis/accounts/docs/AuthSub.html#AuthSubSessionToken"
   * >AuthSubSessionToken</a>. Sets the authorization header of the Google transport using the
   * session token, and automatically sets the token used by this instance using
   * {@link #setToken(String)}.
   * <p>
   * Note that Google allows at most 10 session tokens per use per web application, so the session
   * token for each user must be persisted.
   *
   * @return session token
   * @throws HttpResponseException if the authentication response has an error code
   * @throws IOException some other kind of I/O exception
   */
  public String exchangeForSessionToken() throws IOException {
    HttpTransport authSubTransport = this.authSubTransport;
    HttpRequest request = authSubTransport.buildGetRequest();
    request.setUrl("https://www.google.com/accounts/AuthSubSessionToken");
    SessionTokenResponse sessionTokenResponse =
        request.execute().parseAs(SessionTokenResponse.class);
    String sessionToken = sessionTokenResponse.sessionToken;
    setToken(sessionToken);
    return sessionToken;
  }

  /**
   * Revokes the session token. Clears any existing authorization header of the Google transport and
   * automatically resets the token by calling {@code setToken(null)}.
   * <p>
   * See <a href= "http://code.google.com/apis/accounts/docs/AuthSub.html#AuthSubRevokeToken"
   * >AuthSubRevokeToken</a> for protocol details.
   *
   * @throws HttpResponseException if the authentication response has an error code
   * @throws IOException some other kind of I/O exception
   */
  public void revokeSessionToken() throws IOException {
    HttpTransport authSubTransport = this.authSubTransport;
    HttpRequest request = authSubTransport.buildGetRequest();
    request.setUrl("https://www.google.com/accounts/AuthSubRevokeToken");
    request.execute().ignore();
    setToken(null);
  }

  /**
   * Retries the token information as described in <a href=
   * "http://code.google.com/apis/accounts/docs/AuthSub.html#AuthSubTokenInfo"
   * >AuthSubTokenInfo</a>.
   *
   * @throws HttpResponseException if the authentication response has an error code
   * @throws IOException some other kind of I/O exception
   */
  public TokenInfoResponse requestTokenInfo() throws IOException {
    HttpTransport authSubTransport = this.authSubTransport;
    HttpRequest request = authSubTransport.buildGetRequest();
    request.setUrl("https://www.google.com/accounts/AuthSubTokenInfo");
    HttpResponse response = request.execute();
    if (response.getParser() == null) {
      throw new IllegalStateException(response.parseAsString());
    }
    return response.parseAs(TokenInfoResponse.class);
  }

  /** Updates the authorization headers. */
  private void updateAuthorizationHeaders() {
    HttpTransport transport = this.transport;
    if (transport != null) {
      setAuthorizationHeaderOf(transport);
    }
    HttpTransport authSubTransport = this.authSubTransport;
    if (authSubTransport != null) {
      setAuthorizationHeaderOf(authSubTransport);
    }
  }

  /**
   * Sets the authorization header for the given HTTP transport based on the current token.
   */
  private void setAuthorizationHeaderOf(HttpTransport transoprt) {
    transport.intercepters.add(new AuthSubIntercepter(token, privateKey));
  }
}
