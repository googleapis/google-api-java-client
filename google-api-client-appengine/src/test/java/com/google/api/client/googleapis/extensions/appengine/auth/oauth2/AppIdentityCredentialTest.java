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

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Tests {@link AppIdentityCredential}.
 *
 * @author Yaniv Inbar
 */
public class AppIdentityCredentialTest extends TestCase {

  private static final Collection<String> SCOPES =
      Collections.unmodifiableCollection(Arrays.asList("scope1", "scope2"));

  public void testBuilder() {
    String[] scopes = SCOPES.toArray(new String[SCOPES.size()]);
    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    scopes[1] = "somethingelse";
    assertTrue(Arrays.deepEquals(SCOPES.toArray(), builder.getScopes().toArray()));
    AppIdentityCredential credential = builder.build();
    assertTrue(Arrays.deepEquals(SCOPES.toArray(), credential.getScopes().toArray()));
  }

  public void testUsesAppIdentityService() throws IOException {
    final String EXPECTED_ACCESS_TOKEN = "ExpectedAccessToken";

    MockAppIdentityService appIdentity = new MockAppIdentityService();
    appIdentity.setAccessTokenText(EXPECTED_ACCESS_TOKEN);
    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    builder.setAppIdentityService(appIdentity);
    AppIdentityCredential appCredential = builder.build();
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request = transport.createRequestFactory().buildRequest(
        "get", null, null);

    appCredential.intercept(request);

    assertEquals(appIdentity.getGetAccessTokenCallCount(), 1);
    HttpHeaders headers = request.getHeaders();
    String authHeader = headers.getAuthorization();
    assertTrue(authHeader.contains(EXPECTED_ACCESS_TOKEN));
  }
}
