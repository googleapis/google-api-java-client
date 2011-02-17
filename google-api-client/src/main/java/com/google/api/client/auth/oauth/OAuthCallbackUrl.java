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
 * Generic URL that parses the callback URL after a temporary token has been authorized by the end
 * user.
 * <p>
 * The {@link #verifier} is required in order to exchange the authorized temporary token for a
 * long-lived access token in {@link OAuthGetAccessToken#verifier}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class OAuthCallbackUrl extends GenericUrl {

  /** The temporary credentials identifier received from the client. */
  @Key("oauth_token")
  public String token;

  /** The verification code. */
  @Key("oauth_verifier")
  public String verifier;

  public OAuthCallbackUrl(String encodedUrl) {
    super(encodedUrl);
  }
}
