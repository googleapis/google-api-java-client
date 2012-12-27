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

package com.google.api.client.googleapis.services;

import com.google.api.client.googleapis.testing.services.MockGoogleClient;
import com.google.api.client.googleapis.testing.services.MockGoogleClientRequest;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests {@link AbstractGoogleClientRequest}.
 *
 * @author Yaniv Inbar
 */
public class AbstractGoogleClientRequestTest extends TestCase {

  private static final String ROOT_URL = "https://www.googleapis.com/test/";
  private static final String SERVICE_PATH = "path/v1/";
  private static final String URI_TEMPLATE = "tests/{testId}";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final JsonObjectParser JSON_OBJECT_PARSER = new JsonObjectParser(JSON_FACTORY);
  private static final String ERROR_CONTENT =
      "{\"error\":{\"code\":401,\"errors\":[{\"domain\":\"global\","
      + "\"location\":\"Authorization\",\"locationType\":\"header\","
      + "\"message\":\"me\",\"reason\":\"authError\"}],\"message\":\"me\"}}";

  public void testExecuteUnparsed_error() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            assertEquals("GET", method);
            assertEquals("https://www.googleapis.com/test/path/v1/tests/foo", url);
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setStatusCode(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
            result.setContentType(Json.MEDIA_TYPE);
            result.setContent(ERROR_CONTENT);
            return result;
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    try {
      request.put("testId", "foo");
      request.executeUnparsed();
      fail("expected " + HttpResponseException.class);
    } catch (HttpResponseException e) {
      // expected
      assertEquals("401" + StringUtils.LINE_SEPARATOR + ERROR_CONTENT, e.getMessage());
    }
  }

  public void testExecuteUsingHead() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            assertEquals("HEAD", method);
            assertEquals("https://www.googleapis.com/test/path/v1/tests/foo", url);
            return new MockLowLevelHttpResponse();
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    request.put("testId", "foo");
    request.executeUsingHead();
  }

  public void testBuildHttpRequest_emptyContent() throws Exception {
    for (String method : Arrays.asList("GET", "HEAD", "DELETE", "FOO")) {
      subtestBuildHttpRequest_emptyContent(method, false);
    }
    for (String method : Arrays.asList("POST", "PUT", "PATCH")) {
      subtestBuildHttpRequest_emptyContent(method, true);
    }
  }

  private void subtestBuildHttpRequest_emptyContent(String method, boolean expectEmptyContent)
      throws Exception {
    HttpTransport transport = new MockHttpTransport();
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request =
        new MockGoogleClientRequest<String>(client, method, URI_TEMPLATE, null, String.class);
    HttpRequest httpRequest = request.buildHttpRequest();
    if (expectEmptyContent) {
      assertTrue(httpRequest.getContent() instanceof EmptyContent);
    } else {
      assertNull(httpRequest.getContent());
    }
  }

  @Deprecated
  public void testBuildHttpRequest() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    assertFalse(request.getDisableGZipContent());
    HttpRequest httpRequest = request.buildHttpRequest();
    assertTrue(httpRequest.getEnableGZipContent());
    request.setDisableGZipContent(true);
    httpRequest = request.buildHttpRequest();
    assertFalse(httpRequest.getEnableGZipContent());
  }
}
