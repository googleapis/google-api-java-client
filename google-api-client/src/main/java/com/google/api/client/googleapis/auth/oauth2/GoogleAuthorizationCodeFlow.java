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
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.util.Collection;

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
 * The default for the {@code approval_prompt} and {@code access_type} parameters is {@code null}.
 * For web applications that means {@code "approval_prompt=auto&access_type=online"} and for
 * installed applications that means {@code "approval_prompt=force&access_type=offline"}. To
 * override the default, you need to explicitly call {@link Builder#setApprovalPrompt(String)} and
 * {@link Builder#setAccessType(String)}.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.7
 */
@SuppressWarnings("deprecation")
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
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param clientId client identifier
   * @param clientSecret client secret
   * @param scopes collection of scopes to be joined by a space separator
   *
   * @since 1.15
   */
  public GoogleAuthorizationCodeFlow(HttpTransport transport, JsonFactory jsonFactory,
      String clientId, String clientSecret, Collection<String> scopes) {
    this(new Builder(transport, jsonFactory, clientId, clientSecret, scopes));
  }

  /**
   * @param builder Google authorization code flow builder
   *
   * @since 1.14
   */
  protected GoogleAuthorizationCodeFlow(Builder builder) {
    super(builder);
    accessType = builder.accessType;
    approvalPrompt = builder.approvalPrompt;
  }

  @Override
  public GoogleAuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
    // don't need to specify clientId & clientSecret because specifying clientAuthentication
    // don't want to specify redirectUri to give control of it to user of this class
    return new GoogleAuthorizationCodeTokenRequest(getTransport(), getJsonFactory(),
        getTokenServerEncodedUrl(), "", "", authorizationCode, "").setClientAuthentication(
        getClientAuthentication())
        .setRequestInitializer(getRequestInitializer()).setScopes(getScopes());
  }

  @Override
  public GoogleAuthorizationCodeRequestUrl newAuthorizationUrl() {
    // don't want to specify redirectUri to give control of it to user of this class
    return new GoogleAuthorizationCodeRequestUrl(
        getAuthorizationServerEncodedUrl(), getClientId(), "", getScopes()).setAccessType(
        accessType).setApprovalPrompt(approvalPrompt);
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
    String approvalPrompt;

    /**
     * Access type ({@code "online"} to request online access or {@code "offline"} to request
     * offline access) or {@code null} for the default behavior.
     */
    String accessType;

    /**
     *
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param scopes collection of scopes to be joined by a space separator (or a single value
     *        containing multiple space-separated scopes)
     *
     * @since 1.15
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String clientId,
        String clientSecret, Collection<String> scopes) {
      super(BearerToken.authorizationHeaderAccessMethod(), transport, jsonFactory, new GenericUrl(
          GoogleOAuthConstants.TOKEN_SERVER_URL), new ClientParametersAuthentication(
          clientId, clientSecret), clientId, GoogleOAuthConstants.AUTHORIZATION_SERVER_URL);
      setScopes(scopes);
    }

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param clientSecrets Google client secrets
     * @param scopes collection of scopes to be joined by a space separator
     *
     * @since 1.15
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory,
        GoogleClientSecrets clientSecrets, Collection<String> scopes) {
      super(BearerToken.authorizationHeaderAccessMethod(), transport, jsonFactory, new GenericUrl(
          GoogleOAuthConstants.TOKEN_SERVER_URL), new ClientParametersAuthentication(
          clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret()),
          clientSecrets.getDetails().getClientId(), GoogleOAuthConstants.AUTHORIZATION_SERVER_URL);
      setScopes(scopes);
    }

    @Override
    public GoogleAuthorizationCodeFlow build() {
      return new GoogleAuthorizationCodeFlow(this);
    }

    @Override
    public Builder setDataStoreFactory(DataStoreFactory dataStore) throws IOException {
      return (Builder) super.setDataStoreFactory(dataStore);
    }

    @Override
    public Builder setCredentialDataStore(DataStore<StoredCredential> typedDataStore) {
      return (Builder) super.setCredentialDataStore(typedDataStore);
    }

    @Override
    public Builder setCredentialCreatedListener(
        CredentialCreatedListener credentialCreatedListener) {
      return (Builder) super.setCredentialCreatedListener(credentialCreatedListener);
    }

    @Beta
    @Override
    @Deprecated
    public Builder setCredentialStore(CredentialStore credentialStore) {
      return (Builder) super.setCredentialStore(credentialStore);
    }

    @Override
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      return (Builder) super.setRequestInitializer(requestInitializer);
    }

    @Override
    public Builder setScopes(Collection<String> scopes) {
      Preconditions.checkState(!scopes.isEmpty());
      return (Builder) super.setScopes(scopes);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setMethod(AccessMethod method) {
      return (Builder) super.setMethod(method);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(transport);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(jsonFactory);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      return (Builder) super.setTokenServerUrl(tokenServerUrl);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      return (Builder) super.setClientAuthentication(clientAuthentication);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setClientId(String clientId) {
      return (Builder) super.setClientId(clientId);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setAuthorizationServerEncodedUrl(String authorizationServerEncodedUrl) {
      return (Builder) super.setAuthorizationServerEncodedUrl(authorizationServerEncodedUrl);
    }

    /**
     * @since 1.11
     */
    @Override
    public Builder setClock(Clock clock) {
      return (Builder) super.setClock(clock);
    }

    @Override
    public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
      return (Builder) super.addRefreshListener(refreshListener);
    }

    @Override
    public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
      return (Builder) super.setRefreshListeners(refreshListeners);
    }

    /**
     * Sets the approval prompt behavior ({@code "auto"} to request auto-approval or {@code "force"}
     * to force the approval UI to show) or {@code null} for the default behavior ({@code "auto"}
     * for web applications and {@code "force"} for installed applications).
     *
     * <p>
     * By default this has the value {@code null}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
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
     * request offline access) or {@code null} for the default behavior ({@code "online"} for web
     * applications and {@code "offline"} for installed applications).
     *
     * <p>
     * By default this has the value {@code null}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
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
