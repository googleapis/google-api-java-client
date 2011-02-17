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

package com.google.api.client.googleapis.auth.oauth;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;

/**
 * Generic Google OAuth 1.0a URL to request to exchange the temporary credentials token (or "request
 * token") for a long-lived credentials token (or "access token") from the Google Authorization
 * server.
 * <p>
 * Use {@link #execute()} to execute the request. The long-lived access token acquired with this
 * request is found in {@link OAuthCredentialsResponse#token} . This token must be stored. It may
 * then be used to authorize HTTP requests to protected resources in Google services by setting the
 * {@link OAuthParameters#token}, and invoking
 * {@link OAuthParameters#signRequestsUsingAuthorizationHeader(HttpTransport)}.
 * <p>
 * To revoke the stored access token, use {@link #revokeAccessToken}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleOAuthGetAccessToken extends OAuthGetAccessToken {

  public GoogleOAuthGetAccessToken() {
    super("https://www.google.com/accounts/OAuthGetAccessToken");
  }

  /**
   * Revokes the long-lived access token.
   *
   * <p>
   * Upgrade warning: prior to version 1.3, there was no {@code transport} parameter, but now it is
   * required.
   * </p>
   *
   * @param parameters OAuth parameters
   * @throws IOException I/O exception
   * @since 1.3
   */
  public static void revokeAccessToken(HttpTransport transport, OAuthParameters parameters)
      throws IOException {
    parameters.signRequestsUsingAuthorizationHeader(transport);
    HttpRequest request = transport.buildGetRequest();
    request.setUrl("https://www.google.com/accounts/AuthSubRevokeToken");
    request.execute().ignore();
  }
}
