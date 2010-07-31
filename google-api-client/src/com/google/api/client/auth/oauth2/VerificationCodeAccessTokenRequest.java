/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.auth.oauth2;

import com.google.api.client.util.Key;

/**
 * OAuth 2.0 Web Server Flow: request an access token based on a verification
 * code as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-5.1.1"
 * >Verification Code</a>.
 * <p>
 * The {@link #clientId}, {@link #clientSecret}, {@link #code}, and
 * {@link #redirectUri} fields are required. Call {@link #execute()} to execute
 * the request.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>static void requestAccessToken(String code, String redirectUrl)
 *     throws IOException {
 *   try {
 *     VerificationCodeAccessTokenRequest request
 *         = new VerificationCodeAccessTokenRequest();
 *     request.clientId = CLIENT_ID;
 *     request.clientSecret = CLIENT_SECRET;
 *     request.code = code;
 *     request.redirectUri = redirectUrl;
 *     AccessTokenResponse response =
 *         request.execute().parseAs(AccessTokenResponse.class);
 *     System.out.println("Access token: " + response.accessToken);
 *   } catch (HttpResponseException e) {
 *     AccessTokenErrorResponse response =
 *         e.response.parseAs(AccessTokenErrorResponse.class);
 *     System.out.println("Error: " + response.error);
 *   }
 * }</code>
 * </pre>
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public class VerificationCodeAccessTokenRequest extends AccessTokenRequest {

  /**
   * (REQUIRED) The verification code received from the authorization server.
   */
  @Key
  public String code;

  /** (REQUIRED) The redirection URI used in the initial request. */
  @Key("redirect_uri")
  public String redirectUri;

  /**
   * @param encodedAuthorizationServerUrl encoded authorization server URL
   */
  public VerificationCodeAccessTokenRequest(String encodedAuthorizationServerUrl) {
    super(encodedAuthorizationServerUrl);
  }
}
