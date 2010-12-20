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

package com.google.api.client.json;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;

import org.codehaus.jackson.JsonParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parses HTTP JSON response content into an data class of key/value pairs.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
 * static void setParser(HttpTransport transport) {
 *   transport.addParser(new JsonHttpParser());
 * }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonHttpParser implements HttpParser {

  /** Content type. Default value is {@link Json#CONTENT_TYPE}. */
  public String contentType = Json.CONTENT_TYPE;

  public final String getContentType() {
    return contentType;
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    return Json.parseAndClose(JsonHttpParser.parserForResponse(response), dataClass, null);
  }

  /**
   * Returns a JSON parser to use for parsing the given HTTP response.
   * <p>
   * The response content will be closed if any throwable is thrown. On success, the current token
   * will be the first key in the JSON object.
   *
   * @param response HTTP response
   * @return JSON parser
   * @throws IllegalArgumentException if content type is not {@link Json#CONTENT_TYPE}
   * @throws IOException I/O exception
   */
  public static JsonParser parserForResponse(HttpResponse response) throws IOException {
    InputStream content = response.getContent();
    try {
      JsonParser parser = Json.JSON_FACTORY.createJsonParser(content);
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
