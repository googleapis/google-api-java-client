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

package com.google.api.client.auth.oauth2;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * OAuth 2.0 URL builder for an authorization web page to allow the end user to authorize the
 * application to access their protected resources as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-3">Obtaining End-User
 * Authorization</a>.
 * <p>
 * Use {@link AuthorizationResponse} to parse the redirect response after the end user grants/denies
 * the request.
 * </p>
 * <p>
 * Sample usage for a web application:
 *
 * <pre>
 * <code>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    AuthorizationRequestUrl result = new AuthorizationRequestUrl(BASE_AUTHORIZATION_URL);
    AuthorizationRequestUrl.ResponseType.CODE.set(result);
    result.clientId = CLIENT_ID;
    result.redirectUri = REDIRECT_URL;
    result.scope = SCOPE;
    response.sendRedirect(result.build());
    return;
  }
 * </code>
 * </pre>
 *
 * @since 1.2
 * @author Yaniv Inbar
 */
public class AuthorizationRequestUrl extends GenericUrl {

  /**
   * Response type enumeration that may be used for setting the
   * {@link AuthorizationRequestUrl#responseType}.
   * <p>
   * Call {@link #set(AuthorizationRequestUrl)} to set the response type on an authorization request
   * URL.
   * </p>
   */
  public enum ResponseType {

    /** Authorization code. */
    CODE,

    /** Access token. */
    TOKEN,

    /** Authorization code and access token. */
    CODE_AND_TOKEN;

    /**
     * Sets the response type on an authorization request URL.
     *
     * @param url authorization request URL
     */
    public void set(AuthorizationRequestUrl url) {
      url.responseType = this.toString().toLowerCase();
    }
  }

  /**
   * (REQUIRED) The requested response: an access token, an authorization code, or both. The
   * parameter value MUST be set to "token" for requesting an access token, "code" for requesting an
   * authorization code, or "code_and_token" to request both. The authorization server MAY decline
   * to provide one or more of these response types. For convenience, you may use
   * {@link ResponseType} to set this value.
   * <p>
   * By default, the response type is {@code "code"}, but this may be overridden.
   * </p>
   * <p>
   * Upgrade warning: in prior version 1.2 of the library, the default value was {@code null}. It is
   * now {@code "code"}.
   * </p>
   */
  @Key("response_type")
  public String responseType = "code";

  /** (REQUIRED) The client identifier. */
  @Key("client_id")
  public String clientId;

  /**
   * (REQUIRED, unless a redirection URI has been established between the client and authorization
   * server via other means) An absolute URI to which the authorization server will redirect the
   * user-agent to when the end-user authorization step is completed. The authorization server
   * SHOULD require the client to pre-register their redirection URI.
   */
  @Key("redirect_uri")
  public String redirectUri;

  /**
   * (OPTIONAL) The scope of the access request expressed as a list of space-delimited strings. The
   * value of the "scope" parameter is defined by the authorization server. If the value contains
   * multiple space-delimited strings, their order does not matter, and each string adds an
   * additional access range to the requested scope.
   */
  @Key
  public String scope;

  /**
   * (OPTIONAL) An opaque value used by the client to maintain state between the request and
   * callback. The authorization server includes this value when redirecting the user-agent back to
   * the client.
   */
  @Key
  public String state;

  /**
   * @param encodedAuthorizationServerUrl encoded authorization server URL
   */
  public AuthorizationRequestUrl(String encodedAuthorizationServerUrl) {
    super(encodedAuthorizationServerUrl);
  }
}
