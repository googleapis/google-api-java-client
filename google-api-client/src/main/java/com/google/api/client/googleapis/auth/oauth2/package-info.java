/*
 * Copyright 2011 Google Inc.
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

/**
 * Google's additions to OAuth 2.0 authorization as specified in <a
 * href="https://developers.google.com/identity/protocols/OAuth2">Using OAuth 2.0 to Access Google
 * APIs</a>.
 *
 * <p>
 * Before using this library, you must register your application at the <a
 * href="https://cloud.google.com/compute/docs/console">APIs Console</a>. The result of this
 * registration process is a set of values that are known to both Google and your application, such
 * as the "Client ID", "Client Secret", and "Redirect URIs".
 * </p>
 *
 * <p>
 * These are the typical steps of the web server flow based on an authorization code, as specified
 * in <a href="https://developers.google.com/identity/protocols/OAuth2WebServer">Using OAuth 2.0 for
 * Web Server Applications</a>:
 * <ul>
 * <li>Redirect the end user in the browser to the authorization page using
 * {@link com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl} to grant
 * your application access to the end user's protected data.</li>
 * <li>Process the authorization response using
 * {@link com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl} to parse the authorization
 * code.</li>
 * <li>Request an access token and possibly a refresh token using
 * {@link com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest}.</li>
 * <li>Access protected resources using
 * {@link com.google.api.client.googleapis.auth.oauth2.GoogleCredential}. Expired access tokens will
 * automatically be refreshed using the refresh token (if applicable).</li>
 * </ul>
 * </p>
 *
 * <p>
 * These are the typical steps of the the browser-based client flow specified in <a
 * href="https://developers.google.com/identity/protocols/OAuth2UserAgent">Using OAuth 2.0 for
 * Client-side Applications</a>:
 * <ul>
 * <li>Redirect the end user in the browser to the authorization page using
 * {@link com.google.api.client.googleapis.auth.oauth2.GoogleBrowserClientRequestUrl} to grant your
 * browser application access to the end user's protected data.</li>
 * <li>Use the <a href="https://github.com/google/google-api-javascript-client">Google API Client
 * library for JavaScript</a> to process the access token found in the URL fragment at the redirect
 * URI registered at the <a href="https://cloud.google.com/compute/docs/consoles">APIs Console</a>.
 * </li>
 * </ul>
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */

package com.google.api.client.googleapis.auth.oauth2;

