/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential.AccessMethod;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.util.Collections;

/**
 * Thread-safe Google OAuth 2.0 authorization code flow that manages and persists end-user
 * credentials.
 *
 * <p>
 * This is designed to simplify the flow in which an end-user authorizes the application to access
 * their protected data, and then the application has access to their data based on an access token
 * and a refresh token to refresh that access token when it expires.
 * </p>
 *
 * <p>
 * The first step is to call {@link #loadCredential(String)} based on the known user ID to check if
 * the end-user's credentials are already known. If not, call {@link #newAuthorizationUrl()} and
 * direct the end-user's browser to an authorization page. The web browser will then redirect to the
 * redirect URL with a {@code "code"} query parameter which can then be used to request an access
 * token using {@link #newTokenRequest(String)}. Finally, use
 * {@link #createAndStoreCredential(TokenResponse, String)} to store and obtain a credential for
 * accessing protected resources.
 * </p>
 *
 * <p>
 * Upgrade warning: prior to version 1.10 the default for the {@code approval_prompt} parameter was
 * {@code "force"}, and the default for the {@code access_type} parameter was {@code "offline"}.
 * However, starting with version 1.10 the default for the {@code approval_prompt} and
 * {@code access_type} parameters and is {@code null}, which means
 * {@code "approval_prompt=auto&access_type=online"} for web applications. To keep the prior
 * behavior, you need to explicitly call {@code setApprovalPrompt("force").setAccesstype("offline")}
 * . See {@link Builder#setApprovalPrompt(String)} and {@link Builder#setAccessType(String)} for
 * more details.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationCodeFlow extends AuthorizationCodeFlow {

  /**
   * Prompt for consent behavior ({@code "auto"} to request auto-approval or {@code "force"} to
   * force the approval UI to show) or {@code null} for the default behavior.
   */
  private final String approvalPrompt;

  /**
   * Access type ({@code "online"} to request online access or {@code "offline"} to request offline
   * access) or {@code null} for the default behavior.
   */
  private final String accessType;

  /**
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken#authorizationHeaderAccessMethod})
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param clientAuthentication client authentication or {@code null} for none (see
   *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
   * @param clientId client identifier
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param credentialStore credential persistence store or {@code null} for none
   * @param requestInitializer HTTP request initializer or {@code null} for none
   * @param scopes space-separated list of scopes or {@code null} for none
   * @param accessType access type ({@code "online"} to request online access or {@code "offline"}
   *        to request offline access) or {@code null} for the default behavior
   * @param approvalPrompt Prompt for consent behavior ({@code "auto"} to request auto-approval or
   *        {@code "force"} to force the approval UI to show) or {@code null} for the default
   *        behavior
   */
  protected GoogleAuthorizationCodeFlow(AccessMethod method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      GenericUrl tokenServerUrl,
      HttpExecuteInterceptor clientAuthentication,
      String clientId,
      String authorizationServerEncodedUrl,
      CredentialStore credentialStore,
      HttpRequestInitializer requestInitializer,
      String scopes,
      String accessType,
      String approvalPrompt) {
    super(method,
        transport,
        jsonFactory,
        tokenServerUrl,
        clientAuthentication,
        clientId,
        authorizationServerEncodedUrl,
        credentialStore,
        requestInitializer,
        scopes);
    this.accessType = accessType;
    this.approvalPrompt = approvalPrompt;
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
    // don't need to specify clientId & clientSecret because specifying clientAuthentication
    // don't want to specify redirectUri to give control of it to user of this class
    return new GoogleAuthorizationCodeTokenRequest(getTransport(),
        getJsonFactory(),
        "",
        "",
        authorizationCode,
        "").setClientAuthentication(getClientAuthentication())
        .setRequestInitializer(getRequestInitializer()).setScopes(getScopes());

  }

  @Override
  public GoogleAuthorizationCodeRequestUrl newAuthorizationUrl() {
    // don't want to specify redirectUri to give control of it to user of this class
    return new GoogleAuthorizationCodeRequestUrl(getClientId(), "", Collections.singleton(
        getScopes())).setAccessType(accessType).setApprovalPrompt(approvalPrompt);
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
   * Returns the access type ({@code "online"} to request online access or {@code "offline"} to
   * request offline access) or {@code null} for the default behavior of {@code "online"}.
   */
  public final String getAccessType() {
    return accessType;
  }

  /**
   * Google authorization code flow builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder extends AuthorizationCodeFlow.Builder {

    /**
     * Prompt for consent behavior ({@code "auto"} to request auto-approval or {@code "force"} to
     * force the approval UI to show) or {@code null} for the default behavior.
     */
    private String approvalPrompt;

    /**
     * Access type ({@code "online"} to request online access or {@code "offline"} to request
     * offline access) or {@code null} for the default behavior.
     */
    private String accessType;

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param scopes list of scopes to be joined by a space separator (or a single value containing
     *        multiple space-separated scopes)
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String clientId,
        String clientSecret, Iterable<String> scopes) {
      super(BearerToken.authorizationHeaderAccessMethod(),
          transport,
          jsonFactory,
          new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
          new ClientParametersAuthentication(clientId, clientSecret),
          clientId,
          GoogleOAuthConstants.AUTHORIZATION_SERVER_URL);
      setScopes(Preconditions.checkNotNull(scopes));
    }

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param clientSecrets Google client secrets
     * @param scopes list of scopes to be joined by a space separator (or a single value containing
     *        multiple space-separated scopes)
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory,
        GoogleClientSecrets clientSecrets, Iterable<String> scopes) {
      super(BearerToken.authorizationHeaderAccessMethod(),
          transport,
          jsonFactory,
          new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
          new ClientParametersAuthentication(clientSecrets.getDetails().getClientId(),
              clientSecrets.getDetails().getClientSecret()),
          clientSecrets.getDetails().getClientId(),
          GoogleOAuthConstants.AUTHORIZATION_SERVER_URL);
      setScopes(Preconditions.checkNotNull(scopes));
    }

    @Override
    public GoogleAuthorizationCodeFlow build() {
      return new GoogleAuthorizationCodeFlow(getMethod(),
          getTransport(),
          getJsonFactory(),
          getTokenServerUrl(),
          getClientAuthentication(),
          getClientId(),
          getAuthorizationServerEncodedUrl(),
          getCredentialStore(),
          getRequestInitializer(),
          getScopes(),
          accessType,
          approvalPrompt);
    }

    @Override
    public Builder setCredentialStore(CredentialStore credentialStore) {
      return (Builder) super.setCredentialStore(credentialStore);
    }

    @Override
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      return (Builder) super.setRequestInitializer(requestInitializer);
    }

    @Override
    public Builder setScopes(Iterable<String> scopes) {
      return (Builder) super.setScopes(scopes);
    }

    @Override
    public Builder setScopes(String... scopes) {
      return (Builder) super.setScopes(scopes);
    }

    /**
     * Sets the approval prompt behavior ({@code "auto"} to request auto-approval or {@code "force"}
     * to force the approval UI to show) or {@code null} for the default behavior of {@code "auto"}.
     *
     * <p>
     * By default this has the value {@code null}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * <p>
     * Upgrade warning: prior to version 1.10 the default was {@code "force"}. However, starting
     * with version 1.10 it is {@code null}, which means {@code "auto"} for web applications. To
     * keep the prior behavior, you need to explicitly call {@code setApprovalPrompt("force")}.
     * </p>
     */
    public Builder setApprovalPrompt(String approvalPrompt) {
      this.approvalPrompt = approvalPrompt;
      return this;
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
     * Sets the access type ({@code "online"} to request online access or {@code "offline"} to
     * request offline access) or {@code null} for the default behavior of {@code "online"}.
     *
     * <p>
     * By default this has the value {@code null}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * <p>
     * Upgrade warning: prior to version 1.10 the default was {@code "offline"}. However, starting
     * with version 1.10 it is {@code null}, which means {@code "online"} for web applications. To
     * keep the prior behavior, you need to explicitly call {@code setAccessType("offline")}.
     * </p>
     */
    public Builder setAccessType(String accessType) {
      this.accessType = accessType;
      return this;
    }

    /**
     * Returns the access type ({@code "online"} to request online access or {@code "offline"} to
     * request offline access) or {@code null} for the default behavior of {@code "online"}.
     */
    public final String getAccessType() {
      return accessType;
    }
  }
}
