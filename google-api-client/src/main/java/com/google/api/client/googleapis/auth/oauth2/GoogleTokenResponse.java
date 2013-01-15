/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.auth.openidconnect.IdTokenResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * Google OAuth 2.0 JSON model for a successful access token response as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-5.1">Successful Response</a>, including an ID
 * token as specified in <a href="http://openid.net/specs/openid-connect-session-1_0.html">OpenID
 * Connect Session Management 1.0</a>.
 *
 * <p>
 * This response object is the result of {@link GoogleAuthorizationCodeTokenRequest#execute()} and
 * {@link GoogleRefreshTokenRequest#execute()}. Use {@link #parseIdToken()} to parse the
 * {@link GoogleIdToken} and then call
 * {@link GoogleIdTokenVerifier#verify(GoogleIdToken, Collection, Collection)}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleTokenResponse extends IdTokenResponse {

  @Override
  public GoogleTokenResponse setIdToken(String idToken) {
    return (GoogleTokenResponse) super.setIdToken(idToken);
  }

  @Override
  public GoogleTokenResponse setAccessToken(String accessToken) {
    return (GoogleTokenResponse) super.setAccessToken(accessToken);
  }

  @Override
  public GoogleTokenResponse setTokenType(String tokenType) {
    return (GoogleTokenResponse) super.setTokenType(tokenType);
  }

  @Override
  public GoogleTokenResponse setExpiresInSeconds(Long expiresIn) {
    return (GoogleTokenResponse) super.setExpiresInSeconds(expiresIn);
  }

  @Override
  public GoogleTokenResponse setRefreshToken(String refreshToken) {
    return (GoogleTokenResponse) super.setRefreshToken(refreshToken);
  }

  @Override
  public GoogleTokenResponse setScope(String scope) {
    return (GoogleTokenResponse) super.setScope(scope);
  }

  @Override
  public GoogleIdToken parseIdToken() throws IOException {
    return GoogleIdToken.parse(getFactory(), getIdToken());
  }

  /**
   * Verifies the ID token as specified in {@link GoogleIdTokenVerifier#verify} by passing it
   * {@link #parseIdToken}.
   *
   * @param verifier Google ID token verifier
   *
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link GoogleIdTokenVerifier#verify(GoogleIdToken, Collection, Collection)} with
   *             {@link #parseIdToken()}
   */
  @Deprecated
  public boolean verifyIdToken(GoogleIdTokenVerifier verifier)
      throws GeneralSecurityException, IOException {
    return verifier.verify(parseIdToken());
  }

  @Override
  public GoogleTokenResponse set(String fieldName, Object value) {
    return (GoogleTokenResponse) super.set(fieldName, value);
  }
}
