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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;

/**
 * Thread-safe Google-specific implementation of the OAuth 2.0 helper for accessing protected
 * resources using an access token, as well as optionally refreshing the access token when it
 * expires using a refresh token.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactoryWithAccessTokenOnly(HttpTransport transport,
      JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return transport.createRequestFactory(new GoogleCredential(tokenResponse.getAccessToken()));
  }

  public static HttpRequestFactory createRequestFactoryWithRefreshToken(HttpTransport transport,
      JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return transport.createRequestFactory(new GoogleCredential(transport, jsonFactory,
        "s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw", tokenResponse.getRefreshToken(), tokenResponse
            .getAccessToken()));
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, override
 * {@link #onTokenResponse(TokenResponse)} and {@link #onTokenErrorResponse(TokenErrorResponse)}.
 * </p>
 *
 * <p>
 * If you have a custom request initializer, request execute interceptor, or unsuccessful response
 * handler, take a look at the sample usage for {@link HttpExecuteInterceptor} and
 * {@link HttpUnsuccessfulResponseHandler}, which are interfaces that this class also implements.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleCredential extends Credential {

  public GoogleCredential() {
    super(BearerToken.authorizationHeaderAccessMethod());
  }

  /**
   * @param transport HTTP transport for executing refresh token request
   * @param jsonFactory JSON factory to use for parsing response for refresh token request
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret
   */
  public GoogleCredential(HttpTransport transport, JsonFactory jsonFactory, String clientId,
      String clientSecret) {
    super(BearerToken.authorizationHeaderAccessMethod(), transport, jsonFactory, new GenericUrl(
        GoogleOAuthConstants.TOKEN_SERVER_URL), new ClientParametersAuthentication(clientId,
        clientSecret));
  }

  @Override
  public GoogleCredential setAccessToken(String accessToken) {
    return (GoogleCredential) super.setAccessToken(accessToken);
  }

  @Override
  public GoogleCredential setRefreshToken(String refreshToken) {
    return (GoogleCredential) super.setRefreshToken(refreshToken);
  }

  @Override
  public GoogleCredential setExpiresInSeconds(Long expiresIn) {
    return (GoogleCredential) super.setExpiresInSeconds(expiresIn);
  }
}
