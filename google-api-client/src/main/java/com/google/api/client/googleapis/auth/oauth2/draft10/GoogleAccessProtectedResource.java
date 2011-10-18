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

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleRefreshTokenGrant;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

/**
 * Thread-safe Google extension to the OAuth 2.0 (draft 10) method for specifying and refreshing the
 * access token parameter as a request parameter.
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactoryNoRefresh(HttpTransport transport,
      JsonFactory jsonFactory, AccessTokenResponse accessTokenResponse) {
    return transport.createRequestFactory(new GoogleAccessProtectedResource(
        accessTokenResponse.accessToken));
  }

  public static HttpRequestFactory createRequestFactory(HttpTransport transport,
      JsonFactory jsonFactory, AccessTokenResponse accessTokenResponse) {
    return transport.createRequestFactory(new GoogleAccessProtectedResource(
        accessTokenResponse.accessToken, transport, jsonFactory, "s6BhdRkqt3", "gX1fBat3bV",
        accessTokenResponse.refreshToken));
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, subclass AccessProtectedResource and
 * override {@link #onAccessToken(String)}.
 * </p>
 *
 * <p>
 * If you have a custom request initializer, request execute interceptor, or unsuccessful response
 * handler, take a look at the sample usage for {@link HttpExecuteInterceptor} and
 * {@link HttpUnsuccessfulResponseHandler}, which are interfaces that this class also implements.
 * </p>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class GoogleAccessProtectedResource extends AccessProtectedResource {

  /**
   * @param accessToken access token or {@code null} for none
   */
  public GoogleAccessProtectedResource(String accessToken) {
    super(accessToken, Method.AUTHORIZATION_HEADER);
  }

  /**
   * @param accessToken access token or {@code null} for none
   * @param transport HTTP transport for executing refresh token request
   * @param jsonFactory JSON factory to use for parsing response for refresh token request
   * @param clientId client identifier
   * @param clientSecret client secret
   * @param refreshToken refresh token associated with the access token to be refreshed or
   *        {@code null} for none
   */
  public GoogleAccessProtectedResource(String accessToken,
      HttpTransport transport,
      JsonFactory jsonFactory,
      String clientId,
      String clientSecret,
      String refreshToken) {
    super(accessToken,
        Method.AUTHORIZATION_HEADER,
        transport,
        jsonFactory,
        GoogleAccessTokenRequest.AUTHORIZATION_SERVER_URL,
        clientId,
        clientSecret,
        refreshToken);
  }

  @Override
  protected boolean executeRefreshToken() throws IOException {
    if (getRefreshToken() != null) {
      GoogleRefreshTokenGrant request = new GoogleRefreshTokenGrant(
          getTransport(), getJsonFactory(), getClientId(), getClientSecret(), getRefreshToken());
      return executeAccessTokenRequest(request);
    }
    return false;
  }
}
