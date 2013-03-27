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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

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
    payload.setExpirationTimeSeconds(100L);
    payload.setIssuedAtTimeSeconds(0L);
    return payload;
  }

  public void testBuilder() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    GoogleIdTokenVerifier.Builder builder =
        new GoogleIdTokenVerifier.Builder(transport, jsonFactory);

    GoogleIdTokenVerifier verifier = builder.build();
    assertEquals(transport, verifier.getTransport());
    assertEquals(jsonFactory, verifier.getJsonFactory());
  }

  private static final String TEST_CERTIFICATES =
      "{\r\n \"69d93af12d09b07b1f55680ac7e7fb2513b823e7\": \"-----BEGIN CERTIFICATE-----"
      + "\\nMIICITCCAYqgAwIBAgIIA9YgrgKJ4cowDQYJKoZIhvcNAQEFBQAwNjE0MDIGA1UE"
      + "\\nAxMrZmVkZXJhdGVkLXNpZ25vbi5zeXN0ZW0uZ3NlcnZpY2VhY2NvdW50LmNvbTAe"
      + "\\nFw0xMjA2MTIyMjQzMzRaFw0xMjA2MTQxMTQzMzRaMDYxNDAyBgNVBAMTK2ZlZGVy"
      + "\\nYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20wgZ8wDQYJKoZI"
      + "\\nhvcNAQEBBQADgY0AMIGJAoGBAJ6TDzmLxYD67aoTrzA3b8ouMXMeFxQOmsHn0SIA"
      + "\\nGjJypTQd0hXr3jGKqP53a4qtzm7YxyPyPOsvG8IMsB0RtB8gxh82KDQUqJ+mww8n"
      + "\\ney7WxW1qSmzyYog1z80MDYojODZ3j7wv1r8ajeJQSxQjBMehMEQkfjPuzERuzkCk"
      + "\\niBzzAgMBAAGjODA2MAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgeAMBYGA1Ud"
      + "\\nJQEB/wQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBBQUAA4GBAIx9j1gXCEm2Vr9r"
      + "\\nck6VK3ayG29+5ehNvzfYob+l731yU0yylEDEfN9OqqdW0dAqaauca+Ol8mGDIszx"
      + "\\nxudWD0NzNyvm39jwypvYz9qMYwbwVnQdfbpY5O0qbcb30eIDKZRHXzpZUj0zWHPM"
      + "\\nfwdrgc6XqQ48rjOsn22sWKQcB4/u\\n-----END CERTIFICATE-----\\n\",\r\n "
      + "\"67aec7b8e284bb03f489a5828d0eba52cc84cc23\": \"-----BEGIN CERTIFICATE-----"
      + "\\nMIICITCCAYqgAwIBAgIIcAqoF0CS2WgwDQYJKoZIhvcNAQEFBQAwNjE0MDIGA1UE"
      + "\\nAxMrZmVkZXJhdGVkLXNpZ25vbi5zeXN0ZW0uZ3NlcnZpY2VhY2NvdW50LmNvbTAe"
      + "\\nFw0xMjA2MTMyMjI4MzRaFw0xMjA2MTUxMTI4MzRaMDYxNDAyBgNVBAMTK2ZlZGVy"
      + "\\nYXRlZC1zaWdub24uc3lzdGVtLmdzZXJ2aWNlYWNjb3VudC5jb20wgZ8wDQYJKoZI"
      + "\\nhvcNAQEBBQADgY0AMIGJAoGBAMVlf20FzpqZHR7lzNWbbXq5Ol+j+/2gwTtYlgNz"
      + "\\ns6njxEP4oTmViZQsuQABmvYzg7BHOOW2IRE0U2osrfAw97Gg8L/84D0Sdf9sAjr2"
      + "\\nb3F6reVPUYJNDvpvKr6351+N+VRskOVnpqp/rS8k69jHlUYiGTpeQ5MA5n1BUCoF"
      + "\\nJb/vAgMBAAGjODA2MAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgeAMBYGA1Ud"
      + "\\nJQEB/wQMMAoGCCsGAQUFBwMCMA0GCSqGSIb3DQEBBQUAA4GBAHoD+K9ffsDR+XWn"
      + "\\nBODExaCtMTie0l2yRds1wsgc7645PeSYsLB8p4NABI/z28VMD2e7CFzoO2kzNj5I"
      + "\\nKLO2FYliXRw35P3ZJxvxs8aSP0S/U2vlhfDM/W0a4KMF9ATfoWqTaoHG1rWmYOuj"
      + "\\nncTIM79cE3iBrhFqq8HpetXj77Qf\\n-----END CERTIFICATE-----\\n\"\r\n}";

  static class PublicCertsMockHttpTransport extends MockHttpTransport {
    boolean useAgeHeader;

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      return new MockLowLevelHttpRequest() {
          @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse r = new MockLowLevelHttpResponse();
          r.setStatusCode(200);
          r.addHeader("Cache-Control", "max-age=12345");
          if (useAgeHeader) {
            r.addHeader("Age", "42");
          }
          r.setContentType(Json.MEDIA_TYPE);
          r.setContent(TEST_CERTIFICATES);
          return r;
        }
      };
    }
  }

  public void testLoadCerts() throws Exception {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new PublicCertsMockHttpTransport(), new JacksonFactory()).build();

    verifier.loadPublicCerts();
    assertEquals(2, verifier.getPublicKeys().size());
  }

  public void testLoadCerts_cache() throws Exception {
    PublicCertsMockHttpTransport transport = new PublicCertsMockHttpTransport();
    transport.useAgeHeader = true;
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        transport, new JacksonFactory()).setClock(new FixedClock(100)).build();

    verifier.loadPublicCerts();
    assertEquals(2, verifier.getPublicKeys().size());
    assertEquals(100 + (12345 - 42) * 1000, verifier.getExpirationTimeMilliseconds());
  }

  public void testGetCacheTimeInSec() throws Exception {
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(new MockHttpTransport(), new JacksonFactory()).build();
    assertEquals(12000, verifier.getCacheTimeInSec(
        new HttpHeaders().setAge(345L).setCacheControl("max-age=12345")));
    assertEquals(0, verifier.getCacheTimeInSec(new HttpHeaders()));
    assertEquals(0, verifier.getCacheTimeInSec(new HttpHeaders().setAge(345L)));
    assertEquals(0,
        verifier.getCacheTimeInSec(new HttpHeaders().setAge(345L).setCacheControl("max-age=300")));
  }

  public void testVerify() throws Exception {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new PublicCertsMockHttpTransport(), new JacksonFactory()).build();
    Header header = new Header();
    header.setAlgorithm("RS25");
    Payload payload = newPayload(CLIENT_ID);
    Payload payload2 = newPayload(CLIENT_ID + "2");
    GoogleIdToken idToken = new GoogleIdToken(header, payload, new byte[0], new byte[0]);
    GoogleIdToken idToken2 = new GoogleIdToken(header, payload2, new byte[0], new byte[0]);
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    verifier = new GoogleIdTokenVerifier(new PublicCertsMockHttpTransport(), new JacksonFactory());
    assertFalse(verifier.verify(idToken));
    assertFalse(verifier.verify(idToken2));
    // TODO(yanivi): add a unit test that returns true
  }
}
