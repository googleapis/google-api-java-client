/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.auth.jsontoken.JsonWebSignature.Header;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleIdTokenVerifier}.
 *
 * @author Yaniv Inbar
 */
public class GoogleIdTokenVerifierTest extends TestCase {

  private static final String CLIENT_ID = "myclientid";

  private static Payload newPayload(String clientId) {
    Payload payload = new Payload();
    payload.setIssuer("accounts.google.com");
    payload.setAudience(clientId);
    payload.setIssuee(clientId);
    payload.setExpirationTimeSeconds(100 + System.currentTimeMillis() / 1000);
    payload.setIssuedAtTimeSeconds(System.currentTimeMillis() / 1000);
    return payload;
  }

  public void testVerify() throws Exception {
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier(new MockHttpTransport(), new JacksonFactory(), CLIENT_ID);
    Header header = new Header();
    header.setAlgorithm("RS25");
    Payload payload = newPayload(CLIENT_ID);
    Payload payload2 = newPayload(CLIENT_ID + "2");
    GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
    GoogleIdToken idToken2 = new GoogleIdToken(header, payload2, new byte[0], new byte[0]);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    verifier = new GoogleIdTokenVerifier(new MockHttpTransport(), new JacksonFactory(), null);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
  }
}
