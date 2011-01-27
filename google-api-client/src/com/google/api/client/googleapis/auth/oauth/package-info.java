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
 * Google's additions to OAuth 1.0a authorization as specified in <a
 * href="http://code.google.com/apis/accounts/docs/OAuth_ref.html">Google's OAuth API Reference </a>
 * (see detailed package specification).
 *
 * <h2>Package Specification</h2>
 *
 * <p>
 * Before using this library, you need to set up your application as follows:
 * </p>
 * <ol>
 * <li>For a web application, you should first register your application at the <a
 * href="https://www.google.com/accounts/ManageDomains">Manage Your Domains</a> page. See detailed
 * instructions at the <a
 * href="http://code.google.com/apis/accounts/docs/RegistrationForWebAppsAuto.html">registration
 * page</a>. Take note of the following OAuth information you will need:
 * <ul>
 * <li>OAuth Consumer Key (same as your appspot domain): use this as the {@code consumerKey} on
 * every OAuth request, for example in
 * {@link com.google.api.client.auth.oauth.AbstractOAuthGetToken#consumerKey}.</li>
 * <li>OAuth Consumer Secret: use this as the
 * {@link com.google.api.client.auth.oauth.OAuthHmacSigner}.
 * {@link com.google.api.client.auth.oauth.OAuthHmacSigner#clientSharedSecret clientSharedSecret}
 * when using the {@code "HMAC-SHA1"} signature method.</li>
 * <li>Upload new X.509 cert: See the instructions for <a
 * href="http://code.google.com/apis/gdata/docs/auth/oauth.html#GeneratingKeyCert">generating a
 * self-signing private key and public certificate</a>. Use
 * {@link com.google.api.client.auth.oauth.OAuthRsaSigner} for this {@code "RSA-SHA1"} signature
 * method.
 * <ul>
 * <li>Example for generating private key and public certificate:
 *
 * <pre><code>
# Generate the RSA keys and certificate
keytool -genkey -v -alias Example -keystore ./Example.jks\
  -keyalg RSA -sigalg SHA1withRSA\
  -dname "CN=myappname.appspot.com, OU=Engineering, O=My_Company, L=Mountain  View, ST=CA, C=US"\
  -storepass changeme -keypass changeme
# Output the public certificate to a file
keytool -export -rfc -keystore ./Example.jks -storepass changeme \
  -alias Example -file mycert.pem</code></pre></li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>For an installed application, an unregistered web application, or a web application running
 * on localhost, you must use the {@code "HMAC-SHA1"} signature method. Use {@code "anonymous"} for
 * the {@code consumerKey} and {@code clientSharedSecret}.</li>
 * </ol>
 * <p>
 * After the set up has been completed, the typical application flow is:
 * </p>
 * <ol>
 * <li>Request a temporary credentials token ("request token") from the Google Authorization server
 * using {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken}. A
 * callback URL should be specified for web applications, but does not need to be specified for
 * installed applications.</li>
 * <li>Direct the end user to a Google Accounts web page to allow the end user to authorize the
 * temporary token using using
 * {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthAuthorizeTemporaryTokenUrl}.</li>
 * <li>After the user has granted the authorization:
 * <ul>
 * <li>For web applications, the user's browser will be redirected to the callback URL which may be
 * parsed using {@link com.google.api.client.auth.oauth.OAuthCallbackUrl}.</li>
 * <li>For installed applications, use {@code ""} for the verification code.</li>
 * </ul>
 * </li>
 * <li>Request to exchange the temporary token for a long-lived access token from the Google
 * Authorization server using
 * {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken}. This access token
 * must be stored.</li>
 * <li>Use the stored access token to authorize HTTP requests to protected resources in Google
 * services by setting the {@link com.google.api.client.auth.oauth.OAuthParameters#token} and
 * invoking
 * {@link com.google.api.client.auth.oauth.OAuthParameters#signRequestsUsingAuthorizationHeader}.
 * </li>
 * <li>For 2-legged OAuth, use
 * {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthDomainWideDelegation} as a request
 * execute intercepter to set the e-mail address of the user on every HTTP request, or
 * {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthDomainWideDelegation.Url} as a
 * generic URL builder with the requestor ID parameter.</li>
 * <li>To revoke an access token, use
 * {@link com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken#revokeAccessToken}.
 * Users can also manually revoke tokens from Google's <a
 * href="https://www.google.com/accounts/IssuedAuthSubTokens">change authorized websites</a> page.
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

package com.google.api.client.googleapis.auth.oauth;

