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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.util.Collection;

/**
 * Google-specific implementation of the OAuth 2.0 request for an access token based on an
 * authorization code (as specified in <a
 * href="https://developers.google.com/identity/protocols/OAuth2WebServer">Using OAuth 2.0 for Web
 * Server Applications</a>).
 *
 * <p>Use {@link GoogleCredential} to access protected resources from the resource server using the
 * {@link TokenResponse} returned by {@link #execute()}. On error, it will instead throw {@link
 * TokenResponseException}.
 *
 * <p>Sample usage:
 *
 * <pre>
 * static void requestAccessToken() throws IOException {
 *   try {
 *     GoogleTokenResponse response =
 *       new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(),
 *             "812741506391.apps.googleusercontent.com", "{client_secret}",
 *             "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7", "https://oauth2-login-demo.appspot.com/code")
 *       .execute();
 *     System.out.println("Access token: " + response.getAccessToken());
 *   } catch (TokenResponseException e) {
 *     if (e.getDetails() != null) {
 *       System.err.println("Error: " + e.getDetails().getError());
 *       if (e.getDetails().getErrorDescription() != null) {
 *         System.err.println(e.getDetails().getErrorDescription());
 *       }
 *       if (e.getDetails().getErrorUri() != null) {
 *         System.err.println(e.getDetails().getErrorUri());
 *       }
 *     } else {
 *       System.err.println(e.getMessage());
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationCodeTokenRequest extends AuthorizationCodeTokenRequest {

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret
   * @param code authorization code generated by the authorization server
   * @param redirectUri redirect URL parameter matching the redirect URL parameter in the
   *     authorization request (see {@link #setRedirectUri(String)}
   */
  public GoogleAuthorizationCodeTokenRequest(
      HttpTransport transport,
      JsonFactory jsonFactory,
      String clientId,
      String clientSecret,
      String code,
      String redirectUri) {
    this(
        transport,
        jsonFactory,
        GoogleOAuthConstants.TOKEN_SERVER_URL,
        clientId,
        clientSecret,
        code,
        redirectUri);
  }

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerEncodedUrl token server encoded URL
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret
   * @param code authorization code generated by the authorization server
   * @param redirectUri redirect URL parameter matching the redirect URL parameter in the
   *     authorization request (see {@link #setRedirectUri(String)}
   * @since 1.12
   */
  public GoogleAuthorizationCodeTokenRequest(
      HttpTransport transport,
      JsonFactory jsonFactory,
      String tokenServerEncodedUrl,
      String clientId,
      String clientSecret,
      String code,
      String redirectUri) {
    super(transport, jsonFactory, new GenericUrl(tokenServerEncodedUrl), code);
    setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
    setRedirectUri(redirectUri);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setRequestInitializer(
      HttpRequestInitializer requestInitializer) {
    return (GoogleAuthorizationCodeTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (GoogleAuthorizationCodeTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setScopes(Collection<String> scopes) {
    return (GoogleAuthorizationCodeTokenRequest) super.setScopes(scopes);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setGrantType(String grantType) {
    return (GoogleAuthorizationCodeTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setClientAuthentication(
      HttpExecuteInterceptor clientAuthentication) {
    Preconditions.checkNotNull(clientAuthentication);
    return (GoogleAuthorizationCodeTokenRequest)
        super.setClientAuthentication(clientAuthentication);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setCode(String code) {
    return (GoogleAuthorizationCodeTokenRequest) super.setCode(code);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest setRedirectUri(String redirectUri) {
    Preconditions.checkNotNull(redirectUri);
    return (GoogleAuthorizationCodeTokenRequest) super.setRedirectUri(redirectUri);
  }

  @Override
  public GoogleTokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(GoogleTokenResponse.class);
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest set(String fieldName, Object value) {
    return (GoogleAuthorizationCodeTokenRequest) super.set(fieldName, value);
  }
}
