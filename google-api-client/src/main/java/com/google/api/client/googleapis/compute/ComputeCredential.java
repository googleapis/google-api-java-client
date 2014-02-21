/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.compute;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link Beta} <br/>
 * Google Compute Engine service accounts OAuth 2.0 credential based on <a
 * href="https://developers.google.com/compute/docs/authentication">Authenticating from Google
 * Compute Engine</a>.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(
      HttpTransport transport, JsonFactory jsonFactory) {
    return transport.createRequestFactory(new GoogleComputeCredential(transport, jsonFactory));
  }
 * </pre>
 *
 * <p>
 * Implementation is immutable and thread-safe.
 * </p>
 *
 * @since 1.15
 * @author Yaniv Inbar
 */
@Beta
public class ComputeCredential extends Credential {

  /** Metadata Service Account token server encoded URL. */
  public static final String TOKEN_SERVER_ENCODED_URL =
      "http://metadata/computeMetadata/v1/instance/service-accounts/default/token";

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   */
  public ComputeCredential(HttpTransport transport, JsonFactory jsonFactory) {
    this(new Builder(transport, jsonFactory));
  }

  /**
   * @param builder builder
   */
  protected ComputeCredential(Builder builder) {
    super(builder);
  }

  @Override
  protected TokenResponse executeRefreshToken() throws IOException {
    GenericUrl tokenUrl = new GenericUrl(getTokenServerEncodedUrl());
    HttpRequest request = getTransport().createRequestFactory().buildGetRequest(tokenUrl);
    request.setParser(new JsonObjectParser(getJsonFactory()));
    request.getHeaders().set("X-Google-Metadata-Request", true);
    return request.execute().parseAs(TokenResponse.class);
  }

  /**
   * {@link Beta} <br/>
   * Google Compute Engine credential builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  @Beta
  public static class Builder extends Credential.Builder {

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory) {
      super(BearerToken.authorizationHeaderAccessMethod());
      setTransport(transport);
      setJsonFactory(jsonFactory);
      setTokenServerEncodedUrl(TOKEN_SERVER_ENCODED_URL);
    }

    @Override
    public ComputeCredential build() {
      return new ComputeCredential(this);
    }

    @Override
    public Builder setTransport(HttpTransport transport) {
      return (Builder) super.setTransport(Preconditions.checkNotNull(transport));
    }

    @Override
    public Builder setClock(Clock clock) {
      return (Builder) super.setClock(clock);
    }

    @Override
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      return (Builder) super.setJsonFactory(Preconditions.checkNotNull(jsonFactory));
    }

    @Override
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      return (Builder) super.setTokenServerUrl(Preconditions.checkNotNull(tokenServerUrl));
    }

    @Override
    public Builder setTokenServerEncodedUrl(String tokenServerEncodedUrl) {
      return (Builder) super.setTokenServerEncodedUrl(
          Preconditions.checkNotNull(tokenServerEncodedUrl));
    }

    @Override
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      Preconditions.checkArgument(clientAuthentication == null);
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
  }
}
