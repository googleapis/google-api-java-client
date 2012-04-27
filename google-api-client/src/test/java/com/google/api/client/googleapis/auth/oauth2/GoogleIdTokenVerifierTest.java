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
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests {@link GoogleIdTokenVerifier}.
 *
 * @author Yaniv Inbar
 */
public class GoogleIdTokenVerifierTest extends TestCase {

  private static final String CLIENT_ID = "myclientid";

  private static Payload newPayload(String clientId) {
    Payload payload = new Payload(new FixedClock(0L));
    payload.setIssuer("accounts.google.com");
    payload.setAudience(clientId);
    payload.setIssuee(clientId);
    payload.setExpirationTimeSeconds(100L);
    payload.setIssuedAtTimeSeconds(0L);
    return payload;
  }

  public void testBuilder() throws Exception {
    String clientId2 = "myclientid2";
    String clientId3 = "myclientid3";
    String clientId4 = "myclientid4";
    Set<String> clientIds = new HashSet<String>();
    clientIds.add(CLIENT_ID);
    clientIds.add(clientId2);
    clientIds.add(clientId3);
    clientIds.add(clientId4);
    GoogleIdTokenVerifier.Builder builder =
        new GoogleIdTokenVerifier.Builder(new MockHttpTransport(), new JacksonFactory());
    builder.setClientIds(CLIENT_ID);
    builder.setClientIds(clientIds);
    Set<String> actualClientIds = builder.getClientIds();
    // The first setClientIds should have been cleared and replaced by the new set.
    assertEquals(4, actualClientIds.size());
    assertTrue(actualClientIds.contains(CLIENT_ID));
    assertTrue(actualClientIds.contains(clientId2));
    assertTrue(actualClientIds.contains(clientId3));
    assertTrue(actualClientIds.contains(clientId4));

    GoogleIdTokenVerifier verifier = builder.build();
    actualClientIds = verifier.getClientIds();
    assertEquals(4, actualClientIds.size());
    assertTrue(actualClientIds.contains(CLIENT_ID));
    assertTrue(actualClientIds.contains(clientId2));
    assertTrue(actualClientIds.contains(clientId3));
    assertTrue(actualClientIds.contains(clientId4));
    try {
      // Ensure that it is an unmodifiable Set.
      actualClientIds.add("something");
      fail("Expected " + UnsupportedOperationException.class);
    } catch (UnsupportedOperationException e) {
      // Expected.
    }
  }

  public void testVerify() throws Exception {
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(new MockHttpTransport(), new JacksonFactory())
            .setClientIds(CLIENT_ID).build();
    Header header = new Header();
    header.setAlgorithm("RS25");
    Payload payload = newPayload(CLIENT_ID);
    Payload payload2 = newPayload(CLIENT_ID + "2");
    GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
    GoogleIdToken idToken2 = new GoogleIdToken(header, payload2, new byte[0], new byte[0]);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    verifier = new GoogleIdTokenVerifier(new MockHttpTransport(), new JacksonFactory());
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
  }

  public void testVerifyWithClientIds() throws Exception {
    Set<String> clientIds = new HashSet<String>();
    clientIds.add(CLIENT_ID);
    clientIds.add("myclientid2");
    clientIds.add("myclientid3");
    clientIds.add("myclientid4");
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(new MockHttpTransport(), new JacksonFactory())
            .setClientIds(clientIds).build();
    Header header = new Header();
    header.setAlgorithm("RS25");
    Payload payload = newPayload(CLIENT_ID);
    Payload payload2 = newPayload(CLIENT_ID + "2");
    GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
    GoogleIdToken idToken2 = new GoogleIdToken(header, payload2, new byte[0], new byte[0]);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    verifier = new GoogleIdTokenVerifier(new MockHttpTransport(), new JacksonFactory());
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
  }
}
