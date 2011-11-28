/*
 * Copyright (c) 2011 Google Inc.
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
import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;
import com.google.common.base.Preconditions;

/**
 * Google-specific implementation of the OAuth 2.0 URL builder for an authorization web page to
 * allow the end user to authorize the application to access their protected resources (as specified
 * in <a href="http://code.google.com/apis/accounts/docs/OAuth2WebServer.html">Using OAuth 2.0 for
 * Web Server Applications</a>).
 *
 * <p>
 * Use {@link AuthorizationCodeResponseUrl} to parse the redirect response after the end user
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
            new GenericUrl("https://oauth2-login-demo.appspot.com/code"))
            .setScopes("https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/userinfo.profile").setState("/profile").build();
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

  /** Encoded URL of Google's end-user authorization server. */
  public static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/auth";

  /**
   * Prompt for consent behavior ({@code "auto"} to request auto-approval or {@code "force"} to
   * force the approval UI to show) or {@code null} for the default behavior of {@code "auto"}.
   */
  @Key("approval_prompt")
  private String approvalPrompt;

  /**
   * Access type ({@code "online"} to request online access or {@code "offline"} to request offline
   * access) or {@code null} for the default behavior of {@code "online"}.
   */
  @Key("access_type")
  private String accessType;

  /**
   * @param clientId client identifier
   * @param redirectUrl URL that the authorization server directs the resource owner's user-agent
   *        back to the client after a successful authorization grant
   */
  public GoogleAuthorizationCodeRequestUrl(String clientId, GenericUrl redirectUrl) {
    super(AUTHORIZATION_SERVER_URL, clientId);
    setRedirectUrl(Preconditions.checkNotNull(redirectUrl));
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
}
