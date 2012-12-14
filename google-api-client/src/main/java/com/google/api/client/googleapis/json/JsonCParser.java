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

import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Parses JSON-C response content into an data class of key/value pairs, assuming the data is
 * wrapped in a {@code "data"} envelope.
 *
 * <p>
 * Warning: this should only be used by some older Google APIs that wrapped the response in a
 * {@code "data"} envelope. All newer Google APIs don't use this envelope, and for those APIs
 * {@link JsonObjectParser} should be used instead.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setParser(HttpRequest request) {
    request.setParser(new JsonCParser(new JacksonFactory()));
  }
 * </code>
 * </pre>
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * Upgrade warning: this class now extends {@link JsonObjectParser}, whereas in prior version 1.11
 * it extended {@link com.google.api.client.http.json.JsonHttpParser}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@SuppressWarnings("javadoc")
public final class JsonCParser extends JsonObjectParser {
  private final JsonFactory jsonFactory;

  /**
   * Returns the JSON factory used for parsing.
   *
   * @since 1.10
   */
  public final JsonFactory getFactory() {
    return jsonFactory;
  }

  /**
   * @param jsonFactory non-null JSON factory used for parsing
   * @since 1.5
   */
  public JsonCParser(JsonFactory jsonFactory) {
    super(jsonFactory);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
  }

  /**
   * Initializes a JSON parser to use for parsing by skipping over the {@code "data"} or
   * {@code "error"} envelope.
   *
   * <p>
   * The parser will be closed if any throwable is thrown. The current token will be the value of
   * the {@code "data"} or {@code "error} key.
   * </p>
   *
   * @param parser the parser which should be initialized for normal parsing
   * @throws IllegalArgumentException if content type is not {@link Json#MEDIA_TYPE} or if expected
   *         {@code "data"} or {@code "error"} key is not found
   * @return the parser which was passed as a parameter
   * @since 1.10
   */
  public static JsonParser initializeParser(JsonParser parser) throws IOException {
    // parse
    boolean failed = true;
    try {
      String match = parser.skipToKey(new HashSet<String>(Arrays.asList("data", "error")));
      if (match == null || parser.getCurrentToken() == JsonToken.END_OBJECT) {
        throw new IllegalArgumentException("data key not found");
      }
      failed = false;
    } finally {
      if (failed) {
        parser.close();
      }
    }
    return parser;
  }

  @Override
  public Object parseAndClose(InputStream in, Charset charset, Type dataType) throws IOException {
    return initializeParser(jsonFactory.createJsonParser(in, charset)).parse(dataType, true, null);
  }

  @Override
  public Object parseAndClose(Reader reader, Type dataType) throws IOException {
    return initializeParser(jsonFactory.createJsonParser(reader)).parse(dataType, true, null);
  }
}
