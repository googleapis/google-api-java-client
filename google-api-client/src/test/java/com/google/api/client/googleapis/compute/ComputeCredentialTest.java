/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.compute;

import static com.google.common.collect.Iterables.getOnlyElement;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

/**
 * Tests {@link ComputeCredential}.
 *
 * @author Yaniv Inbar
 */
public class ComputeCredentialTest extends TestCase {

  static final String ACCESS_TOKEN = "ya29.AHES6ZRN3-HlhAPya30GnW_bHSb_QtAS08i85nHq39HE3C2LTrCARA";

  static final long EXPIRES_IN_SECONDS = 3599L;

  static final String TOKEN_TYPE = "Bearer";

  static final String URL =
      "http://metadata/computeMetadata/v1/instance/service-accounts/default/token";

  public void testExecuteRefreshToken() throws Exception {
    String content = "{" + "\"access_token\":\"" + ACCESS_TOKEN + "\"," + "\"expires_in\":"
        + EXPIRES_IN_SECONDS + "," + "\"token_type\":\"" + TOKEN_TYPE + "\"" + "}";
    final MockLowLevelHttpRequest request = new MockLowLevelHttpRequest(URL).setResponse(
        new MockLowLevelHttpResponse().setContent(content).setContentType(Json.MEDIA_TYPE));

    HttpTransport transport = new MockHttpTransport() {
      private int count = 0;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) {
        assertEquals(1, ++count);
        assertEquals("GET", method);
        assertEquals(URL, url);

        return request;
      }
    };

    ComputeCredential cred = new ComputeCredential(transport, new JacksonFactory());
    TokenResponse response = cred.executeRefreshToken();
    assertEquals(ACCESS_TOKEN, response.getAccessToken());
    assertEquals(EXPIRES_IN_SECONDS, response.getExpiresInSeconds().longValue());
    assertEquals(TOKEN_TYPE, response.getTokenType());

    // Verify that the request had the metadata-request header configured properly
    assertEquals("true", getOnlyElement(request.getHeaderValues(
        "X-Google-Metadata-Request")));
  }
}
