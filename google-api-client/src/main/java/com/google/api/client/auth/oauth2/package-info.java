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

/**
 * OAuth 2.0 authorization as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10">The OAuth 2.0 Protocol
 * (draft-ietf-oauth-v2-10)</a> (see detailed package specification).
 *
 * <p>
 * Before using this library, you may need to register your application with the authorization
 * server to receive a client ID and client secret.
 * </p>
 *
 * <p>
 * Typical steps for the OAuth 2 authorization flow:
 * <ul>
 * <li>Redirect end user in the browser to the authorization page using
 * {@link com.google.api.client.auth.oauth2.AuthorizationRequestUrl} to grant your application
 * access to their protected data.</li>
 * <li>Process the authorization response using
 * {@link com.google.api.client.auth.oauth2.AuthorizationResponse} to parse the authorization code
 * and/or access token.</li>
 * <li>Request an access token, depending on the access grant type:
 * <ul>
 * <li>Authorization code:
 * {@link com.google.api.client.auth.oauth2.AccessTokenRequest.AuthorizationCodeGrant}</li>
 * <li>Resource Owner Password Credentials: {@link
 * com.google.api.client.auth.oauth2.AccessTokenRequest.ResourceOwnerPasswordCredentialsGrant}</li>
 * <li>Assertion: {@link com.google.api.client.auth.oauth2.AccessTokenRequest.AssertionGrant}</li>
 * <li>Refresh Token: {@link com.google.api.client.auth.oauth2.AccessTokenRequest.RefreshTokenGrant}
 * </li>
 * <li>None (e.g. client owns protected resource):
 * {@link com.google.api.client.auth.oauth2.AccessTokenRequest}</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Warning: this package is experimental, and its content may be changed in incompatible ways or
 * possibly entirely removed in a future version of the library</b>
 * </p>
 *
 * @since 1.2
 * @author Yaniv Inbar
 */

package com.google.api.client.auth.oauth2;

