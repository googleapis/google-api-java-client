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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.util.SecurityTestUtils;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;

/**
 * Tests {@link GoogleCredential}.
 *
 * @author Yaniv Inbar
 */
public class GoogleCredentialTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static final Collection<String> SCOPES = Arrays.asList("scope1", "scope2");

  public void testRefreshToken_ServiceAccounts() throws Exception {
    final String SA_EMAIL= "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(SA_EMAIL, ACCESS_TOKEN);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(SA_EMAIL)
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();

    assertTrue(credential.refreshToken());
    assertEquals(ACCESS_TOKEN, credential.getAccessToken());
  }

  public void testRefreshToken_User() throws Exception {
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PuGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    credential.setRefreshToken(REFRESH_TOKEN);

    assertTrue(credential.refreshToken());
    assertEquals(ACCESS_TOKEN, credential.getAccessToken());
  }
}
