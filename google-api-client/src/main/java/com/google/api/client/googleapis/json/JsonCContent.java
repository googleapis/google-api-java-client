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

import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes JSON-C content based on the data key/value mapping object for an item, wrapped in a
 * {@code "data"} envelope.
 *
 * <p>
 * Warning: this should only be used by some older Google APIs that wrapped the response in a {@code
 * "data"} envelope. All newer Google APIs don't use this envelope, and for those APIs
 * {@link JsonHttpContent} should be used instead.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object data) {
    JsonCContent content = new JsonCContent();
    content.jsonFactory = new JacksonFactory();
    content.data = data;
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class JsonCContent extends JsonHttpContent {

  /**
   * @param jsonFactory JSON factory to use
   * @param data JSON key name/value data
   * @since 1.5
   */
  public JsonCContent(JsonFactory jsonFactory, Object data) {
    super(jsonFactory, data);
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    JsonGenerator generator = getJsonFactory().createJsonGenerator(out, JsonEncoding.UTF8);
    generator.writeStartObject();
    generator.writeFieldName("data");
    generator.serialize(getData());
    generator.writeEndObject();
    generator.flush();
  }
}
