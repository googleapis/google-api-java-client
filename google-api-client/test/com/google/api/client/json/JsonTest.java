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

package com.google.api.client.json;

import com.google.api.client.util.Key;

import junit.framework.TestCase;

import org.codehaus.jackson.JsonParser;

import java.util.Collection;

/**
 * Tests {@link Json}.
 *
 * @author Yaniv Inbar
 */
public class JsonTest extends TestCase {

  public JsonTest() {
  }

  public JsonTest(String name) {
    super(name);
  }

  private static final String PARSE_ENTRY = "{\"title\":\"foo\"}";

  private static final String PARSE_FEED =
      "{\"entries\":[" + "{\"title\":\"foo\"}," + "{\"title\":\"bar\"}]}";

  public void testParseEntry() throws Exception {
    JsonParser parser = Json.JSON_FACTORY.createJsonParser(PARSE_ENTRY);
    parser.nextToken();
    Entry entry = Json.parseAndClose(parser, Entry.class, null);
    assertEquals("foo", entry.title);
  }

  public void testParseFeed() throws Exception {
    JsonParser parser = Json.JSON_FACTORY.createJsonParser(PARSE_FEED);
    parser.nextToken();
    Feed feed = Json.parseAndClose(parser, Feed.class, null);
    Entry entry = feed.entries.iterator().next();
    assertEquals("foo", entry.title);
  }

  public static class Entry {
    @Key
    public String title;
  }

  public static class Feed {
    @Key
    public Collection<Entry> entries;
  }
}
