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

import com.google.api.client.http.HttpContent;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes JSON HTTP content based on the data key/value mapping object for an item.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object data) {
    JsonHttpContent content = new JsonHttpContent();
    content.data = data;
    request.content = content;
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
public class JsonHttpContent implements HttpContent {
  // TODO: ability to annotate fields as only needed for POST?

  /** Content type. Default value is {@link Json#CONTENT_TYPE}. */
  public String contentType = Json.CONTENT_TYPE;

  /** Key/value pair data. */
  public Object data;

  /**
   * JSON factory to use.
   *
   * @since 1.3
   */
  public JsonFactory jsonFactory;

  public long getLength() {
    // TODO
    return -1;
  }

  public final String getEncoding() {
    return null;
  }

  public String getType() {
    return Json.CONTENT_TYPE;
  }

  public void writeTo(OutputStream out) throws IOException {
    JsonGenerator generator = jsonFactory.createJsonGenerator(out, JsonEncoding.UTF8);
    generator.serialize(data);
    generator.flush();
  }
}
