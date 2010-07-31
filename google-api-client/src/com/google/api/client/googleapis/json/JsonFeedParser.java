/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.Json;

import org.codehaus.jackson.JsonParser;

import java.io.IOException;

/**
 * Google JSON-C feed parser when the item class is known in advance.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class JsonFeedParser<T, I> extends AbstractJsonFeedParser<T> {

  private final Class<I> itemClass;

  public JsonFeedParser(JsonParser parser, Class<T> feedClass,
      Class<I> itemClass) {
    super(parser, feedClass);
    this.itemClass = itemClass;
  }

  @SuppressWarnings("unchecked")
  @Override
  public I parseNextItem() throws IOException {
    return (I) super.parseNextItem();
  }

  @Override
  Object parseItemInternal() throws IOException {
    return Json.parse(parser, itemClass, null);
  }

  public static <T, I> JsonFeedParser<T, I> use(HttpResponse response,
      Class<T> feedClass, Class<I> itemClass) throws IOException {
    JsonParser parser = JsonCParser.parserForResponse(response);
    return new JsonFeedParser<T, I>(parser, feedClass, itemClass);
  }
}
