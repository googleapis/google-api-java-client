/*
 * Copyright 2012 Google Inc.
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

import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Lists;
import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleIdTokenVerifier}.
 *
 * @author Yaniv Inbar
 */
public class GoogleIdTokenVerifierTest extends TestCase {

  private static final String ISSUER = "issuer.example.com";

  private static final String CLIENT_ID = "myclientid";

  private static final List<String> TRUSTED_CLIENT_IDS = Arrays.asList(CLIENT_ID);

  private static Payload newPayload(String clientId) {
    Payload payload = new Payload();
    payload.setIssuer("accounts.google.com");
    payload.setAudience(clientId);
    payload.setAuthorizedParty(clientId);
    payload.setExpirationTimeSeconds(100L);
    payload.setIssuedAtTimeSeconds(0L);
    return payload;
  }

  public void testBuilder() throws Exception {
    GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder(
        new GooglePublicKeysManagerTest.PublicCertsMockHttpTransport(), new JacksonFactory()).setIssuer(
        ISSUER).setAudience(TRUSTED_CLIENT_IDS);
    assertEquals(Clock.SYSTEM, builder.getClock());
    assertEquals(ISSUER, builder.getIssuer());
    assertTrue(TRUSTED_CLIENT_IDS.equals(builder.getAudience()));
    Clock clock = new FixedClock(4);
    builder.setClock(clock);
    assertEquals(clock, builder.getClock());
    IdTokenVerifier verifier = builder.build();
    assertEquals(clock, verifier.getClock());
    assertEquals(ISSUER, verifier.getIssuer());
    assertEquals(TRUSTED_CLIENT_IDS, Lists.newArrayList(verifier.getAudience()));
  }

  public void testVerify() throws Exception {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new GooglePublicKeysManagerTest.PublicCertsMockHttpTransport(), new JacksonFactory()).build();
    Header header = new Header();
    header.setAlgorithm("RS25");
    Payload payload = newPayload(CLIENT_ID);
    Payload payload2 = newPayload(CLIENT_ID + "2");
    GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
    GoogleIdToken idToken2 = new GoogleIdToken(header, payload2, new byte[0], new byte[0]);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    verifier = new GoogleIdTokenVerifier(
        new GooglePublicKeysManagerTest.PublicCertsMockHttpTransport(), new JacksonFactory());
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    // TODO(yanivi): add a unit test that returns true
  }
}
