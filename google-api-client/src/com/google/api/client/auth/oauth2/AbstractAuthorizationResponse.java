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

import com.google.api.client.http.UrlEncodedParser;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * OAuth 2.0 parser for the redirect URL after end user grants or denies
 * authorization as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-4.1.1.1"
 * >Authorization Server Response</a>
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractAuthorizationResponse extends GenericData {

  /**
   * (REQUIRED if the end user denies authorization) MUST be set to
   * "user_denied".
   */
  @Key
  public String error;

  /**
   * REQUIRED if the "state" parameter was present in the client authorization
   * request. Set to the exact value received from the client.
   */
  @Key
  public String state;

  /**
   * @param redirectUrl encoded redirect URL
   * @param readQueryParameters {@code true} to read query parameters or {@code
   *        false} to read fragment parameters
   * @throws IllegalArgumentException URI syntax exception
   */
  AbstractAuthorizationResponse(String redirectUrl, boolean readQueryParameters) {
    try {
      URI uri = new URI(redirectUrl);
      String parameters =
          readQueryParameters ? uri.getRawQuery() : uri.getRawFragment();
      UrlEncodedParser.parse(parameters, this);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
