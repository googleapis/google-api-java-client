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

package com.google.api.client.testing.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstract test case for {@link JsonParser}.
 *
 * @author Yaniv Inbar
 */
public abstract class AbstractJsonParserTest extends TestCase {

  public AbstractJsonParserTest(String name) {
    super(name);
  }

  protected abstract JsonFactory newFactory();

  private static final String JSON_ENTRY = "{\"title\":\"foo\"}";

  private static final String JSON_FEED =
      "{\"entries\":[" + "{\"title\":\"foo\"}," + "{\"title\":\"bar\"}]}";

  public void testParseEntry() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    Entry entry = parser.parseAndClose(Entry.class, null);
    assertEquals("foo", entry.title);
  }

  public void testParseFeed() throws Exception {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    parser.nextToken();
    Feed feed = parser.parseAndClose(Feed.class, null);
    Iterator<Entry> iterator = feed.entries.iterator();
    assertEquals("foo", iterator.next().title);
    assertEquals("bar", iterator.next().title);
    assertFalse(iterator.hasNext());
  }

  @SuppressWarnings("unchecked")
  public void testParseEntryAsMap() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    HashMap<String, Object> map = parser.parseAndClose(HashMap.class, null);
    assertEquals("foo", map.remove("title"));
    assertTrue(map.isEmpty());
  }

  public void testSkipToKey_missing() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("missing");
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public void testSkipToKey_found() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("title");
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipToKey_startWithFieldName() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.nextToken();
    parser.skipToKey("title");
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipChildren_string() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipToKey("title");
    parser.skipChildren();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    assertEquals("foo", parser.getText());
  }

  public void testSkipChildren_object() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_ENTRY);
    parser.nextToken();
    parser.skipChildren();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
  }

  public void testSkipChildren_array() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    parser.nextToken();
    parser.skipToKey("entries");
    parser.skipChildren();
    assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
  }

  public void testNextToken() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.START_ARRAY, parser.nextToken());
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertEquals(JsonToken.START_OBJECT, parser.nextToken());
    assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
    assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    assertNull(parser.nextToken());
  }

  public void testCurrentToken() throws IOException {
    JsonParser parser = newFactory().createJsonParser(JSON_FEED);
    assertNull(parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
    parser.nextToken();
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    parser.nextToken();
    assertNull(parser.getCurrentToken());
  }

  public static class Entry {
    @Key
    public String title;
  }

  public static class Feed {
    @Key
    public Collection<Entry> entries;
  }

  public static class A {
    @Key
    public Map<String, String> map;
  }

  static final String CONTAINED_MAP = "{\"map\":{\"title\":\"foo\"}}";

  public void testParse() throws IOException {
    JsonParser parser = newFactory().createJsonParser(CONTAINED_MAP);
    parser.nextToken();
    A a = parser.parse(A.class, null);
    assertEquals(ImmutableMap.of("title", "foo"), a.map);
  }
}
