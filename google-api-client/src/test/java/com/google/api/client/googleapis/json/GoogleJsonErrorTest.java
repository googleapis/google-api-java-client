/*
 * Copyright 2011 Google Inc.
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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.TestCase;

/**
 * Tests {@link com.google.api.client.googleapis.json.GoogleJsonError}.
 *
 * @author Yaniv Inbar
 */
public class GoogleJsonErrorTest extends TestCase {

  public static final com.google.gson.JsonParser JSON_PARSER = new com.google.gson.JsonParser();
  static final JsonFactory FACTORY = new GsonFactory();
  static final String ERROR =
      "{"
          + "\"code\":403,"
          + "\"errors\":[{"
          + "\"domain\":\"usageLimits\","
          + "\"message\":\"Access Not Configured\","
          + "\"reason\":\"accessNotConfigured\""
          + "}],"
          + "\"message\":\"Access Not Configured\"}";
  static final String ERROR_RESPONSE = "{\"error\":" + ERROR + "}";

  public void test_json() throws Exception {
    JsonParser parser = FACTORY.createJsonParser(ERROR);
    parser.nextToken();
    com.google.api.client.googleapis.json.GoogleJsonError e =
        parser.parse(com.google.api.client.googleapis.json.GoogleJsonError.class);
    assertEquals(ERROR, FACTORY.toString(e));
  }

  static class ErrorTransport extends MockHttpTransport {
    final MockLowLevelHttpResponse response;

    ErrorTransport() {
      this(ERROR_RESPONSE, Json.MEDIA_TYPE);
    }

    ErrorTransport(String content, String contentType) {
      response =
          new MockLowLevelHttpResponse()
              .setContent(content)
              .setContentType(contentType)
              .setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
    }

    ErrorTransport(MockLowLevelHttpResponse mockLowLevelHttpResponse) {
      response = mockLowLevelHttpResponse;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      return new MockLowLevelHttpRequest(url).setResponse(response);
    }
  }

  public void testParse() throws Exception {
    HttpTransport transport = new ErrorTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    com.google.api.client.googleapis.json.GoogleJsonError errorResponse =
        com.google.api.client.googleapis.json.GoogleJsonError.parse(FACTORY, response);
    assertEquals(ERROR, FACTORY.toString(errorResponse));
  }

  public void testParse_withMultipleErrorTypesInDetails() throws Exception {
    InputStream errorResponseStream =
        GoogleJsonErrorTest.class.getResourceAsStream(
            "errorResponseWithMultipleTypesInDetails.json");

    InputStream expectedParsedErrorResponse =
        GoogleJsonErrorTest.class.getResourceAsStream(
            "expectedParsedErrorWithMultipleTypesInDetails.json");

    HttpTransport transport =
        new ErrorTransport(
            new MockLowLevelHttpResponse()
                .setContent(errorResponseStream)
                .setContentType(Json.MEDIA_TYPE)
                .setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN));
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    com.google.api.client.googleapis.json.GoogleJsonError actualParsedErrorResponse =
        com.google.api.client.googleapis.json.GoogleJsonError.parse(FACTORY, response);
    assertEquals(
        JSON_PARSER.parse(new InputStreamReader(expectedParsedErrorResponse)),
        JSON_PARSER.parse(FACTORY.toString(actualParsedErrorResponse)));
  }

  public void testParse_withDetails() throws Exception {
    String DETAILS_ERROR =
        "{"
            + "\"code\":400,"
            + "\"details\":[{"
            + "\"@type\":\"type.googleapis.com/google.dataflow.v1beta3.InvalidTemplateParameters\","
            + "\"parameterViolations\":[{"
            + "\"description\":\"Parameter didn't match regex '^[0-9a-zA-Z_]+$'\","
            + "\"parameter\":\"safeBrowsingApiKey\""
            + "}]},{"
            + "\"@type\":\"type.googleapis.com/google.rpc.DebugInfo\","
            + "\"detail\":\"test detail\"}],"
            + "\"message\":\"The template parameters are invalid.\","
            + "\"status\":\"INVALID_ARGUMENT\""
            + "}";
    InputStream errorContent = GoogleJsonErrorTest.class.getResourceAsStream("error.json");
    HttpTransport transport =
        new ErrorTransport(
            new MockLowLevelHttpResponse()
                .setContent(errorContent)
                .setContentType(Json.MEDIA_TYPE)
                .setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN));
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    com.google.api.client.googleapis.json.GoogleJsonError errorResponse =
        com.google.api.client.googleapis.json.GoogleJsonError.parse(FACTORY, response);

    assertEquals(
        JSON_PARSER.parse(DETAILS_ERROR), JSON_PARSER.parse(FACTORY.toString(errorResponse)));
    assertNotNull(errorResponse.getDetails());
  }

  public void testParse_withReasonInDetails() throws Exception {
    String DETAILS_ERROR =
        "{"
            + "\"code\":400,"
            + "\"details\":"
            + "[{"
            + "\"@type\":\"type.googleapis.com/google.dataflow.v1beta3.InvalidTemplateParameters\","
            + "\"parameterViolations\":[{"
            + "\"description\":\"Parameter didn't match regex '^[0-9a-zA-Z_]+$'\","
            + "\"parameter\":\"safeBrowsingApiKey\""
            + "}],"
            + "\"reason\":\"TEST REASON 1\""
            + "},{"
            + "\"@type\":\"type.googleapis.com/google.rpc.DebugInfo\","
            + "\"detail\":\"test detail\""
            + "},{"
            + "\"@type\":\"type.googleapis.com/google.rpc.DebugInfo\","
            + "\"reason\":\"test reason 2\""
            + "},{"
            + "\"@type\":\"type.googleapis.com/google.rpc.DebugInfo\""
            + "}],"
            + "\"message\":\"The template parameters are invalid.\","
            + "\"status\":\"INVALID_ARGUMENT\""
            + "}";
    InputStream errorContent =
        GoogleJsonErrorTest.class.getResourceAsStream("errorWithReasonInDetails.json");

    HttpTransport transport =
        new ErrorTransport(
            new MockLowLevelHttpResponse()
                .setContent(errorContent)
                .setContentType(Json.MEDIA_TYPE)
                .setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN));
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    com.google.api.client.googleapis.json.GoogleJsonError errorResponse =
        com.google.api.client.googleapis.json.GoogleJsonError.parse(FACTORY, response);

    assertEquals(
        JSON_PARSER.parse(DETAILS_ERROR), JSON_PARSER.parse(FACTORY.toString(errorResponse)));
    assertNotNull(errorResponse.getDetails().get(2).getReason());
  }
}
