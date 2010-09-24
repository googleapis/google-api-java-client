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


/**
 * OAuth 2.0 Web Server Flow: URL builder for an authorization web page to allow the end user to
 * authorize the application to access their protected resources.
 * <p>
 * The most commonly-set fields are {@link #clientId}, {@link #redirectUri}, and {@link #scope}.
 * After the end-user grants or denies the request, they will be redirected to the
 * {@link #redirectUri} with query parameters set by the authorization server. Use
 * {@link WebServerAuthorizationResponse} to parse the redirect URL.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>@Override
 * public void doGet(HttpServletRequest request, HttpServletResponse response)
 *     throws IOException {
 *   WebServerAuthorizationUrl builder
 *       = new WebServerAuthorizationUrl(BASE_AUTHORIZATION_URL);
 *   builder.clientId = CLIENT_ID;
 *   builder.redirectUri = REDIRECT_URL;
 *   builder.scope = SCOPE;
 *   response.sendRedirect(authorizeUrl.build());
 * }</code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class WebServerAuthorizationRequestUrl extends AbstractAuthorizationRequestUrl {

  public WebServerAuthorizationRequestUrl(String encodedAuthorizationServerUrl) {
    super("web_server", encodedAuthorizationServerUrl);
  }
}
