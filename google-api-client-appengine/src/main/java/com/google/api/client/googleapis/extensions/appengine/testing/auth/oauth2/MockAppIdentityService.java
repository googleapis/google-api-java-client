/*
 * Copyright 2014 Google Inc.
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

package com.google.api.client.googleapis.extensions.appengine.testing.auth.oauth2;

import com.google.api.client.util.Beta;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.api.appidentity.PublicCertificate;

import java.util.Collection;
import java.util.Date;

/**
 * {@link Beta} <br/>
 * Mock implementation of AppIdentityService interface for testing.
 *
 * @since 1.19
 */
@Beta
public class MockAppIdentityService implements AppIdentityService {

  private int getAccessTokenCallCount = 0;
  private String accessTokenText = null;

  public MockAppIdentityService() {
  }

  public int getGetAccessTokenCallCount() {
    return getAccessTokenCallCount;
  }

  public String getAccessTokenText() {
    return accessTokenText;
  }

  public void setAccessTokenText(String text) {
    accessTokenText = text;
  }

  @Override
  public SigningResult signForApp(byte[] signBlob) {
    return null;
  }

  @Override
  public Collection<PublicCertificate> getPublicCertificatesForApp() {
    return null;
  }

  @Override
  public GetAccessTokenResult getAccessToken(Iterable<String> scopes) {
    getAccessTokenCallCount++;
    int scopeCount = 0;
    for (String scope : scopes) {
      if (scope != null) {
        scopeCount++;
      }
    }
    if (scopeCount == 0) {
      throw new AppIdentityServiceFailureException("No scopes specified.");
    }
    return new GetAccessTokenResult(accessTokenText,
        new Date(System.currentTimeMillis() + 3600000));
  }

  @Override
  public GetAccessTokenResult getAccessTokenUncached(Iterable<String> scopes) {
    return null;
  }

  @Override
  public String getServiceAccountName() {
    return null;
  }

  @Override
  public ParsedAppId parseFullAppId(String fullAppId) {
    return null;
  }

  @Override
  public String getDefaultGcsBucketName() { return null; }
}
