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

package com.google.api.client.http.json;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses HTTP JSON response content into an data class of key/value pairs.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setParser(HttpTransport transport) {
    transport.addParser(new JsonHttpParser());
  }
 * </code>
 * </pre>
 *
 * <p>
 * Upgrade warning: in prior version 1.2 this class was previously in the
 * {@link com.google.api.client.json} package.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonHttpParser implements HttpParser {

  /** Content type. Default value is {@link Json#CONTENT_TYPE}. */
  public String contentType = Json.CONTENT_TYPE;

  /**
   * JSON factory to use.
   *
   * @since 1.3
   */
  public JsonFactory jsonFactory;

  public final String getContentType() {
    return contentType;
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    return parserForResponse(jsonFactory, response).parseAndClose(dataClass, null);
  }

  /**
   * Returns a JSON parser to use for parsing the given HTTP response.
   * <p>
   * The response content will be closed if any throwable is thrown. On success, the current token
   * will be the first top token, which is normally {@link JsonToken#START_ARRAY} or
   * {@link JsonToken#START_OBJECT}.
   * </p>
   * <p>
   * Upgrade warning: prior to version 1.3, there was no {@code jsonFactory} parameter, but now it
   * is required.
   * </p>
   *
   * @param jsonFactory JSON factory to use
   * @param response HTTP response
   * @return JSON parser
   * @throws IllegalArgumentException if content type is not {@link Json#CONTENT_TYPE}
   * @throws IOException I/O exception
   * @since 1.3
   */
  public static JsonParser parserForResponse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    InputStream content = response.getContent();
    try {
      JsonParser parser = jsonFactory.createJsonParser(content);
      parser.nextToken();
      content = null;
      return parser;
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }
}
