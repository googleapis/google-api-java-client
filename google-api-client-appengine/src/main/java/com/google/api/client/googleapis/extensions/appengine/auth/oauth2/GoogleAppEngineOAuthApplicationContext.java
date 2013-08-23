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
 * @author ngmiceli@google.com (Your Name Here)
 *
 */
public class GoogleAppEngineOAuthApplicationContext extends AppEngineOAuthApplicationContext {

  private AuthorizationCodeFlow flow;
  private GoogleClientSecrets clientSecrets;
  private String clientSecretsPath;
  private ReentrantLock lock = new ReentrantLock();

  public GoogleAppEngineOAuthApplicationContext(String redirectUri, String clientSecretsPath,
      Collection<String> scopes, String applicationName) {
    super(redirectUri, scopes, applicationName);
    this.clientSecretsPath = clientSecretsPath;
  }

  protected GoogleClientSecrets getClientSecrets() throws IOException {
    if (clientSecrets == null) {
      clientSecrets = GoogleClientSecrets.load(getJsonFactory(), new InputStreamReader(
          GoogleAppEngineOAuthApplicationContext.class.getResourceAsStream(clientSecretsPath)));
    }
    return clientSecrets;
  }

  @Override // TODO(NOW): Lock / thread-safety
  public AuthorizationCodeFlow getFlow() throws IOException {
    if (flow == null) {
      try {
        lock.lock();
        flow = new GoogleAuthorizationCodeFlow.Builder(getTransport(), getJsonFactory(),
            getClientSecrets(), getScopes()).setDataStoreFactory(getDataStoreFactory())
            .setAccessType("offline").build();
      } finally {
        lock.unlock();
      }
    }
    return flow;
  }
}
