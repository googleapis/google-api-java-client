/*
 * Copyright 2013 Google Inc.
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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.appengine.testing.auth.oauth2.MockAppIdentityService;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestCase;

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
    final String expectedAccessToken = "ExpectedAccessToken";

    MockAppIdentityService appIdentity = new MockAppIdentityService();
    appIdentity.setAccessTokenText(expectedAccessToken);
    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    builder.setAppIdentityService(appIdentity);
    AppIdentityCredential appCredential = builder.build();
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request = transport.createRequestFactory().buildRequest(
        "get", null, null);

    appCredential.intercept(request);

    assertEquals(1, appIdentity.getGetAccessTokenCallCount());
    HttpHeaders headers = request.getHeaders();
    String authHeader = headers.getAuthorization();
    Boolean headerContainsToken = authHeader.contains(expectedAccessToken);
    assertTrue(headerContainsToken);
  }

  public void testAppEngineCredentialWrapper() throws IOException {
    final String expectedAccessToken = "ExpectedAccessToken";
    final Collection<String> emptyScopes = Collections.emptyList();

    HttpTransport transport = new MockHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    MockAppIdentityService appIdentity = new MockAppIdentityService();
    appIdentity.setAccessTokenText(expectedAccessToken);

    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(emptyScopes);
    builder.setAppIdentityService(appIdentity);
    AppIdentityCredential appCredential = builder.build();

    GoogleCredential wrapper = new
        AppIdentityCredential.AppEngineCredentialWrapper(appCredential, transport, jsonFactory);

    HttpRequest request = transport.createRequestFactory().buildRequest("get", null, null);

    assertTrue(wrapper.createScopedRequired());
    try {
      wrapper.intercept(request);
      fail("Should not be able to use credential without scopes.");
    } catch (Exception expected) {
    }
    assertEquals(1, appIdentity.getGetAccessTokenCallCount());

    GoogleCredential scopedWrapper = wrapper.createScoped(SCOPES);
    assertNotSame(wrapper, scopedWrapper);
    scopedWrapper.intercept(request);

    assertEquals(2, appIdentity.getGetAccessTokenCallCount());
    HttpHeaders headers = request.getHeaders();
    String authHeader = headers.getAuthorization();
    assertTrue(authHeader.contains(expectedAccessToken));
  }

  public void testAppEngineCredentialWrapperGetAccessToken() throws IOException {
    final String expectedAccessToken = "ExpectedAccessToken";

    HttpTransport transport = new MockHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    MockAppIdentityService appIdentity = new MockAppIdentityService();
    appIdentity.setAccessTokenText(expectedAccessToken);

    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    builder.setAppIdentityService(appIdentity);
    AppIdentityCredential appCredential = builder.build();

    GoogleCredential wrapper = new
        AppIdentityCredential.AppEngineCredentialWrapper(appCredential, transport, jsonFactory);
    assertTrue(wrapper.refreshToken());
    assertEquals(expectedAccessToken, wrapper.getAccessToken());
  }

  public void testAppEngineCredentialWrapperNullTransportThrows() throws IOException {
    JsonFactory jsonFactory = new JacksonFactory();
    try {
      new AppIdentityCredential.AppEngineCredentialWrapper(null, jsonFactory);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testAppEngineCredentialWrapperNullJsonFactoryThrows() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    try {
      new AppIdentityCredential.AppEngineCredentialWrapper(transport, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }
}
