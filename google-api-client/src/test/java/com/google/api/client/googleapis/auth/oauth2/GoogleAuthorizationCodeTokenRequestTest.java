/*
 * Copyright (c) 2011 Google Inc.
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

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleAuthorizationCodeTokenRequest}.
 *
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationCodeTokenRequestTest extends TestCase {

  private static final String CLIENT_ID = "812741506391.apps.googleusercontent.com";
  private static final String CLIENT_SECRET = "{client_secret}";
  private static final String CODE = "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7";
  private static final String REDIRECT_URI = "https://oauth2-login-demo.appspot.com/code";

  public void test() {
    GoogleAuthorizationCodeTokenRequest request =
        new GoogleAuthorizationCodeTokenRequest(new MockHttpTransport(),
            new JacksonFactory(),
            CLIENT_ID,
            CLIENT_SECRET,
            CODE,
            REDIRECT_URI);
    ClientParametersAuthentication clientAuthentication =
        (ClientParametersAuthentication) request.getClientAuthentication();
    assertEquals(CLIENT_ID, clientAuthentication.getClientId());
    assertEquals(CLIENT_SECRET, clientAuthentication.getClientSecret());
    assertEquals(CODE, request.getCode());
    assertEquals(REDIRECT_URI, request.getRedirectUri());
    assertEquals("authorization_code", request.getGrantType());
    assertNull(request.getScopes());
    assertNotNull(request.getTokenServerUrl());
  }
}
