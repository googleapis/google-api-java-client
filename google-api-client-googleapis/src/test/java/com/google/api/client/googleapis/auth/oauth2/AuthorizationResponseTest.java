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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationResponse;

import junit.framework.TestCase;

/**
 * Tests {@link AuthorizationResponse}.
 *
 * @author Yaniv Inbar
 */
public class AuthorizationResponseTest extends TestCase {

  public AuthorizationResponseTest(String name) {
    super(name);
  }

  public void test() {
    AuthorizationResponse response =
        new AuthorizationResponse("https://client.example.com/cb?code=i1WsRn1uB1");
    assertEquals("i1WsRn1uB1", response.code);
    response =
        new AuthorizationResponse("http://example.com/rd#access_token=FJQbwq9&expires_in=3600");
    assertEquals("FJQbwq9", response.accessToken);
    assertEquals(3600L, response.expiresIn.longValue());
    response = new AuthorizationResponse(
        "http://example.com/rd?code=i1WsRn1uB1#access_token=FJQbwq9&expires_in=3600");
    assertEquals("i1WsRn1uB1", response.code);
    assertEquals("FJQbwq9", response.accessToken);
    assertEquals(3600L, response.expiresIn.longValue());
  }
}
