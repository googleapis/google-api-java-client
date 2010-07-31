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
import com.google.api.client.json.CustomizeJsonParser;
import com.google.api.client.json.Json;

import org.codehaus.jackson.JsonParser;

import java.io.IOException;
import java.io.InputStream;

/**
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in version 1.1)
 */
@Deprecated
public class JsonHttp {

  /**
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link JsonFeedParser#use(HttpResponse, Class, Class)}
   */
  @Deprecated
  public static <T, I> JsonFeedParser<T, I> useFeedParser(
      HttpResponse response, Class<T> feedClass, Class<I> itemClass)
      throws IOException {
    JsonParser parser = JsonCParser.parserForResponse(response);
    return new JsonFeedParser<T, I>(parser, feedClass, itemClass);
  }

  /**
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link JsonFeedParser#use(HttpResponse, Class, Class)}
   */
  @Deprecated
  public static <T, I> JsonMultiKindFeedParser<T> useMultiKindFeedParser(
      HttpResponse response, Class<T> feedClass, Class<?>... itemClasses)
      throws IOException {
    return new JsonMultiKindFeedParser<T>(JsonHttp
        .processAsJsonParser(response), feedClass, itemClasses);
  }

  /**
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link JsonCParser#parserForResponse(HttpResponse)} and then
   *             {@link Json#parseAndClose(JsonParser, Class, CustomizeJsonParser)}
   */
  @Deprecated
  public static <T> T parse(HttpResponse response,
      Class<T> classToInstantiateAndParse) throws IOException {
    JsonParser parser = processAsJsonParser(response);
    return Json.parseAndClose(parser, classToInstantiateAndParse, null);
  }

  /**
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link JsonCParser#parserForResponse(HttpResponse)}
   */
  @Deprecated
  public static JsonParser processAsJsonParser(HttpResponse response)
      throws IOException {
    InputStream content = response.getContent();
    try {
      // check for JSON content type
      String contentType = response.contentType;
      if (!contentType.startsWith(Json.CONTENT_TYPE)) {
        throw new IllegalArgumentException("Wrong content type: expected <"
            + Json.CONTENT_TYPE + "> but got <" + contentType + ">");
      }
      JsonParser parser = Json.JSON_FACTORY.createJsonParser(content);
      content = null;
      parser.nextToken();
      Json.skipToKey(parser, response.isSuccessStatusCode ? "data" : "error");
      return parser;
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }
}
