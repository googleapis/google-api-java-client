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

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.util.Collection;

/**
 * Google-specific implementation of the OAuth 2.0 URL builder for an authorization web page to
 * allow the end user to authorize the application to access their protected resources and that
 * returns an authorization code, as specified in <a
 * href="https://developers.google.com/accounts/docs/OAuth2WebServer">Using OAuth 2.0 for Web Server
 * Applications</a>.
 *
 * <p>
 * The default for {@link #getResponseTypes()} is {@code "code"}. Use
 * {@link AuthorizationCodeResponseUrl} to parse the redirect response after the end user
 * grants/denies the request. Using the authorization code in this response, use
 * {@link GoogleAuthorizationCodeTokenRequest} to request the access token.
 * </p>
 *
 * <p>
 * Sample usage for a web application:
 * </p>
 *
 * <pre>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url =
        new GoogleAuthorizationCodeRequestUrl("812741506391.apps.googleusercontent.com",
            "https://oauth2-login-demo.appspot.com/code", Arrays.asList(
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/userinfo.profile")).setState("/profile").build();
    response.sendRedirect(url);
  }
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationCodeRequestUrl extends AuthorizationCodeRequestUrl {

  /**
   * Prompt for consent behavior ({@code "auto"} to request auto-approval or {@code "force"} to
   * force the approval UI to show) or {@code null} for the default behavior.
   */
  @Key("approval_prompt")
  private String approvalPrompt;

  /**
   * Access type ({@code "online"} to request online access or {@code "offline"} to request offline
   * access) or {@code null} for the default behavior.
   */
  @Key("access_type")
  private String accessType;

  /**
   * @param clientId client identifier
   * @param redirectUri URI that the authorization server directs the resource owner's user-agent
   *        back to the client after a successful authorization grant
   * @param scopes scopes (see {@link #setScopes(Collection)})
   *
   * @since 1.15
   */
  public GoogleAuthorizationCodeRequestUrl(
      String clientId, String redirectUri, Collection<String> scopes) {
    this(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId, redirectUri, scopes);
  }

  /**
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param clientId client identifier
   * @param redirectUri URI that the authorization server directs the resource owner's user-agent
   *        back to the client after a successful authorization grant
   * @param scopes scopes (see {@link #setScopes(Collection)})
   *
   * @since 1.15
   */
  public GoogleAuthorizationCodeRequestUrl(String authorizationServerEncodedUrl, String clientId,
      String redirectUri, Collection<String> scopes) {
    super(authorizationServerEncodedUrl, clientId);
    setRedirectUri(redirectUri);
    setScopes(scopes);
  }

  /**
   * @param clientSecrets OAuth 2.0 client secrets JSON model as specified in <a
   *        href="https://developers.google.com/api-client-library/python/guide/aaa_client_secrets">
   *        client_secrets.json file format</a>
   * @param redirectUri URI that the authorization server directs the resource owner's user-agent
   *        back to the client after a successful authorization grant
   * @param scopes scopes (see {@link #setScopes(Collection)})
   *
   * @since 1.15
   */
  public GoogleAuthorizationCodeRequestUrl(
      GoogleClientSecrets clientSecrets, String redirectUri, Collection<String> scopes) {
    this(clientSecrets.getDetails().getClientId(), redirectUri, scopes);
  }

  /**
   * Returns the approval prompt behavior ({@code "auto"} to request auto-approval or
   * {@code "force"} to force the approval UI to show) or {@code null} for the default behavior of
   * {@code "auto"}.
   */
  public final String getApprovalPrompt() {
    return approvalPrompt;
  }

  /**
   * Sets the approval prompt behavior ({@code "auto"} to request auto-approval or {@code "force"}
   * to force the approval UI to show) or {@code null} for the default behavior of {@code "auto"}.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public GoogleAuthorizationCodeRequestUrl setApprovalPrompt(String approvalPrompt) {
    this.approvalPrompt = approvalPrompt;
    return this;
  }

  /**
   * Returns the access type ({@code "online"} to request online access or {@code "offline"} to
   * request offline access) or {@code null} for the default behavior of {@code "online"}.
   */
  public final String getAccessType() {
    return accessType;
  }

  /**
   * Sets the access type ({@code "online"} to request online access or {@code "offline"} to request
   * offline access) or {@code null} for the default behavior of {@code "online"}.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public GoogleAuthorizationCodeRequestUrl setAccessType(String accessType) {
    this.accessType = accessType;
    return this;
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl setResponseTypes(Collection<String> responseTypes) {
    return (GoogleAuthorizationCodeRequestUrl) super.setResponseTypes(responseTypes);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl setRedirectUri(String redirectUri) {
    Preconditions.checkNotNull(redirectUri);
    return (GoogleAuthorizationCodeRequestUrl) super.setRedirectUri(redirectUri);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl setScopes(Collection<String> scopes) {
    Preconditions.checkArgument(scopes.iterator().hasNext());
    return (GoogleAuthorizationCodeRequestUrl) super.setScopes(scopes);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl setClientId(String clientId) {
    return (GoogleAuthorizationCodeRequestUrl) super.setClientId(clientId);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl setState(String state) {
    return (GoogleAuthorizationCodeRequestUrl) super.setState(state);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl set(String fieldName, Object value) {
    return (GoogleAuthorizationCodeRequestUrl) super.set(fieldName, value);
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl clone() {
    return (GoogleAuthorizationCodeRequestUrl) super.clone();
  }
}
