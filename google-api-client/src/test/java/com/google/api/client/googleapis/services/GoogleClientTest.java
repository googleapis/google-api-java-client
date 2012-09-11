/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.googleapis.services;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Tests {@link GoogleJsonError}.
 *
 * @author Yaniv Inbar
 */
@Deprecated
public class GoogleClientTest extends TestCase {

  @Test
  public void testExecuteUnparsed_error() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) {
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
    GoogleClient client =
        new GoogleClient(transport, jsonFactory, HttpTesting.SIMPLE_URL, "test/", null);
    JsonHttpRequest request = new JsonHttpRequest(client, HttpMethod.GET, "foo", null);
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
