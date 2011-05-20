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
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.util.Key;

/**
 * Generic Google OAuth 1.0a URL to request a temporary credentials token (or "request token") from
 * the Google Authorization server.
 * <p>
 * Use {@link #execute()} to execute the request. Google verifies that the requesting application
 * has been registered with Google or is using an approved signature (in the case of installed
 * applications). The temporary token acquired with this request is found in
 * {@link OAuthCredentialsResponse#token} . This temporary token is used in
 * {@link GoogleOAuthAuthorizeTemporaryTokenUrl#temporaryToken} to direct the end user to a Google
 * Accounts web page to allow the end user to authorize the temporary token.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleOAuthGetTemporaryToken extends OAuthGetTemporaryToken {

  /**
   * Optional string identifying the application or {@code null} for none. This string is displayed
   * to end users on Google's authorization confirmation page. For registered applications, the
   * value of this parameter overrides the name set during registration and also triggers a message
   * to the user that the identity can't be verified. For unregistered applications, this parameter
   * enables them to specify an application name, In the case of unregistered applications, if this
   * parameter is not set, Google identifies the application using the URL value of oauth_callback;
   * if neither parameter is set, Google uses the string "anonymous".
   */
  @Key("xoauth_displayname")
  public String displayName;

  /**
   * Required URL identifying the service(s) to be accessed. The resulting token enables access to
   * the specified service(s) only. Scopes are defined by each Google service; see the service's
   * documentation for the correct value. To specify more than one scope, list each one separated
   * with a space.
   */
  @Key
  public String scope;

  public GoogleOAuthGetTemporaryToken() {
    super("https://www.google.com/accounts/OAuthGetRequestToken");
  }

  @Override
  public OAuthParameters createParameters() {
    OAuthParameters result = super.createParameters();
    result.callback = callback;
    return result;
  }
}
