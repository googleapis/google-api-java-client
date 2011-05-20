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

import com.google.api.client.json.CustomizeJsonParser;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;

import java.io.IOException;

/**
 * Abstract base class for a Google JSON-C feed parser when the feed class is known in advance.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractJsonFeedParser<T> {

  private boolean feedParsed;
  final JsonParser parser;
  final Class<T> feedClass;

  AbstractJsonFeedParser(JsonParser parser, Class<T> feedClass) {
    this.parser = parser;
    this.feedClass = feedClass;
  }

  /**
   * Parse the feed and return a new parsed instance of the feed class. This method can be skipped
   * if all you want are the items.
   */
  public T parseFeed() throws IOException {
    boolean close = true;
    try {
      this.feedParsed = true;
      T result = parser.parse(this.feedClass, new StopAtItems());
      close = false;
      return result;
    } finally {
      if (close) {
        close();
      }
    }
  }

  final class StopAtItems extends CustomizeJsonParser {
    @Override
    public boolean stopAt(Object context, String key) {
      return "items".equals(key)
          && context.getClass().equals(AbstractJsonFeedParser.this.feedClass);
    }
  }

  /**
   * Parse the next item in the feed and return a new parsed instance of the item class. If there is
   * no item to parse, it will return {@code null} and automatically close the parser (in which case
   * there is no need to call {@link #close()}.
   */
  public Object parseNextItem() throws IOException {
    JsonParser parser = this.parser;
    if (!this.feedParsed) {
      this.feedParsed = true;
      parser.skipToKey("items");
    }
    boolean close = true;
    try {
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        Object result = parseItemInternal();
        close = false;
        return result;
      }
    } finally {
      if (close) {
        close();
      }
    }
    return null;
  }

  /** Closes the underlying parser. */
  public void close() throws IOException {
    this.parser.close();
  }

  abstract Object parseItemInternal() throws IOException;
}
