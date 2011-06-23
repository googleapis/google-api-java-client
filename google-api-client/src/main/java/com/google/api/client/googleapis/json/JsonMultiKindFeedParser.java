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
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Types;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Google JSON-C feed parser when the item class can be computed from the kind.
 *
 * @param <T> feed type
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class JsonMultiKindFeedParser<T> extends AbstractJsonFeedParser<T> {

  private final HashMap<String, Class<?>> kindToItemClassMap = new HashMap<String, Class<?>>();

  /**
   * @param parser JSON parser
   * @param feedClass feed class
   * @param itemClasses item classes
   */
  public JsonMultiKindFeedParser(JsonParser parser, Class<T> feedClass, Class<?>... itemClasses) {
    super(parser, feedClass);
    int numItems = itemClasses.length;
    HashMap<String, Class<?>> kindToItemClassMap = this.kindToItemClassMap;
    for (int i = 0; i < numItems; i++) {
      Class<?> itemClass = itemClasses[i];
      ClassInfo classInfo = ClassInfo.of(itemClass);
      Field field = classInfo.getField("kind");
      if (field == null) {
        throw new IllegalArgumentException("missing kind field for " + itemClass.getName());
      }
      Object item = Types.newInstance(itemClass);
      String kind = (String) FieldInfo.getFieldValue(field, item);
      if (kind == null) {
        throw new IllegalArgumentException(
            "missing value for kind field in " + itemClass.getName());
      }
      kindToItemClassMap.put(kind, itemClass);
    }
  }

  @Override
  Object parseItemInternal() throws IOException {
    parser.nextToken();
    String key = parser.getText();
    if (key != "kind") {
      throw new IllegalArgumentException("expected kind field: " + key);
    }
    parser.nextToken();
    String kind = parser.getText();
    Class<?> itemClass = kindToItemClassMap.get(kind);
    if (itemClass == null) {
      throw new IllegalArgumentException("unrecognized kind: " + kind);
    }
    return parser.parse(itemClass, null);
  }

  /**
   * Parses the given HTTP response using the given feed class and item classes.
   *
   * @param jsonFactory JSON factory
   * @param response HTTP response
   * @param feedClass feed class
   * @param itemClasses item classes
   * @since 1.3
   */
  public static <T, I> JsonMultiKindFeedParser<T> use(
      JsonFactory jsonFactory, HttpResponse response, Class<T> feedClass, Class<?>... itemClasses)
      throws IOException {
    return new JsonMultiKindFeedParser<T>(
        JsonCParser.parserForResponse(jsonFactory, response), feedClass, itemClasses);
  }
}
