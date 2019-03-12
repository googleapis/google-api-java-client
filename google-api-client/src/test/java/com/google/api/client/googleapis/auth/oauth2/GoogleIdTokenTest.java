/*
 * Copyright 2013 Google Inc.
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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import junit.framework.TestCase;

/** Tests for {@link GoogleIdToken}.*/

public class GoogleIdTokenTest extends TestCase {
  private static final String USER_ID = "1234567890";
  private static final String ANOTHER_USER_ID = "2345678901";
  private static final String CLIENT_ID = "myClientId";
  private static final String ANOTHER_CLIENT_ID = "anotherClientId";
  private static final String EMAIL_VERIFIED_KEY = "email_verified";

  private static Payload newPayload(String userId, String clientId) {
    Payload payload = new Payload();
    payload.setIssuer("accounts.google.com");
    payload.setAudience(clientId);
    payload.setAuthorizedParty(clientId);
    payload.setSubject(userId);
    payload.setExpirationTimeSeconds(100L);
    payload.setIssuedAtTimeSeconds(0L);
    return payload;
  }

  @SuppressWarnings("deprecation")
  public void testDeprecatedMethods() {
    Payload payload = newPayload(USER_ID, CLIENT_ID);
    assertEquals(USER_ID, payload.getUserId());
    assertEquals(CLIENT_ID, payload.getIssuee());

    payload.setUserId(ANOTHER_USER_ID);
    payload.setIssuee(ANOTHER_CLIENT_ID);
    assertEquals(ANOTHER_USER_ID, payload.getUserId());
    assertEquals(ANOTHER_CLIENT_ID, payload.getIssuee());
    assertEquals(ANOTHER_USER_ID, payload.getSubject());
    assertEquals(ANOTHER_CLIENT_ID, payload.getAuthorizedParty());
  }

  public void testEmailVerified() {
    Payload payload = newPayload(USER_ID, CLIENT_ID);
    assertNull(payload.getEmailVerified());

    payload.setEmailVerified(true);
    assertTrue(payload.getEmailVerified());

    payload.setEmailVerified(false);
    assertFalse(payload.getEmailVerified());

    payload.setEmailVerified(null);
    assertNull(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, "true");
    assertTrue(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, true);
    assertTrue(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, "false");
    assertFalse(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, false);
    assertFalse(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, "RandomString");
    assertFalse(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, "");
    assertFalse(payload.getEmailVerified());

    payload.set(EMAIL_VERIFIED_KEY, null);
    assertNull(payload.getEmailVerified());

    // Wrong type.
    payload.set(EMAIL_VERIFIED_KEY, new Integer(5));
    try {
      payload.getEmailVerified();
      fail();
    } catch (ClassCastException e) {
      // Expected.
    }
  }
}
