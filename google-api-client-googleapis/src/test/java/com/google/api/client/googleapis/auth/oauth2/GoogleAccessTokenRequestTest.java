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

import com.google.api.client.googleapis.auth.oauth2.GoogleAccessTokenRequest.GoogleAssertionGrant;
import com.google.api.client.googleapis.auth.oauth2.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleAccessTokenRequest}.
 *
 * @author Yaniv Inbar
 */
public class GoogleAccessTokenRequestTest extends TestCase {
  private static final NetHttpTransport TRANSPORT = new NetHttpTransport();
  private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
  private static final String CLIENT_ID = "s6BhdRkqt3";
  private static final String CLIENT_SECRET = "gX1fBat3bV";
  private static final String CODE = "i1WsRn1uB1";
  private static final String REDIRECT_URL = "https://client.example.com/cb";
  private static final String ASSERTION_TYPE = "urn:oasis:names:tc:SAML:2.0:";
  private static final String ASSERTION = "PHNhbWxwOl...[omitted for brevity]...ZT4=";

  public void testGoogleAuthorizationCodeGrant() {
    check(new GoogleAuthorizationCodeGrant(), false);
    check(new GoogleAuthorizationCodeGrant(TRANSPORT,
        JSON_FACTORY,
        CLIENT_ID,
        CLIENT_SECRET,
        CODE,
        REDIRECT_URL), true);
  }

  public void testGoogleAssertionGrant() {
    check(new GoogleAssertionGrant(), false);
    check(new GoogleAssertionGrant(TRANSPORT,
        JSON_FACTORY,
        CLIENT_ID,
        CLIENT_SECRET,
        ASSERTION_TYPE,
        ASSERTION), true);
  }

  private void check(GoogleAuthorizationCodeGrant request, boolean withParameters) {
    check(request, "authorization_code", withParameters);
    if (withParameters) {
      assertEquals(CODE, request.code);
      assertEquals(REDIRECT_URL, request.redirectUri);
    } else {
      assertNull(request.clientId);
      assertNull(request.code);
      assertNull(request.redirectUri);
    }
  }

  private void check(GoogleAssertionGrant request, boolean withParameters) {
    check(request, "assertion", withParameters);
    if (withParameters) {
      assertEquals(ASSERTION_TYPE, request.assertionType);
      assertEquals(ASSERTION, request.assertion);
    } else {
      assertNull(request.assertionType);
      assertNull(request.assertion);
    }
  }

  private void check(GoogleAccessTokenRequest request, String grantType, boolean withParameters) {
    assertEquals(grantType, request.grantType);
    assertEquals("https://accounts.google.com/o/oauth2/token", request.authorizationServerUrl);
    assertFalse(request.useBasicAuthorization);
    assertNull(request.scope);
    if (withParameters) {
      assertEquals(TRANSPORT, request.transport);
      assertEquals(JSON_FACTORY, request.jsonFactory);
      assertEquals(CLIENT_ID, request.clientId);
      assertEquals(CLIENT_SECRET, request.clientSecret);
    } else {
      assertNull(request.transport);
      assertNull(request.jsonFactory);
      assertNull(request.clientId);
      assertNull(request.clientSecret);
    }
  }
}
