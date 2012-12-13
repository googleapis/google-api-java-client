/*
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

package com.google.api.client.googleapis.services.json;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.testing.services.json.MockGoogleJsonClient;
import com.google.api.client.googleapis.testing.services.json.MockGoogleJsonClientRequest;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

/**
 * Tests {@link AbstractGoogleJsonClient}.
 *
 * @author Yaniv Inbar
 */
public class AbstractGoogleJsonClientTest extends TestCase {

  public void testExecuteUnparsed_error() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildRequest(String name, String url) {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setStatusCode(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
            result.setContentType(Json.MEDIA_TYPE);
            result.setContent("{\"error\":{\"code\":401,\"errors\":[{\"domain\":\"global\","
                + "\"location\":\"Authorization\",\"locationType\":\"header\","
                + "\"message\":\"me\",\"reason\":\"authError\"}],\"message\":\"me\"}}");
            return result;
          }
        };
      }
    };
    JsonFactory jsonFactory = new JacksonFactory();
    MockGoogleJsonClient client = new MockGoogleJsonClient.Builder(
        transport, jsonFactory, HttpTesting.SIMPLE_URL, "", null, false).setApplicationName(
        "Test Application").build();
    MockGoogleJsonClientRequest<String> request =
        new MockGoogleJsonClientRequest<String>(client, "GET", "foo", null, String.class);
    try {
      request.executeUnparsed();
      fail("expected " + GoogleJsonResponseException.class);
    } catch (GoogleJsonResponseException e) {
      // expected
      GoogleJsonError details = e.getDetails();
      assertEquals("me", details.getMessage());
      assertEquals("me", details.getErrors().get(0).getMessage());
    }
  }
}
