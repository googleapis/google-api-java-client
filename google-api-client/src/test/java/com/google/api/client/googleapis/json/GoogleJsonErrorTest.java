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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;


/**
 * Tests {@link GoogleJsonError}.
 *
 * @author Yaniv Inbar
 */
public class GoogleJsonErrorTest extends TestCase {

  static final JsonFactory FACTORY = new JacksonFactory();
  static final String ERROR = "{" + "\"code\":403," + "\"errors\":[{"
      + "\"domain\":\"usageLimits\"," + "\"message\":\"Access Not Configured\","
      + "\"reason\":\"accessNotConfigured\"" + "}]," + "\"message\":\"Access Not Configured\"}";
  static final String ERROR_RESPONSE = "{\"error\":" + ERROR + "}";

  public void test_json() throws Exception {
    JsonParser parser = FACTORY.createJsonParser(ERROR);
    parser.nextToken();
    GoogleJsonError e = parser.parse(GoogleJsonError.class, null);
    assertEquals(ERROR, FACTORY.toString(e));
  }

  static class ErrorTransport extends MockHttpTransport {
    final MockLowLevelHttpResponse response;

    ErrorTransport() {
      this(ERROR_RESPONSE, Json.MEDIA_TYPE);
    }

    ErrorTransport(String content, String contentType) {
      response = new MockLowLevelHttpResponse().setContent(content)
          .setContentType(contentType).setStatusCode(HttpStatusCodes.STATUS_CODE_FORBIDDEN);
    }

    @Override
    public LowLevelHttpRequest buildRequest(String name, String url) {
      return new MockLowLevelHttpRequest(url).setResponse(response);
    }
  }

  @SuppressWarnings("deprecation")
  public void testParse() throws Exception {
    HttpTransport transport = new ErrorTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonError errorResponse = GoogleJsonError.parse(FACTORY, response);
    assertEquals(ERROR, FACTORY.toString(errorResponse));
  }
}
