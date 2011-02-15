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
import com.google.api.client.json.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes JSON-C content based on the data key/value mapping object for an item, wrapped in a
 * {@code "data"} envelope.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object data) {
    JsonCContent content = new JsonCContent();
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

  @Override
  public void writeTo(OutputStream out) throws IOException {
    JsonGenerator generator = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
    generator.writeStartObject();
    generator.writeFieldName("data");
    generator.serialize(data);
    generator.writeEndObject();
    generator.flush();
  }
}
