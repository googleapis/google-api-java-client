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

package com.google.api.client.googleapis.auth.oauth2.draft10;

import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.GrantType;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAssertionGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleAuthorizationCodeGrant;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest.GoogleRefreshTokenGrant;
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
  private static final String REFRESH_TOKEN = "n4E9O119d";

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
    check(new GoogleAssertionGrant(TRANSPORT, JSON_FACTORY, ASSERTION_TYPE, ASSERTION), true);
  }

  public void testGoogleRefreshTokenGrant() {
    check(new GoogleRefreshTokenGrant(), false);
    check(new GoogleRefreshTokenGrant(
        TRANSPORT, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET, REFRESH_TOKEN), true);
  }

  private void check(GoogleAuthorizationCodeGrant request, boolean withParameters) {
    check(request, GrantType.AUTHORIZATION_CODE, withParameters);
    if (withParameters) {
      assertEquals(CLIENT_ID, request.clientId);
      assertEquals(CODE, request.code);
      assertEquals(REDIRECT_URL, request.redirectUri);
    } else {
      assertNull(request.clientId);
      assertNull(request.code);
      assertNull(request.redirectUri);
    }
  }

  private void check(GoogleAssertionGrant request, boolean withParameters) {
    check(request, GrantType.ASSERTION, withParameters);
    if (withParameters) {
      assertEquals(ASSERTION_TYPE, request.assertionType);
      assertEquals(ASSERTION, request.assertion);
    } else {
      assertNull(request.assertionType);
      assertNull(request.assertion);
    }
  }

  private void check(GoogleRefreshTokenGrant request, boolean withParameters) {
    check(request, GrantType.REFRESH_TOKEN, withParameters);
    if (withParameters) {
      assertEquals(CLIENT_ID, request.clientId);
      assertEquals(REFRESH_TOKEN, request.refreshToken);
    } else {
      assertNull(request.clientId);
      assertNull(request.refreshToken);
    }
  }

  private void check(AccessTokenRequest request, GrantType grantType, boolean withParameters) {
    assertEquals(grantType, request.grantType);
    assertEquals("https://accounts.google.com/o/oauth2/token", request.authorizationServerUrl);
    assertFalse(request.useBasicAuthorization);
    assertNull(request.scope);
    if (withParameters) {
      assertEquals(TRANSPORT, request.transport);
      assertEquals(JSON_FACTORY, request.jsonFactory);
      if (grantType != GrantType.ASSERTION) {
        assertEquals(CLIENT_SECRET, request.clientSecret);
      }
    } else {
      assertNull(request.transport);
      assertNull(request.jsonFactory);
      assertNull(request.clientSecret);
    }
  }
}
