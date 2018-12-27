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

import com.google.api.client.auth.oauth2.BrowserClientRequestUrl;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.util.Collection;

/**
 * Google-specific implementation of the OAuth 2.0 URL builder for an authorization web page to
 * allow the end user to authorize the application to access their protected resources and that
 * returns the access token to a browser client using a scripting language such as JavaScript, as
 * specified in <a href="https://developers.google.com/accounts/docs/OAuth2UserAgent">Using OAuth
 * 2.0 for Client-side Applications</a>.
 *
 * <p>
 * The default for {@link #getResponseTypes()} is {@code "token"}.
 * </p>
 *
 * <p>
 * Sample usage for a web application:
 * </p>
 *
 * <pre>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = new GoogleBrowserClientRequestUrl("812741506391.apps.googleusercontent.com",
        "https://oauth2-login-demo.appspot.com/oauthcallback", Arrays.asList(
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
public class GoogleBrowserClientRequestUrl extends BrowserClientRequestUrl {

  /**
   * Prompt for consent behavior ({@code "auto"} to request auto-approval or {@code "force"} to
   * force the approval UI to show) or {@code null} for the default behavior.
   */
  @Key("approval_prompt")
  private String approvalPrompt;

  /**
   * @param clientId client identifier
   * @param redirectUri URI that the authorization server directs the resource owner's user-agent
   *        back to the client after a successful authorization grant
   * @param scopes scopes (see {@link #setScopes(Collection)})
   *
   * @since 1.15
   */
  public GoogleBrowserClientRequestUrl(
      String clientId, String redirectUri, Collection<String> scopes) {
    super(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId);
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
  public GoogleBrowserClientRequestUrl(
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
  public GoogleBrowserClientRequestUrl setApprovalPrompt(String approvalPrompt) {
    this.approvalPrompt = approvalPrompt;
    return this;
  }

  @Override
  public GoogleBrowserClientRequestUrl setResponseTypes(Collection<String> responseTypes) {
    return (GoogleBrowserClientRequestUrl) super.setResponseTypes(responseTypes);
  }

  @Override
  public GoogleBrowserClientRequestUrl setRedirectUri(String redirectUri) {
    return (GoogleBrowserClientRequestUrl) super.setRedirectUri(redirectUri);
  }

  @Override
  public GoogleBrowserClientRequestUrl setScopes(Collection<String> scopes) {
    Preconditions.checkArgument(scopes.iterator().hasNext());
    return (GoogleBrowserClientRequestUrl) super.setScopes(scopes);
  }

  @Override
  public GoogleBrowserClientRequestUrl setClientId(String clientId) {
    return (GoogleBrowserClientRequestUrl) super.setClientId(clientId);
  }

  @Override
  public GoogleBrowserClientRequestUrl setState(String state) {
    return (GoogleBrowserClientRequestUrl) super.setState(state);
  }

  @Override
  public GoogleBrowserClientRequestUrl set(String fieldName, Object value) {
    return (GoogleBrowserClientRequestUrl) super.set(fieldName, value);
  }

  @Override
  public GoogleBrowserClientRequestUrl clone() {
    return (GoogleBrowserClientRequestUrl) super.clone();
  }
}
