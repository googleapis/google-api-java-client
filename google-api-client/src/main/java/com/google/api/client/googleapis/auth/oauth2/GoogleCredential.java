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
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.PemReader;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Collections;

/**
 * Thread-safe Google-specific implementation of the OAuth 2.0 helper for accessing protected
 * resources using an access token, as well as optionally refreshing the access token when it
 * expires using a refresh token.
 *
 * <p>
 * There are three modes supported: access token only, refresh token flow, and service account flow
 * (with or without impersonating a user).
 * </p>
 *
 * <p>
 * If all you have is an access token, you simply pass the {@link TokenResponse} to the credential
 * using {@link Builder#setFromTokenResponse(TokenResponse)}. Google credential uses
 * {@link BearerToken#authorizationHeaderAccessMethod()} as the access method. Sample usage:
 * </p>
 *
 * <pre>
  public static GoogleCredential createCredentialWithAccessTokenOnly(TokenResponse tokenResponse) {
    return new GoogleCredential().setFromTokenResponse(tokenResponse);
  }
 * </pre>
 *
 * <p>
 * If you have a refresh token, it is similar to the case of access token only, but you additionally
 * need to pass the credential the client secrets using
 * {@link Builder#setClientSecrets(GoogleClientSecrets)} or
 * {@link Builder#setClientSecrets(String, String)}. Google credential uses
 * {@link GoogleOAuthConstants#TOKEN_SERVER_URL} as the token server URL, and
 * {@link ClientParametersAuthentication} with the client ID and secret as the client
 * authentication. Sample usage:
 * </p>
 *
 * <pre>
  public static GoogleCredential createCredentialWithRefreshToken(HttpTransport transport,
      JsonFactory jsonFactory, GoogleClientSecrets clientSecrets, TokenResponse tokenResponse) {
    return new GoogleCredential.Builder().setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setClientSecrets(clientSecrets)
        .build()
        .setFromTokenResponse(tokenResponse);
  }
 * </pre>
 *
 * <p>
 * The <a href="https://developers.google.com/accounts/docs/OAuth2ServiceAccount">service account
 * flow</a> is used when you want to access data owned by your client application. You download the
 * private key in a {@code .p12} file from the Google APIs Console. Use
 * {@link Builder#setServiceAccountId(String)},
 * {@link Builder#setServiceAccountPrivateKeyFromP12File(File)}, and
 * {@link Builder#setServiceAccountScopes(Collection)}. Sample usage:
 * </p>
 *
 * <pre>
  public static GoogleCredential createCredentialForServiceAccount(
      HttpTransport transport,
      JsonFactory jsonFactory,
      String serviceAccountId,
      Collection&lt;String&gt; serviceAccountScopes,
      File p12File) throws GeneralSecurityException, IOException {
    return new GoogleCredential.Builder().setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setServiceAccountId(serviceAccountId)
        .setServiceAccountScopes(serviceAccountScopes)
        .setServiceAccountPrivateKeyFromP12File(p12File)
        .build();
  }
 * </pre>
 *
 * <p>
 * You can also use the service account flow to impersonate a user in a domain that you own. This is
 * very similar to the service account flow above, but you additionally call
 * {@link Builder#setServiceAccountUser(String)}. Sample usage:
 * </p>
 *
 * <pre>
  public static GoogleCredential createCredentialForServiceAccountImpersonateUser(
      HttpTransport transport,
      JsonFactory jsonFactory,
      String serviceAccountId,
      Collection&lt;String&gt; serviceAccountScopes,
      File p12File,
      String serviceAccountUser) throws GeneralSecurityException, IOException {
    return new GoogleCredential.Builder().setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setServiceAccountId(serviceAccountId)
        .setServiceAccountScopes(serviceAccountScopes)
        .setServiceAccountPrivateKeyFromP12File(p12File)
        .setServiceAccountUser(serviceAccountUser)
        .build();
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, use {@link DataStoreFactory} and
 * {@link Builder#addRefreshListener(CredentialRefreshListener)} with
 * {@link DataStoreCredentialRefreshListener}.
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
   * Service account ID (typically an e-mail address) or {@code null} if not using the service
   * account flow.
   */
  private String serviceAccountId;

  /**
   * Collection of OAuth scopes to use with the the service account flow or {@code null} if not
   * using the service account flow.
   */
  private Collection<String> serviceAccountScopes;

  /**
   * Private key to use with the the service account flow or {@code null} if not using the service
   * account flow.
   */
  private PrivateKey serviceAccountPrivateKey;

  /**
   * Email address of the user the application is trying to impersonate in the service account flow
   * or {@code null} for none or if not using the service account flow.
   */
  private String serviceAccountUser;

  /**
   * Constructor with the ability to access protected resources, but not refresh tokens.
   *
   * <p>
   * To use with the ability to refresh tokens, use {@link Builder}.
   * </p>
   */
  public GoogleCredential() {
    this(new Builder());
  }

  /**
   * @param builder Google credential builder
   *
   * @since 1.14
   */
  protected GoogleCredential(Builder builder) {
    super(builder);
    if (builder.serviceAccountPrivateKey == null) {
      Preconditions.checkArgument(builder.serviceAccountId == null
          && builder.serviceAccountScopes == null && builder.serviceAccountUser == null);
    } else {
      serviceAccountId = Preconditions.checkNotNull(builder.serviceAccountId);
      serviceAccountScopes = Collections.unmodifiableCollection(builder.serviceAccountScopes);
      serviceAccountPrivateKey = builder.serviceAccountPrivateKey;
      serviceAccountUser = builder.serviceAccountUser;
    }
  }

  @Override
  public GoogleCredential setAccessToken(String accessToken) {
    return (GoogleCredential) super.setAccessToken(accessToken);
  }

  @Override
  public GoogleCredential setRefreshToken(String refreshToken) {
    if (refreshToken != null) {
      Preconditions.checkArgument(
          getJsonFactory() != null && getTransport() != null && getClientAuthentication() != null,
          "Please use the Builder and call setJsonFactory, setTransport and setClientSecrets");
    }
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

  @Override
  @Beta
  protected TokenResponse executeRefreshToken() throws IOException {
    if (serviceAccountPrivateKey == null) {
      return super.executeRefreshToken();
    }
    // service accounts: no refresh token; instead use private key to request new access token
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("RS256");
    header.setType("JWT");
    JsonWebToken.Payload payload = new JsonWebToken.Payload();
    long currentTime = getClock().currentTimeMillis();
    payload.setIssuer(serviceAccountId);
    payload.setAudience(getTokenServerEncodedUrl());
    payload.setIssuedAtTimeSeconds(currentTime / 1000);
    payload.setExpirationTimeSeconds(currentTime / 1000 + 3600);
    payload.setSubject(serviceAccountUser);
    payload.put("scope", Joiner.on(' ').join(serviceAccountScopes));
    try {
      String assertion = JsonWebSignature.signUsingRsaSha256(
          serviceAccountPrivateKey, getJsonFactory(), header, payload);
      TokenRequest request = new TokenRequest(
          getTransport(), getJsonFactory(), new GenericUrl(getTokenServerEncodedUrl()),
          "urn:ietf:params:oauth:grant-type:jwt-bearer");
      request.put("assertion", assertion);
      return request.execute();
    } catch (GeneralSecurityException exception) {
      IOException e = new IOException();
      e.initCause(exception);
      throw e;
    }
  }

  /**
   * {@link Beta} <br/>
   * Returns the service account ID (typically an e-mail address) or {@code null} if not using the
   * service account flow.
   */
  @Beta
  public final String getServiceAccountId() {
    return serviceAccountId;
  }

  /**
   * {@link Beta} <br/>
   * Returns a collection of OAuth scopes to use with the the service account flow or {@code null}
   * if not using the service account flow.
   */
  @Beta
  public final Collection<String> getServiceAccountScopes() {
    return serviceAccountScopes;
  }

  /**
   * {@link Beta} <br/>
   * Returns the space-separated OAuth scopes to use with the the service account flow or
   * {@code null} if not using the service account flow.
   *
   * @since 1.15
   */
  @Beta
  public final String getServiceAccountScopesAsString() {
    return serviceAccountScopes == null ? null : Joiner.on(' ').join(serviceAccountScopes);
  }

  /**
   * {@link Beta} <br/>
   * Returns the private key to use with the the service account flow or {@code null} if not using
   * the service account flow.
   */
  @Beta
  public final PrivateKey getServiceAccountPrivateKey() {
    return serviceAccountPrivateKey;
  }

  /**
   * {@link Beta} <br/>
   * Returns the email address of the user the application is trying to impersonate in the service
   * account flow or {@code null} for none or if not using the service account flow.
   */
  @Beta
  public final String getServiceAccountUser() {
    return serviceAccountUser;
  }

  /**
   * Google credential builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder extends Credential.Builder {

    /** Service account ID (typically an e-mail address) or {@code null} for none. */
    String serviceAccountId;

    /**
     * Collection of OAuth scopes to use with the the service account flow or {@code null} for none.
     */
    Collection<String> serviceAccountScopes;

    /** Private key to use with the the service account flow or {@code null} for none. */
    PrivateKey serviceAccountPrivateKey;

    /**
     * Email address of the user the application is trying to impersonate in the service account
     * flow or {@code null} for none.
     */
    String serviceAccountUser;

    public Builder() {
      super(BearerToken.authorizationHeaderAccessMethod());
      setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL);
    }

    @Override
    public GoogleCredential build() {
      return new GoogleCredential(this);
    }

    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(transport);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(jsonFactory);
    }

    /**
     * @since 1.9
     */
    @Override
    public Builder setClock(Clock clock) {
      return (Builder) super.setClock(clock);
    }

    /**
     * Sets the client identifier and secret.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setClientSecrets(String clientId, String clientSecret) {
      setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
      return this;
    }

    /**
     * Sets the client secrets.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setClientSecrets(GoogleClientSecrets clientSecrets) {
      Details details = clientSecrets.getDetails();
      setClientAuthentication(
          new ClientParametersAuthentication(details.getClientId(), details.getClientSecret()));
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Returns the service account ID (typically an e-mail address) or {@code null} for none.
     */
    @Beta
    public final String getServiceAccountId() {
      return serviceAccountId;
    }

    /**
     * {@link Beta} <br/>
     * Sets the service account ID (typically an e-mail address) or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    @Beta
    public Builder setServiceAccountId(String serviceAccountId) {
      this.serviceAccountId = serviceAccountId;
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Returns a collection of OAuth scopes to use with the the service account flow or {@code null}
     * for none.
     */
    @Beta
    public final Collection<String> getServiceAccountScopes() {
      return serviceAccountScopes;
    }

    /**
     * {@link Beta} <br/>
     * Sets the space-separated OAuth scopes to use with the the service account flow or
     * {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param serviceAccountScopes collection of scopes to be joined by a space separator (or a
     *        single value containing multiple space-separated scopes)
     * @since 1.15
     */
    @Beta
    public Builder setServiceAccountScopes(Collection<String> serviceAccountScopes) {
      this.serviceAccountScopes = serviceAccountScopes;
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Returns the private key to use with the the service account flow or {@code null} for none.
     */
    @Beta
    public final PrivateKey getServiceAccountPrivateKey() {
      return serviceAccountPrivateKey;
    }

    /**
     * {@link Beta} <br/>
     * Sets the private key to use with the the service account flow or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    @Beta
    public Builder setServiceAccountPrivateKey(PrivateKey serviceAccountPrivateKey) {
      this.serviceAccountPrivateKey = serviceAccountPrivateKey;
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Sets the private key to use with the the service account flow or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param p12File input stream to the p12 file (closed at the end of this method in a finally
     *        block)
     */
    @Beta
    public Builder setServiceAccountPrivateKeyFromP12File(File p12File)
        throws GeneralSecurityException, IOException {
      serviceAccountPrivateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
          SecurityUtils.getPkcs12KeyStore(), new FileInputStream(p12File), "notasecret",
          "privatekey", "notasecret");
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Sets the private key to use with the the service account flow or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param pemFile input stream to the PEM file (closed at the end of this method in a finally
     *        block)
     * @since 1.13
     */
    @Beta
    public Builder setServiceAccountPrivateKeyFromPemFile(File pemFile)
        throws GeneralSecurityException, IOException {
      byte[] bytes = PemReader.readFirstSectionAndClose(new FileReader(pemFile), "PRIVATE KEY")
          .getBase64DecodedBytes();
      serviceAccountPrivateKey =
          SecurityUtils.getRsaKeyFactory().generatePrivate(new PKCS8EncodedKeySpec(bytes));
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Returns the email address of the user the application is trying to impersonate in the service
     * account flow or {@code null} for none.
     */
    @Beta
    public final String getServiceAccountUser() {
      return serviceAccountUser;
    }

    /**
     * {@link Beta} <br/>
     * Sets the email address of the user the application is trying to impersonate in the service
     * account flow or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    @Beta
    public Builder setServiceAccountUser(String serviceAccountUser) {
      this.serviceAccountUser = serviceAccountUser;
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
    public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
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
