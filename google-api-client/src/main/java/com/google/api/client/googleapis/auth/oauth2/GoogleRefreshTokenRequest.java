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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

/**
 * Google-specific implementation of the OAuth 2.0 request to refresh an access token using a
 * refresh token as specified in <a href="http://tools.ietf.org/html/rfc6749#section-6">Refreshing
 * an Access Token</a>.
 *
 * <p>
 * Use {@link GoogleCredential} to access protected resources from the resource server using the
 * {@link TokenResponse} returned by {@link #execute()}. On error, it will instead throw
 * {@link TokenResponseException}.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static void refreshAccessToken() throws IOException {
    try {
      TokenResponse response =
          new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
              "tGzv3JOkF0XG5Qx2TlKWIA", "s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw").execute();
      System.out.println("Access token: " + response.getAccessToken());
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        System.err.println("Error: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          System.err.println(e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          System.err.println(e.getDetails().getErrorUri());
        }
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleRefreshTokenRequest extends RefreshTokenRequest {

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param refreshToken refresh token issued to the client
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret
   */
  public GoogleRefreshTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
      String refreshToken, String clientId, String clientSecret) {
    super(transport, jsonFactory, new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
        refreshToken);
    setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
  }

  @Override
  public GoogleRefreshTokenRequest setRequestInitializer(
      HttpRequestInitializer requestInitializer) {
    return (GoogleRefreshTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public GoogleRefreshTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (GoogleRefreshTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public GoogleRefreshTokenRequest setScopes(String... scopes) {
    return (GoogleRefreshTokenRequest) super.setScopes(scopes);
  }

  @Override
  public GoogleRefreshTokenRequest setScopes(Iterable<String> scopes) {
    return (GoogleRefreshTokenRequest) super.setScopes(scopes);
  }

  @Override
  public GoogleRefreshTokenRequest setGrantType(String grantType) {
    return (GoogleRefreshTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public GoogleRefreshTokenRequest setClientAuthentication(
      HttpExecuteInterceptor clientAuthentication) {
    return (GoogleRefreshTokenRequest) super.setClientAuthentication(clientAuthentication);
  }

  @Override
  public GoogleRefreshTokenRequest setRefreshToken(String refreshToken) {
    return (GoogleRefreshTokenRequest) super.setRefreshToken(refreshToken);
  }

  @Override
  public GoogleTokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(GoogleTokenResponse.class);
  }

  @Override
  public GoogleRefreshTokenRequest set(String fieldName, Object value) {
    return (GoogleRefreshTokenRequest) super.set(fieldName, value);
  }
}
