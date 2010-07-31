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
 * OAuth 2.0 User Agent Flow: parses the redirect URL fragment after end user
 * grants or denies authorization as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-4.1.1.1"
 * >Authorization Server Response</a>
 * <p>
 * Use {@link AccessProtectedResource} to authorize executed HTTP requests based
 * on the {@link #accessToken}.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>static void processRedirectUrl(HttpTransport transport, String redirectUrl)
 *     throws URISyntaxException {
 *   UserAgentAuthorizationResponse response
 *       = new UserAgentAuthorizationResponse(redirectUrl);
 *   if (response.error != null) {
 *     throw new RuntimeException("Authorization denied");
 *   }
 *   AccessProtectedResource.usingAuthorizationHeader(transport,
 *       response.accessToken);
 * }
 * </code>
 * </pre>
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public class UserAgentAuthorizationResponse extends
    AbstractAuthorizationResponse {


  /** (REQUIRED if the end user grants authorization) The access token. */
  @Key("access_token")
  public String accessToken;

  /**
   * (OPTIONAL) The duration in seconds of the access token lifetime.
   */
  @Key("expires_in")
  public Long expiresIn;

  /** (OPTIONAL) The refresh token. */
  @Key("refresh_token")
  public String refreshToken;

  /**
   * @param redirectUrl encoded redirect URL
   * @throws IllegalArgumentException URI syntax exception
   */
  public UserAgentAuthorizationResponse(String redirectUrl) {
    super(redirectUrl, false);
  }
}
