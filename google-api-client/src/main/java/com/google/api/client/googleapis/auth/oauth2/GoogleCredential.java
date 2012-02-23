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

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;

import java.util.List;

/**
 * Thread-safe Google-specific implementation of the OAuth 2.0 helper for accessing protected
 * resources using an access token, as well as optionally refreshing the access token when it
 * expires using a refresh token.
 *
 * <p>
 * It uses {@link BearerToken#authorizationHeaderAccessMethod()} as the access method,
 * {@link GoogleOAuthConstants#TOKEN_SERVER_URL} as the token server URL, and
 * {@link ClientParametersAuthentication} with the client ID and secret as the client authentication
 * (use {@link Builder#setClientSecrets(GoogleClientSecrets)} or
 * {@link Builder#setClientSecrets(String, String)}).
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactoryWithAccessTokenOnly(
      HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return transport.createRequestFactory(
        new GoogleCredential().setFromTokenResponse(tokenResponse));
  }

  public static HttpRequestFactory createRequestFactoryWithRefreshToken(HttpTransport transport,
      JsonFactory jsonFactory, GoogleClientSecrets clientSecrets, TokenResponse tokenResponse) {
    return transport.createRequestFactory(new GoogleCredential.Builder().setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setClientSecrets(clientSecrets)
        .build()
        .setFromTokenResponse(tokenResponse));
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, use {@link CredentialStore} and
 * {@link Builder#addRefreshListener(CredentialRefreshListener)}.
 * </p>
 *
 * <p>
 * If you have a custom request initializer, request execute interceptor, or unsuccessful response
 * handler, take a look at the sample usage for {@link HttpExecuteInterceptor} and
 * {@link HttpUnsuccessfulResponseHandler}, which are interfaces that this class also implements.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class GoogleCredential extends Credential {

  /**
   * Constructor with the ability to access protected resources, but not refresh tokens.
   *
   * <p>
   * To use with the ability to refresh tokens, use {@link Builder}.
   * </p>
   */
  public GoogleCredential() {
    super(BearerToken.authorizationHeaderAccessMethod());
  }

  /**
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken#authorizationHeaderAccessMethod})
   * @param transport HTTP transport for executing refresh token request or {@code null} if not
   *        refreshing tokens
   * @param jsonFactory JSON factory to use for parsing response for refresh token request or
   *        {@code null} if not refreshing tokens
   * @param tokenServerEncodedUrl encoded token server URL or {@code null} if not refreshing tokens
   * @param clientAuthentication client authentication or {@code null} for none (see
   *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
   * @param requestInitializer HTTP request initializer for refresh token requests to the token
   *        server or {@code null} for none.
   * @param refreshListeners listeners for refresh token results or {@code null} for none
   */
  protected GoogleCredential(AccessMethod method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      String tokenServerEncodedUrl,
      HttpExecuteInterceptor clientAuthentication,
      HttpRequestInitializer requestInitializer,
      List<CredentialRefreshListener> refreshListeners) {
    super(method,
        transport,
        jsonFactory,
        tokenServerEncodedUrl,
        clientAuthentication,
        requestInitializer,
        refreshListeners);
  }

  @Override
  public GoogleCredential setAccessToken(String accessToken) {
    return (GoogleCredential) super.setAccessToken(accessToken);
  }

  @Override
  public GoogleCredential setRefreshToken(String refreshToken) {
    return (GoogleCredential) super.setRefreshToken(refreshToken);
  }

  @Override
  public GoogleCredential setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
    return (GoogleCredential) super.setExpirationTimeMilliseconds(expirationTimeMilliseconds);
  }

  @Override
  public GoogleCredential setExpiresInSeconds(Long expiresIn) {
    return (GoogleCredential) super.setExpiresInSeconds(expiresIn);
  }

  @Override
  public GoogleCredential setFromTokenResponse(TokenResponse tokenResponse) {
    return (GoogleCredential) super.setFromTokenResponse(tokenResponse);
  }

  /**
   * Google credential builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder extends Credential.Builder {

    public Builder() {
      super(BearerToken.authorizationHeaderAccessMethod());
      setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL);
    }

    @Override
    public GoogleCredential build() {
      return new GoogleCredential(getMethod(),
          getTransport(),
          getJsonFactory(),
          getTokenServerUrl() == null ? null : getTokenServerUrl().build(),
          getClientAuthentication(),
          getRequestInitializer(),
          getRefreshListeners());
    }

    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(transport);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(jsonFactory);
    }

    /** Sets the client identifier and secret. */
    public Builder setClientSecrets(String clientId, String clientSecret) {
      setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
      return this;
    }

    /** Sets the client secrets. */
    public Builder setClientSecrets(GoogleClientSecrets clientSecrets) {
      Details details = clientSecrets.getDetails();
      setClientAuthentication(
          new ClientParametersAuthentication(details.getClientId(), details.getClientSecret()));
      return this;
    }

    @Override
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      return (Builder) super.setRequestInitializer(requestInitializer);
    }

    @Override
    public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
      return (Builder) super.addRefreshListener(refreshListener);
    }

    @Override
    public Builder setRefreshListeners(List<CredentialRefreshListener> refreshListeners) {
      return (Builder) super.setRefreshListeners(refreshListeners);
    }

    @Override
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      return (Builder) super.setTokenServerUrl(tokenServerUrl);
    }

    @Override
    public Builder setTokenServerEncodedUrl(String tokenServerEncodedUrl) {
      return (Builder) super.setTokenServerEncodedUrl(tokenServerEncodedUrl);
    }

    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      return (Builder) super.setClientAuthentication(clientAuthentication);
    }
  }
}
