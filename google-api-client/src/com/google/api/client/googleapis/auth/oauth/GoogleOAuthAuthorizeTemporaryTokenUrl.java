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

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCallbackUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.util.Key;

/**
 * Google OAuth 1.0a URL builder for a Google Accounts web page to allow the end user to authorize
 * the temporary token.
 * <p>
 * This only supports Google API's that use {@code
 * "https://www.google.com/accounts/OAuthAuthorizeToken"} for authorizing temporary tokens.
 * </p>
 * <p>
 * The {@link #temporaryToken} should be set from the {@link OAuthCredentialsResponse#token}
 * returned by {@link GoogleOAuthGetTemporaryToken#execute()}. Use {@link #build()} to build the
 * authorization URL. If a {@link OAuthGetTemporaryToken#callback} was specified, after the end user
 * grants the authorization, the Google authorization server will redirect to that callback URL. To
 * parse the response, use {@link OAuthCallbackUrl}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleOAuthAuthorizeTemporaryTokenUrl extends OAuthAuthorizeTemporaryTokenUrl {

  /**
   * Optionally use {@code "mobile"} to for a mobile version of the approval page or {@code null}
   * for normal.
   */
  @Key("btmpl")
  public String template;

  /**
   * Optional value identifying a particular Google Apps (hosted) domain account to be accessed (for
   * example, 'mycollege.edu') or {@code null} or {@code "default"} for a regular Google account
   * ('username@gmail.com').
   */
  @Key("hd")
  public String hostedDomain;

  /**
   * Optional ISO 639 country code identifying what language the approval page should be translated
   * in (for example, 'hl=en' for English) or {@code null} for the user's selected language.
   */
  @Key("hl")
  public String language;

  public GoogleOAuthAuthorizeTemporaryTokenUrl() {
    super("https://www.google.com/accounts/OAuthAuthorizeToken");
  }
}
