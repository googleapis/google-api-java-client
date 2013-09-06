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

package com.google.api.client.googleapis.extensions.appengine.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineOAuthApplicationContext;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread safe OAuth 2.0 authorization appengine application context. It extends
 * {@link AppEngineOAuthApplicationContext} and add implementation for both
 * {@link #getClientSecrets} and {@link #getFlow}.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 *
 */
public class GoogleAppEngineOAuthApplicationContext extends AppEngineOAuthApplicationContext {

  private AuthorizationCodeFlow flow;
  private GoogleClientSecrets clientSecrets;

  private final String clientSecretsPath;
  private final ReentrantLock lock = new ReentrantLock();

  public GoogleAppEngineOAuthApplicationContext(String redirectUri, String clientSecretsPath,
      Collection<String> scopes, String applicationName) {
    super(redirectUri, scopes, applicationName);
    this.clientSecretsPath = clientSecretsPath;
  }

  protected GoogleClientSecrets getClientSecrets() throws IOException {
    lock.lock();
    try {
      if (clientSecrets == null) {
        clientSecrets = GoogleClientSecrets.load(getJsonFactory(), new InputStreamReader(
            GoogleAppEngineOAuthApplicationContext.class.getResourceAsStream(clientSecretsPath)));
      }
    } finally {
      lock.unlock();
    }
    return clientSecrets;
  }

  @Override // TODO(NOW): Lock / thread-safety
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
}
