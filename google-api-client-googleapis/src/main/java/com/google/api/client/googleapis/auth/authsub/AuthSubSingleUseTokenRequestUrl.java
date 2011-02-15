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

package com.google.api.client.googleapis.auth.authsub;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * Generic URL that builds an AuthSub request URL to retrieve a single-use token. See <a href=
 * "http://code.google.com/apis/accounts/docs/AuthSub.html#AuthSubRequest" >documentation</a>.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AuthSubSingleUseTokenRequestUrl extends GenericUrl {

  /**
   * Generic URL with a token parameter that can be used to extract the AuthSub single-use token
   * from the AuthSubRequest response.
   */
  public static class ResponseUrl extends GenericUrl {

    @Key
    public String token;

    public ResponseUrl(String url) {
      super(url);
    }
  }

  /**
   * (required) URL the user should be redirected to after a successful login. This value should be
   * a page on the web application site, and can include query parameters.
   */
  @Key("next")
  public String nextUrl;

  /**
   * (required) URL identifying the service(s) to be accessed; see documentation for the service for
   * the correct value(s). The resulting token enables access to the specified service(s) only. To
   * specify more than one scope, list each one separated with a space (encodes as "%20").
   */
  @Key
  public String scope;

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

  /**
   * (optional) Boolean flag indicating whether the authorization transaction should issue a secure
   * token (1) or a non-secure token (0). Secure tokens are available to registered applications
   * only.
   */
  @Key
  public int secure;

  /**
   * (optional) Boolean flag indicating whether the one-time-use token may be exchanged for a
   * session token (1) or not (0).
   */
  @Key
  public int session;

  public AuthSubSingleUseTokenRequestUrl() {
    super("https://www.google.com/accounts/AuthSubRequest");
  }
}
