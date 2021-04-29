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

import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleJsonError}.
 *
 * @author Yaniv Inbar
 */
public class GoogleJsonErrorTest extends TestCase {

  static final JsonFactory FACTORY = new GsonFactory();
  static final String ERROR =
      "{\"code\":400,"
          + "\"message\":\"The template parameters are invalid.\","
          + "\"status\":\"INVALID_ARGUMENT\","
          + "\"details\":[{"
          + "\"@type\":\"type.googleapis.com/google.dataflow.v1beta3.InvalidTemplateParameters\","
          + "\"parameterViolations\":[{"
          + "\"parameter\":\"safeBrowsingApiKey\","
          + "\"description\":\"Parameter didn't match regex '^[0-9a-zA-Z_]+$'\""
          + "}]},{"
          + "\"@type\":\"type.googleapis.com/google.rpc.DebugInfo\","
          + "\"detail\":\"test detail\"}]}";
  static final String ERROR_RESPONSE = "{\"error\":" + ERROR + "}";

  public void test_json() throws Exception {
    JsonParser parser = FACTORY.createJsonParser(ERROR);
    parser.nextToken();
    GoogleJsonError e = parser.parse(GoogleJsonError.class);
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

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      return new MockLowLevelHttpRequest(url).setResponse(response);
    }
  }

  public void testParse() throws Exception {
    String testError ="{"
        + "\"code\": 400,"
        + "\"message\": \"The template parameters are invalid.\","
        + "\"status\": \"INVALID_ARGUMENT\","
        + "\"details\": [{"
        + "\"@type\": \"type.googleapis.com\\/google.dataflow.v1beta3.InvalidTemplateParameters\","
        + "\"parameterViolations\": [{"
        + "\"parameter\": \"safeBrowsingApiKey\","
        + "\"description\": \"Parameter didn't match regex '^[0-9a-zA-Z_]+$'\""
        + "}]},{"
        + "\"@type\": \"type.googleapis.com\\/google.rpc.DebugInfo\","
        + "\"detail\": \"test detail\"}]}";
    InputStream errorContent = GoogleJsonErrorTest.class.getResourceAsStream("error.json");
    JsonObjectParser parser = FACTORY.createJsonObjectParser();
    GoogleJsonError error = parser.parseAndClose(errorContent, StandardCharsets.UTF_8, GoogleJsonError.class);
    System.out.println(error);
    System.out.println(error.get("status"));
    System.out.println(error.get("details"));
    System.out.println(error.get("details").getClass());
    System.out.println(((List)error.get("details")).get(0).getClass());
//    System.out.println(error.getDetails());
//    System.out.println(error.getDetails().get(0).getParameterViolations());

//    HttpTransport transport = new ErrorTransport(testError, Json.MEDIA_TYPE);
//    HttpRequest request =
//        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
//    request.setThrowExceptionOnExecuteError(false);
//    HttpResponse response = request.execute();
//    GoogleJsonError errorResponse = GoogleJsonError.parse(FACTORY, response);
//    System.out.println(errorResponse.getCode());
//    System.out.println(errorResponse.getMessage());
//
//    ErrorInfo errorInfoList = errorResponse.getError();
//    System.out.println(errorInfoList);
//    assertEquals(ERROR, FACTORY.toString(errorResponse));
  }
}
