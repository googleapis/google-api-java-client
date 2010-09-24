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

import com.google.api.client.util.Key;

/**
 * OAuth 2.0 Assertion Flow: request an access token based on as assertion as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-5.1.3" >Assertion</a>.
 * <p>
 * The {@link #clientId}, {@link #clientSecret}, {@link #assertionType}, and {@link #assertion}
 * fields are required. Call {@link #execute()} to execute the request.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>static void requestAccessToken(String assertion) throws IOException {
 *   try {
 *     AssertionAccessTokenRequest request
 *         = new AssertionAccessTokenRequest();
 *     request.clientId = CLIENT_ID;
 *     request.clientSecret = CLIENT_SECRET;
 *     request.assertionType = ASSERTION_TYPE;
 *     request.assertion = assertion;
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
public class AssertionAccessTokenRequest extends AccessTokenRequest {

  /**
   * (REQUIRED) The format of the assertion as defined by the authorization server. The value MUST
   * be an absolute URI.
   */
  @Key("assertion_type")
  public String assertionType;

  /** (REQUIRED) The assertion. */
  @Key
  public String assertion;

  /**
   * (OPTIONAL) The scope of the access request expressed as a list of space-delimited strings. The
   * value of the "scope" parameter is defined by the authorization server. If the value contains
   * multiple space-delimited strings, their order does not matter, and each string adds an
   * additional access range to the requested scope.
   */
  public String scope;

  /**
   * @param encodedAuthorizationServerUrl encoded authorization server URL
   */
  public AssertionAccessTokenRequest(String encodedAuthorizationServerUrl) {
    super(encodedAuthorizationServerUrl);
  }
}
