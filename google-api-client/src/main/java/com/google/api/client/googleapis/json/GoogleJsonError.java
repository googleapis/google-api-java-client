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

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

/**
 * Data class representing the Google JSON error response content, as documented for example in <a
 * href="http://code.google.com/apis/buzz/v1/using_rest.html#errors">Error Messages in Google
 * Buzz</a>.
 *
 * <p>
 * Parse the error response using {@link #parse(JsonFactory, HttpResponse)}. Sample usage:
 * </p>
 *
 * <pre>
    try {
      request.execute();
    } catch (HttpResponseException e) {
        GoogleJsonError errorResponse = GoogleJsonError.parse(factory, e.response);
        System.err.println(errorResponse.code + " Error: " + errorResponse.message);
        for (ErrorInfo error : errorResponse.errors) {
          System.err.println(factory.toString(error));
        }
    } catch (IOException e) {
...
    }
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class GoogleJsonError extends GenericJson {

  /**
   * Parses the given error HTTP response using the given JSON factory.
   *
   * @param jsonFactory JSON factory
   * @param response HTTP response
   * @return new instance of the Google JSON error information
   * @throws IllegalArgumentException if content type is not {@link Json#CONTENT_TYPE} or if
   *         expected {@code "data"} or {@code "error"} key is not found
   */
  public static GoogleJsonError parse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    return JsonCParser.parserForResponse(jsonFactory, response).parseAndClose(
        GoogleJsonError.class, null);
  }

  /** Detailed error information. */
  public static class ErrorInfo extends GenericJson {

    /** Specifies the error classification. */
    @Key
    public String domain;

    /** Specifies the error code. */
    @Key
    public String reason;

    /** A human readable explanation of the error. */
    @Key
    public String message;

    /** Specifies the location of the error or {@code null} for none. */
    @Key
    public String location;

    /** Specifies the type of location of the error or {@code null} for none. */
    @Key
    public String locationType;
  }

  /** List of detailed errors. */
  @Key
  public List<ErrorInfo> errors;

  /** Error code. */
  @Key
  public int code;

  /** Error message. */
  @Key
  public String message;
}
