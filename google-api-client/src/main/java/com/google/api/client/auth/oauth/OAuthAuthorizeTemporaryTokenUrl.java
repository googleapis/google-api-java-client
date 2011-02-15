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

package com.google.api.client.auth.oauth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * OAuth 1.0a URL builder for an authorization web page to allow the end user to authorize the
 * temporary token.
 * <p>
 * The {@link #temporaryToken} should be set from the {@link OAuthCredentialsResponse#token}
 * returned by {@link OAuthGetTemporaryToken#execute()}. Use {@link #build()} to build the
 * authorization URL. If a {@link OAuthGetTemporaryToken#callback} was specified, after the end user
 * grants the authorization, the authorization server will redirect to that callback URL. To parse
 * the response, use {@link OAuthCallbackUrl}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class OAuthAuthorizeTemporaryTokenUrl extends GenericUrl {

  /**
   * The temporary credentials token obtained from temporary credentials request in the
   * "oauth_token" parameter. It is found in the {@link OAuthCredentialsResponse#token} returned by
   * {@link OAuthGetTemporaryToken#execute()}.
   */
  @Key("oauth_token")
  public String temporaryToken;

  /**
   * @param encodedUserAuthorizationUrl encoded user authorization URL
   */
  public OAuthAuthorizeTemporaryTokenUrl(String encodedUserAuthorizationUrl) {
    super(encodedUserAuthorizationUrl);
  }
}
