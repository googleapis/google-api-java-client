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

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.FixedClock;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

/**
 * Tests {@link GooglePublicKeysManager}.
 *
 * @author Yaniv Inbar
 */
public class GooglePublicKeysManagerTest extends TestCase {

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

  private static final int MAX_AGE = 12345;

  private static final int AGE = 42;

  public void testBuilder() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    GooglePublicKeysManager.Builder builder = new GooglePublicKeysManager.Builder(transport, jsonFactory);

    GooglePublicKeysManager certs = builder.build();
    assertEquals(transport, certs.getTransport());
    assertEquals(jsonFactory, certs.getJsonFactory());
  }

  static class PublicCertsMockHttpTransport extends MockHttpTransport {
    boolean useAgeHeader;

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      return new MockLowLevelHttpRequest() {
          @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse r = new MockLowLevelHttpResponse();
          r.setStatusCode(200);
          r.addHeader("Cache-Control", "max-age=" + MAX_AGE);
          if (useAgeHeader) {
            r.addHeader("Age", String.valueOf(AGE));
          }
          r.setContentType(Json.MEDIA_TYPE);
          r.setContent(TEST_CERTIFICATES);
          return r;
        }
      };
    }
  }

  public void testRefresh() throws Exception {
    GooglePublicKeysManager certs = new GooglePublicKeysManager.Builder(
        new PublicCertsMockHttpTransport(), new JacksonFactory()).build();
    certs.refresh();
    assertEquals(2, certs.getPublicKeys().size());
  }

  public void testLoadCerts_cache() throws Exception {
    PublicCertsMockHttpTransport transport = new PublicCertsMockHttpTransport();
    transport.useAgeHeader = true;
    GooglePublicKeysManager certs = new GooglePublicKeysManager.Builder(
        transport, new JacksonFactory()).setClock(new FixedClock(100)).build();

    certs.refresh();
    assertEquals(2, certs.getPublicKeys().size());
    assertEquals(100 + (MAX_AGE - AGE) * 1000, certs.getExpirationTimeMilliseconds());
  }

  public void testGetCacheTimeInSec() throws Exception {
    GooglePublicKeysManager certs =
        new GooglePublicKeysManager.Builder(new MockHttpTransport(), new JacksonFactory()).build();
    assertEquals(12000, certs.getCacheTimeInSec(
        new HttpHeaders().setAge(345L).setCacheControl("max-age=" + MAX_AGE)));
    assertEquals(0, certs.getCacheTimeInSec(new HttpHeaders()));
    assertEquals(0, certs.getCacheTimeInSec(new HttpHeaders().setAge(345L)));
    assertEquals(
        0, certs.getCacheTimeInSec(new HttpHeaders().setAge(345L).setCacheControl("max-age=300")));
  }
}
