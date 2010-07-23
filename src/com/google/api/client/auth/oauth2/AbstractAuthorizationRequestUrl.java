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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * OAuth 2.0 URL builder for an authorization web page to allow the end user to
 * authorize the application to access their protected resources as specified in
 * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-4.1.1"
 * >User-Agent Flow</a>.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractAuthorizationRequestUrl extends GenericUrl {

  /**
   * Determines how the authorization server delivers the authorization response
   * back to the client.
   */
  @Key
  public final String type;

  /** (REQUIRED) The client identifier. */
  @Key("client_id")
  public String clientId;

  /**
   * REQUIRED unless a redirection URI has been established between the client
   * and authorization server via other means. An absolute URI to which the
   * authorization server will redirect the user-agent to when the end-user
   * authorization step is completed. The authorization server SHOULD require
   * the client to pre-register their redirection URI. Authorization servers MAY
   * restrict the redirection URI to not include a query component as defined by
   * [RFC3986] section 3.
   */
  @Key("redirect_uri")
  public String redirectUri;

  /**
   * (OPTIONAL) An opaque value used by the client to maintain state between the
   * request and callback. The authorization server includes this value when
   * redirecting the user-agent back to the client.
   */
  @Key
  public String state;

  /**
   * (OPTIONAL) The scope of the access request expressed as a list of
   * space-delimited strings. The value of the "scope" parameter is defined by
   * the authorization server. If the value contains multiple space-delimited
   * strings, their order does not matter, and each string adds an additional
   * access range to the requested scope.
   */
  @Key
  public String scope;

  AbstractAuthorizationRequestUrl(String type,
      String encodedAuthorizationServerUrl) {
    super(encodedAuthorizationServerUrl);
    this.type = type;
  }
}
