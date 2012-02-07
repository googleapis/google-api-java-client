/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.googleapis.auth.oauth2;

/**
 * Constants for Google's OAuth 2.0 implementation.
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleOAuthConstants {

  /** Encoded URL of Google's end-user authorization server. */
  public static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/auth";

  /** Encoded URL of Google's token server. */
  public static final String TOKEN_SERVER_URL = "https://accounts.google.com/o/oauth2/token";

  /**
   * Redirect URI to use for an installed application as specified in <a
   * href="http://code.google.com/apis/accounts/docs/OAuth2InstalledApp.html">Using OAuth 2.0 for
   * Installed Applications</a>.
   */
  public static final String OOB_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

  private GoogleOAuthConstants() {
  }
}
