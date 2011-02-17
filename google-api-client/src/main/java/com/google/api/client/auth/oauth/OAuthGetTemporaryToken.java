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


/**
 * Generic OAuth 1.0a URL to request a temporary credentials token (or "request token") from an
 * authorization server.
 * <p>
 * Use {@link #execute()} to execute the request. The temporary token acquired with this request is
 * found in {@link OAuthCredentialsResponse#token}. This temporary token is used in
 * {@link OAuthAuthorizeTemporaryTokenUrl#temporaryToken} to direct the end user to an authorization
 * page to allow the end user to authorize the temporary token.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class OAuthGetTemporaryToken extends AbstractOAuthGetToken {

  /**
   * Optional absolute URI back to which the server will redirect the resource owner when the
   * Resource Owner Authorization step is completed or {@code null} for none.
   */
  public String callback;

  /**
   * @param authorizationServerUrl encoded authorization server URL
   */
  public OAuthGetTemporaryToken(String authorizationServerUrl) {
    super(authorizationServerUrl);
  }

  @Override
  public OAuthParameters createParameters() {
    OAuthParameters result = super.createParameters();
    result.callback = callback;
    return result;
  }
}
