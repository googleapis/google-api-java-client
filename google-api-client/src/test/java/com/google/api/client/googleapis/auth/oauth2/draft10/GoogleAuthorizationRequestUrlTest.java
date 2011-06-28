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

import junit.framework.TestCase;

/**
 * Tests {@link GoogleAuthorizationRequestUrl}.
 *
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationRequestUrlTest extends TestCase {
  private static final String CLIENT_ID = "s6BhdRkqt3";
  private static final String REDIRECT_URL = "https://client.example.com/cb";
  private static final String SCOPE = "https://www.googleapis.com/auth/buzz";
  private static final String EXPECTED =
      "https://accounts.google.com/o/oauth2/auth?client_id=" + CLIENT_ID + "&redirect_uri="
          + REDIRECT_URL + "&response_type=code&" + "scope=" + SCOPE;

  public void testConstructor() {
    GoogleAuthorizationRequestUrl url = new GoogleAuthorizationRequestUrl();
    url.clientId = CLIENT_ID;
    url.redirectUri = REDIRECT_URL;
    url.scope = SCOPE;
    assertEquals(EXPECTED, url.build());
  }

  public void testConstructor_withParameters() {
    GoogleAuthorizationRequestUrl url =
        new GoogleAuthorizationRequestUrl(CLIENT_ID, REDIRECT_URL, SCOPE);
    assertEquals(EXPECTED, url.build());
  }

}
