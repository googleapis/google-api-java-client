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
 * OAuth 2.0 Username and Password Flow: request an access token based on resource owner credentials
 * used in the as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-5.1.2" >Resource Owner
 * Credentials</a>.
 * <p>
 * The {@link #clientId}, {@link #clientSecret}, {@link #username}, and {@link #password} fields are
 * required. Call {@link #execute()} to execute the request.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>static void requestAccessToken(String username, String password)
 *     throws IOException {
 *   try {
 *     ResourceOwnerCredentialsAccessTokenRequest request
 *         = new ResourceOwnerCredentialsAccessTokenRequest();
 *     request.clientId = CLIENT_ID;
 *     request.clientSecret = CLIENT_SECRET;
 *     request.username = username;
 *     request.password = password;
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
public class ResourceOwnerCredentialsAccessTokenRequest extends AccessTokenRequest {

  /** (REQUIRED) The end-user's username. */
  @Key
  public String username;

  /** (REQUIRED) The end-user's password. */
  public String password;

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
  public ResourceOwnerCredentialsAccessTokenRequest(String encodedAuthorizationServerUrl) {
    super(encodedAuthorizationServerUrl);
  }
}
