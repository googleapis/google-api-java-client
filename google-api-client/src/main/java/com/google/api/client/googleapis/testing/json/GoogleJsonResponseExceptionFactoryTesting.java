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

package com.google.api.client.googleapis.testing.json;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Beta;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Factory class that builds {@link GoogleJsonResponseException} instances for
 * testing.
 *
 * @since 1.18
 */
@Beta
public final class GoogleJsonResponseExceptionFactoryTesting {

  /**
   * Convenience factory method that builds a {@link GoogleJsonResponseException}
   * from its arguments. The method builds a dummy {@link HttpRequest} and
   * {@link HttpResponse}, sets the response's status to a user-specified HTTP
   * error code, suppresses exceptions, and executes the request. This forces
   * the underlying framework to create, but not throw, a
   * {@link GoogleJsonResponseException}, which the method retrieves and returns
   * to the invoker.
   *
   * @param jsonFactory the JSON factory that will create all JSON required
   *        by the underlying framework
   * @param httpCode the desired HTTP error code. Note: do nut specify any codes
   *        that indicate successful completion, e.g. 2XX.
   * @param reasonPhrase the HTTP reason code that explains the error. For example,
   *        if {@code httpCode} is {@code 404}, the reason phrase should be
   *        {@code NOT FOUND}.
   * @return the generated {@link GoogleJsonResponseException}, as specified.
   * @throws IOException if request transport fails.
   */
  public static GoogleJsonResponseException newMock(JsonFactory jsonFactory,
      int httpCode, String reasonPhrase) throws IOException {
    MockLowLevelHttpResponse otherServiceUnavaiableLowLevelResponse =
        new MockLowLevelHttpResponse()
        .setStatusCode(httpCode)
        .setReasonPhrase(reasonPhrase);
    MockHttpTransport otherTransport = MockHttpTransport.builder()
        .setLowLevelHttpResponse(otherServiceUnavaiableLowLevelResponse)
        .build();
    HttpRequest otherRequest = otherTransport
        .createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    otherRequest.setThrowExceptionOnExecuteError(false);
    HttpResponse otherServiceUnavailableResponse = otherRequest.execute();
    return GoogleJsonResponseException.from(jsonFactory, otherServiceUnavailableResponse);
  }
}
