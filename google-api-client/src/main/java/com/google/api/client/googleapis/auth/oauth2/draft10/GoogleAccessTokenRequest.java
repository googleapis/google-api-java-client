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

package com.google.api.client.googleapis.auth.oauth2.draft10;

import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AssertionGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AuthorizationCodeGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.RefreshTokenGrant;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Google extension to the OAuth 2.0 (draft 10) request for an access token.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class GoogleAccessTokenRequest {

  /** Authorization server URL for requesting tokens. */
  public static final String AUTHORIZATION_SERVER_URL =
      "https://accounts.google.com/o/oauth2/token";

  /**
   * Google extension to the OAuth 2.0 Web Server Flow: request an access token based on a
   * verification code.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        GoogleAuthorizationCodeGrant request =
            new GoogleAuthorizationCodeGrant(new NetHttpTransport(),
                new JacksonFactory(),
                "s6BhdRkqt3",
                "gX1fBat3bV",
                "i1WsRn1uB1",
                "https://client.example.com/cb");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class GoogleAuthorizationCodeGrant extends AuthorizationCodeGrant {

    public GoogleAuthorizationCodeGrant() {
      init(this);
    }

    /**
     * @param transport HTTP transport required for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param clientId client identifier
     * @param clientSecret String clientSecret
     * @param code authorization code received from the authorization server
     * @param redirectUri redirection URI used in the initial request
     */
    public GoogleAuthorizationCodeGrant(HttpTransport transport,
        JsonFactory jsonFactory,
        String clientId,
        String clientSecret,
        String code,
        String redirectUri) {
      super(transport,
          jsonFactory,
          AUTHORIZATION_SERVER_URL,
          clientId,
          clientSecret,
          code,
          redirectUri);
      init(this);
    }
  }

  /**
   * Google extension to the OAuth 2.0 request to refresh an access token.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        GoogleRefreshTokenGrant request = new GoogleRefreshTokenGrant(new NetHttpTransport(),
            new JacksonFactory(),
            "s6BhdRkqt3",
            "gX1fBat3bV",
            "n4E9O119d");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class GoogleRefreshTokenGrant extends RefreshTokenGrant {

    public GoogleRefreshTokenGrant() {
      init(this);
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param refreshToken refresh token associated with the access token to be refreshed
     */
    public GoogleRefreshTokenGrant(HttpTransport transport, JsonFactory jsonFactory,
        String clientId, String clientSecret, String refreshToken) {
      super(transport, jsonFactory, AUTHORIZATION_SERVER_URL, clientId, clientSecret, refreshToken);
      init(this);
    }
  }

  /**
   * Google extension to the OAuth 2.0 Assertion Flow: request an access token based on an
   * assertion.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        GoogleAssertionGrant request =
            new GoogleAssertionGrant(new NetHttpTransport(), new JacksonFactory(), "gX1fBat3bV",
                "urn:oasis:names:tc:SAML:2.0:", "PHNhbWxwOl...[omitted for brevity]...ZT4=");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class GoogleAssertionGrant extends AssertionGrant {

    public GoogleAssertionGrant() {
      init(this);
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param assertionType format of the assertion as defined by the authorization server. The
     *        value MUST be an absolute URI
     * @param assertion assertion
     */
    public GoogleAssertionGrant(
        HttpTransport transport, JsonFactory jsonFactory, String assertionType, String assertion) {
      super(transport, jsonFactory, AUTHORIZATION_SERVER_URL, assertionType, assertion);
      init(this);
    }
  }

  /**
   * Initializes the access token request for the Google authorization endpoint.
   *
   * @param request access token request
   */
  static void init(AccessTokenRequest request) {
    request.authorizationServerUrl = AUTHORIZATION_SERVER_URL;
  }

  private GoogleAccessTokenRequest() {
  }
}
