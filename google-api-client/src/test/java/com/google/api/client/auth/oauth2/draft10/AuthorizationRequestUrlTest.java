/*
 * Copyright (c) 2010 Google Inc.
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

import junit.framework.TestCase;

/**
 * Tests {@link AuthorizationRequestUrl}.
 *
 * @author Yaniv Inbar
 */
public class AuthorizationRequestUrlTest extends TestCase {

  private static final String EXPECTED =
      "https://server.example.com/authorize?client_id=s6BhdRkqt3&"
          + "redirect_uri=https://client.example.com/cb&response_type=code";

  public AuthorizationRequestUrlTest(String name) {
    super(name);
  }

  public void testConstructor() {
    AuthorizationRequestUrl url =
        new AuthorizationRequestUrl("https://server.example.com/authorize");
    url.clientId = "s6BhdRkqt3";
    url.redirectUri = "https://client.example.com/cb";
    assertEquals(EXPECTED, url.build());
  }

  public void testConstructor_withClienId() {
    AuthorizationRequestUrl url =
        new AuthorizationRequestUrl("https://server.example.com/authorize", "s6BhdRkqt3");
    url.redirectUri = "https://client.example.com/cb";
    assertEquals(EXPECTED, url.build());
  }
}
