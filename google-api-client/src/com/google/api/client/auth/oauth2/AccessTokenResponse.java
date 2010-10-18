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

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

/**
 * OAuth 2.0 access token success response content as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.2">Access Token Response</a>.
 * <p>
 * Use {@link AccessProtectedResource} to authorize executed HTTP requests based on the
 * {@link #accessToken}, for example {@link AccessProtectedResource}.{@link
 * AccessProtectedResource#usingAuthorizationHeader(com.google.api.client.http.HttpTransport,
 * String) usingAuthorizationHeader}{@code (transport, response.accessToken)}.
 * </p>
 *
 * @since 1.2
 * @author Yaniv Inbar
 */
public class AccessTokenResponse extends GenericData {

  /** (REQUIRED) The access token issued by the authorization server. */
  @Key("access_token")
  public String accessToken;

  /**
   * (OPTIONAL) The duration in seconds of the access token lifetime. For example, the value "3600"
   * denotes that the access token will expire in one hour from the time the response was generated
   * by the authorization server.
   */
  @Key("expires_in")
  public Long expiresIn;

  /**
   * (OPTIONAL) The refresh token used to obtain new access tokens. The authorization server SHOULD
   * NOT issue a refresh token when the access grant type is set to "none".
   */
  @Key("refresh_token")
  public String refreshToken;

  /**
   * (OPTIONAL) The scope of the access token as a list of space- delimited strings. The value of
   * the "scope" parameter is defined by the authorization server. If the value contains multiple
   * space-delimited strings, their order does not matter, and each string adds an additional access
   * range to the requested scope. The authorization server SHOULD include the parameter if the
   * requested scope is different from the one requested by the client.
   */
  @Key
  public String scope;
}
