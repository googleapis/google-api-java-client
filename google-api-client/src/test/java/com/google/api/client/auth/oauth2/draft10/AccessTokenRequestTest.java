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

package com.google.api.client.auth.oauth2.draft10;

import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AssertionGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AuthorizationCodeGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.GrantType;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.RefreshTokenGrant;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.ResourceOwnerPasswordCredentialsGrant;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

import junit.framework.TestCase;

/**
 * Tests {@link AccessTokenRequest}.
 *
 * @author Yaniv Inbar
 */
public class AccessTokenRequestTest extends TestCase {

  private static final NetHttpTransport TRANSPORT = new NetHttpTransport();
  private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
  private static final String AUTHORIZATION_SERVER_URL = "https://server.example.com/authorize";
  private static final String CLIENT_ID = "s6BhdRkqt3";
  private static final String CLIENT_SECRET = "gX1fBat3bV";
  private static final String CODE = "i1WsRn1uB1";
  private static final String REDIRECT_URL = "https://client.example.com/cb";
  private static final String USERNAME = "johndoe";
  private static final String PASSWORD = "A3ddj3w";
  private static final String ASSERTION_TYPE = "urn:oasis:names:tc:SAML:2.0:";
  private static final String ASSERTION = "PHNhbWxwOl...[omitted for brevity]...ZT4=";
  private static final String REFRESH_TOKEN = "n4E9O119d";

  public void testAccessTokenRequest() {
    check(new AccessTokenRequest(), false);
    check(new AccessTokenRequest(
        TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, CLIENT_ID, CLIENT_SECRET), true);
  }

  public void testAuthorizationCodeGrant() {
    check(new AuthorizationCodeGrant(), false);
    check(new AuthorizationCodeGrant(TRANSPORT,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        CODE,
        REDIRECT_URL), true);
  }

  public void testResourceOwnerPasswordCredentialsGrant() {
    check(new ResourceOwnerPasswordCredentialsGrant(), false);
    check(new ResourceOwnerPasswordCredentialsGrant(TRANSPORT,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        USERNAME,
        PASSWORD), true);
  }

  public void testAssertionGrant() {
    check(new AssertionGrant(), false);
    check(new AssertionGrant(TRANSPORT,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_SECRET,
        ASSERTION_TYPE,
        ASSERTION), true);
  }

  public void testRefreshTokenGrant() {
    check(new RefreshTokenGrant(), false);
    check(new RefreshTokenGrant(TRANSPORT,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        REFRESH_TOKEN), true);
  }

  private void check(AuthorizationCodeGrant request, boolean withParameters) {
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

  private void check(ResourceOwnerPasswordCredentialsGrant request, boolean withParameters) {
    check(request, GrantType.PASSWORD, withParameters);
    if (withParameters) {
      assertEquals(CLIENT_ID, request.clientId);
      assertEquals(USERNAME, request.username);
      assertEquals(PASSWORD, request.password);
    } else {
      assertNull(request.clientId);
      assertNull(request.username);
      assertNull(request.password);
    }
  }

  private void check(AssertionGrant request, boolean withParameters) {
    check(request, GrantType.ASSERTION, withParameters);
    assertNull(request.clientId);
    if (withParameters) {
      assertEquals(ASSERTION_TYPE, request.assertionType);
      assertEquals(ASSERTION, request.assertion);
    } else {
      assertNull(request.assertionType);
      assertNull(request.assertion);
    }
  }

  private void check(RefreshTokenGrant request, boolean withParameters) {
    check(request, GrantType.REFRESH_TOKEN, withParameters);
    if (withParameters) {
      assertEquals(CLIENT_ID, request.clientId);
      assertEquals(REFRESH_TOKEN, request.refreshToken);
    } else {
      assertNull(request.clientId);
      assertNull(request.refreshToken);
    }
  }

  private void check(AccessTokenRequest request, boolean withParameters) {
    check(request, GrantType.NONE, withParameters);
  }

  private void check(AccessTokenRequest request, GrantType grantType, boolean withParameters) {
    assertEquals(grantType, request.grantType);
    assertFalse(request.useBasicAuthorization);
    assertNull(request.scope);
    if (withParameters) {
      assertEquals(TRANSPORT, request.transport);
      assertEquals(JSON_FACTORY, request.jsonFactory);
      assertEquals(AUTHORIZATION_SERVER_URL, request.authorizationServerUrl);
      assertEquals(CLIENT_SECRET, request.clientSecret);
    } else {
      assertNull(request.transport);
      assertNull(request.jsonFactory);
      assertNull(request.authorizationServerUrl);
      assertNull(request.clientSecret);
    }
  }
}
