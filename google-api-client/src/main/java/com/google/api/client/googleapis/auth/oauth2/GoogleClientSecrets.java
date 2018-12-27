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

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * OAuth 2.0 client secrets JSON model as specified in <a
 * href="https://developers.google.com/api-client-library/python/guide/aaa_client_secrets">client_secrets.json
 * file format</a>.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static GoogleClientSecrets loadClientSecretsResource(JsonFactory jsonFactory) throws IOException {
    return GoogleClientSecrets.load(
        jsonFactory,
        new InputStreamReader(
            SampleClass.class.getResourceAsStream("/client_secrets.json"), "UTF-8"));
  }
 * </pre>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public final class GoogleClientSecrets extends GenericJson {

  /** Details for installed applications. */
  @Key
  private Details installed;

  /** Details for web applications. */
  @Key
  private Details web;

  /** Returns the details for installed applications. */
  public Details getInstalled() {
    return installed;
  }

  /** Sets the details for installed applications. */
  public GoogleClientSecrets setInstalled(Details installed) {
    this.installed = installed;
    return this;
  }

  /** Returns the details for web applications. */
  public Details getWeb() {
    return web;
  }

  /** Sets the details for web applications. */
  public GoogleClientSecrets setWeb(Details web) {
    this.web = web;
    return this;
  }

  /** Returns the details for either installed or web applications. */
  public Details getDetails() {
    // that web or installed, but not both
    Preconditions.checkArgument((web == null) != (installed == null));
    return web == null ? installed : web;
  }

  /** Client credential details. */
  public static final class Details extends GenericJson {

    /** Client ID. */
    @Key("client_id")
    private String clientId;

    /** Client secret. */
    @Key("client_secret")
    private String clientSecret;

    /** Redirect URIs. */
    @Key("redirect_uris")
    private List<String> redirectUris;

    /** Authorization server URI. */
    @Key("auth_uri")
    private String authUri;

    /** Token server URI. */
    @Key("token_uri")
    private String tokenUri;

    /** Returns the client ID. */
    public String getClientId() {
      return clientId;
    }

    /** Sets the client ID. */
    public Details setClientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    /** Returns the client secret. */
    public String getClientSecret() {
      return clientSecret;
    }

    /** Sets the client secret. */
    public Details setClientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    /** Returns the redirect URIs. */
    public List<String> getRedirectUris() {
      return redirectUris;
    }

    /** Sets the redirect URIs. */
    public Details setRedirectUris(List<String> redirectUris) {
      this.redirectUris = redirectUris;
      return this;
    }

    /** Returns the authorization server URI. */
    public String getAuthUri() {
      return authUri;
    }

    /** Sets the authorization server URI. */
    public Details setAuthUri(String authUri) {
      this.authUri = authUri;
      return this;
    }

    /** Returns the token server URI. */
    public String getTokenUri() {
      return tokenUri;
    }

    /** Sets the token server URI. */
    public Details setTokenUri(String tokenUri) {
      this.tokenUri = tokenUri;
      return this;
    }

    @Override
    public Details set(String fieldName, Object value) {
      return (Details) super.set(fieldName, value);
    }

    @Override
    public Details clone() {
      return (Details) super.clone();
    }
  }

  @Override
  public GoogleClientSecrets set(String fieldName, Object value) {
    return (GoogleClientSecrets) super.set(fieldName, value);
  }

  @Override
  public GoogleClientSecrets clone() {
    return (GoogleClientSecrets) super.clone();
  }

  /**
   * Loads the {@code client_secrets.json} file from the given reader.
   *
   * @since 1.15
   */
  public static GoogleClientSecrets load(JsonFactory jsonFactory, Reader reader)
      throws IOException {
    return jsonFactory.fromReader(reader, GoogleClientSecrets.class);
  }
}
