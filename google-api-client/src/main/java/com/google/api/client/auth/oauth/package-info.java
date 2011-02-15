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
 * OAuth 1.0 authorization as specified in <a href="http://tools.ietf.org/html/rfc5849">RFC 5849:
 * The OAuth 1.0 Protocol</a> (see detailed package specification).
 *
 * <p>
 * There are a few features not supported by this implementation:
 * </p>
 * <ul>
 * <li>{@code PLAINTEXT} signature algorithm</li>
 * <li>{@code "application/x-www-form-urlencoded"} HTTP request body</li>
 * <li>{@code "oauth_*"} parameters specified in the HTTP request URL (instead assumes they are
 * specified in the {@code Authorization} header)</li>
 * </ul>
 *
 * <p>
 * Before using this library, you may need to set up your application as follows:
 * </p>
 * <ol>
 * <li>For web applications, you may need to first register your application with the authorization
 * server. It may provide two pieces of information you need:
 * <ul>
 * <li>OAuth Consumer Key: use this as the {@code consumerKey} on every OAuth request, for example
 * in {@link com.google.api.client.auth.oauth.AbstractOAuthGetToken#consumerKey}.</li>
 * <li>OAuth Consumer Secret: use this as the
 * {@link com.google.api.client.auth.oauth.OAuthHmacSigner#clientSharedSecret} when using the {@code
 * "HMAC-SHA1"} signature method.</li>
 * </ul>
 * </li>
 * <li>For an installed application, an unregistered web application, or a web application running
 * on localhost, you must use the {@code "HMAC-SHA1"} signature method. The documentation for the
 * authorization server will need to provide you with the {@code consumerKey} and {@code
 * clientSharedSecret} to use.</li>
 * <li>For the {@code "HMAC-SHA1"} signature method, use
 * {@link com.google.api.client.auth.oauth.OAuthHmacSigner}.</li>
 * <li>For the {@code "RSA-SHA1"} signature method, use
 * {@link com.google.api.client.auth.oauth.OAuthRsaSigner}.</li>
 * </ol>
 * <p>
 * After the set up has been completed, the typical application flow is:
 * </p>
 * <ol>
 * <li>Request a temporary credentials token from the Authorization server using
 * {@link com.google.api.client.auth.oauth.OAuthGetTemporaryToken}. A callback URL should be
 * specified for web applications, but does not need to be specified for installed applications.
 * </li>
 * <li>Direct the end user to an authorization web page to allow the end user to authorize the
 * temporary token using using
 * {@link com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl}.</li>
 * <li>After the user has granted the authorization:
 * <ul>
 * <li>For web applications, the user's browser will be redirected to the callback URL which may be
 * parsed using {@link com.google.api.client.auth.oauth.OAuthCallbackUrl}.</li>
 * <li>For installed applications, see the authorization server's documentation for figuring out the
 * verification code.</li>
 * </ul>
 * </li>
 * <li>Request to exchange the temporary token for a long-lived access token from the Authorization
 * server using {@link com.google.api.client.auth.oauth.OAuthGetAccessToken}. This access token must
 * be stored.</li>
 * <li>Use the stored access token to authorize HTTP requests to protected resources by setting the
 * {@link com.google.api.client.auth.oauth.OAuthParameters#token} and invoking
 * {@link com.google.api.client.auth.oauth.OAuthParameters#signRequestsUsingAuthorizationHeader}.
 * </li>
 * </ol>
 *
 *
 * <p>
 * <b>Warning: this package is experimental, and its content may be changed in incompatible ways or
 * possibly entirely removed in a future version of the library</b>
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */

package com.google.api.client.auth.oauth;

