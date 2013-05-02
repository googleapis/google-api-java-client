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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.util.SecurityTestUtils;

import junit.framework.TestCase;

import java.util.Collections;

/**
 * Tests {@link GoogleCredential}.
 *
 * @author Yaniv Inbar
 */
public class GoogleCredentialTest extends TestCase {

  public void testRefreshToken_ServiceAccounts() throws Exception {
    GoogleCredential credential = new GoogleCredential.Builder().setServiceAccountId("id")
        .setServiceAccountScopes(Collections.singleton("scope"))
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(new MockHttpTransport() {

            @Override
          public LowLevelHttpRequest buildRequest(String method, String url) {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse().setContentType(
                Json.MEDIA_TYPE).setContent("{\"refresh_token\":\"abc\"}");
            return new MockLowLevelHttpRequest(url).setResponse(response);
          }
        }).setJsonFactory(new JacksonFactory()).setClientSecrets("clientId", "clientSecret")
        .build();
    assertTrue(credential.refreshToken());
    assertEquals("abc", credential.getRefreshToken());
  }
}
