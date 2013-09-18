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

package com.google.api.client.googleapis.extensions.jackson2.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.OAuthContext;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread safe OAuth 2.0 authorization context for installed application. It implements
 * {@link OAuthContext} with default values such as
 * {@link NetHttpTransport#getDefaultInstance} as the HTTP transport and
 * {@link JacksonFactory#getDefaultInstance} as the JSON factory.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 */
public class GoogleOAuthInstalledAppContext implements OAuthContext {

  private AuthorizationCodeFlow flow;
  private GoogleClientSecrets clientSecrets;

  private final DataStoreFactory dataStoreFactory;
  private final String clientSecretsPath;
  private final Collection<String> scopes;
  private final String applicationName;

  private final ReentrantLock lock = new ReentrantLock();

  /**
   * Constructs a new OAuth context for installed applications.
   *
   * @param clientSecretsPath path to the client secrets Json file
   * @param scopes scopes
   * @param applicationName application name
   */
  public GoogleOAuthInstalledAppContext(
      String clientSecretsPath, Collection<String> scopes, String applicationName)
      throws IOException {
    dataStoreFactory = new FileDataStoreFactory(
        new java.io.File(System.getProperty("user.home"), ".store/" + applicationName));
    this.clientSecretsPath = clientSecretsPath;
    this.applicationName = applicationName;
    this.scopes = scopes;
  }

  @Override
  public HttpTransport getTransport() {
    return NetHttpTransport.getDefaultInstance();
  }

  @Override
  public JsonFactory getJsonFactory() {
    return JacksonFactory.getDefaultInstance();
  }

  @Override
  public AuthorizationCodeFlow getFlow() throws IOException {
    lock.lock();
    try {
      if (flow == null) {
        flow = new GoogleAuthorizationCodeFlow.Builder(getTransport(), getJsonFactory(),
            getClientSecrets(), getScopes()).setDataStoreFactory(getDataStoreFactory())
            .setAccessType("offline").build();
      }
    } finally {
      lock.unlock();
    }
    return flow;
  }

  @Override
  public DataStoreFactory getDataStoreFactory() {
    return dataStoreFactory;
  }

  @Override
  public String getUserAgent() {
    return applicationName;
  }

  @Override
  public Collection<String> getScopes() {
    return scopes;
  }

  /**
   * Returns the Google client secrets which contains the client identifier and client secret
   */
  protected GoogleClientSecrets getClientSecrets() throws IOException {
    lock.lock();
    try {
      if (clientSecrets == null) {
        clientSecrets = GoogleClientSecrets.load(getJsonFactory(), new InputStreamReader(
            GoogleOAuthInstalledAppContext.class.getResourceAsStream(clientSecretsPath)));
      }
    } finally {
      lock.unlock();
    }
    return clientSecrets;
  }
}
