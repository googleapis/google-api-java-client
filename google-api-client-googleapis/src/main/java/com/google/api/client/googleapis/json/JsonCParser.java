/*
 * Copyright (c) 2010 Google Inc.
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
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import java.io.IOException;

/**
 * Parses HTTP JSON-C response content into an data class of key/value pairs, assuming the data is
 * wrapped in a {@code "data"} envelope.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setParser(HttpTransport transport) {
    transport.addParser(new JsonCParser());
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class JsonCParser extends JsonHttpParser {

  @Override
  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    return parserForResponse(jsonFactory, response).parseAndClose(dataClass, null);
  }

  /**
   * Returns a JSON parser to use for parsing the given HTTP response, skipped over the {@code
   * "data"} envelope.
   * <p>
   * The parser will be closed if any throwable is thrown. The current token will be the value of
   * the {@code "data"} key.
   * </p>
   * <p>
   * Upgrade warning: prior to version 1.3, there was no {@code jsonFactory} parameter, but now it
   * is required.
   * </p>
   *
   * @param response HTTP response
   * @return JSON parser
   * @throws IllegalArgumentException if content type is not {@link Json#CONTENT_TYPE} or if {@code
   *         "data"} key is not found
   * @throws IOException I/O exception
   * @since 1.3
   */
  public static JsonParser parserForResponse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    // check for JSON content type
    String contentType = response.contentType;
    if (contentType == null || !contentType.startsWith(Json.CONTENT_TYPE)) {
      throw new IllegalArgumentException(
          "Wrong content type: expected <" + Json.CONTENT_TYPE + "> but got <" + contentType + ">");
    }
    // parse
    boolean failed = true;
    JsonParser parser = JsonHttpParser.parserForResponse(jsonFactory, response);
    try {
      parser.skipToKey(response.isSuccessStatusCode ? "data" : "error");
      if (parser.getCurrentToken() == JsonToken.END_OBJECT) {
        throw new IllegalArgumentException("data key not found");
      }
      failed = false;
      return parser;
    } finally {
      if (failed) {
        parser.close();
      }
    }
  }
}
