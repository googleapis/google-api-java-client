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
import com.google.api.client.json.JsonString;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

  public static class NumberTypes {
    @Key
    byte byteValue;
    @Key
    Byte byteObjValue;
    @Key
    short shortValue;
    @Key
    Short shortObjValue;
    @Key
    int intValue;
    @Key
    Integer intObjValue;
    @Key
    float floatValue;
    @Key
    Float floatObjValue;
    @Key
    long longValue;
    @Key
    Long longObjValue;
    @Key
    double doubleValue;
    @Key
    Double doubleObjValue;
    @Key
    BigInteger bigIntegerValue;
    @Key
    BigDecimal bigDecimalValue;
    @Key("yetAnotherBigDecimalValue")
    BigDecimal anotherBigDecimalValue;
  }

  public static class NumberTypesAsString {
    @Key
    @JsonString
    byte byteValue;
    @Key
    @JsonString
    Byte byteObjValue;
    @Key
    @JsonString
    short shortValue;
    @Key
    @JsonString
    Short shortObjValue;
    @Key
    @JsonString
    int intValue;
    @Key
    @JsonString
    Integer intObjValue;
    @Key
    @JsonString
    float floatValue;
    @Key
    @JsonString
    Float floatObjValue;
    @Key
    @JsonString
    long longValue;
    @Key
    @JsonString
    Long longObjValue;
    @Key
    @JsonString
    double doubleValue;
    @Key
    @JsonString
    Double doubleObjValue;
    @Key
    @JsonString
    BigInteger bigIntegerValue;
    @Key
    @JsonString
    BigDecimal bigDecimalValue;
    @Key("yetAnotherBigDecimalValue")
    @JsonString
    BigDecimal anotherBigDecimalValue;
  }

  static final String NUMBER_TYPES =
      "{\"bigDecimalValue\":1.0,\"bigIntegerValue\":1,\"byteObjValue\":1,\"byteValue\":1,"
          + "\"doubleObjValue\":1.0,\"doubleValue\":1.0,\"floatObjValue\":1.0,\"floatValue\":1.0,"
          + "\"intObjValue\":1,\"intValue\":1,\"longObjValue\":1,\"longValue\":1,"
          + "\"shortObjValue\":1,\"shortValue\":1,\"yetAnotherBigDecimalValue\":1}";

  static final String NUMBER_TYPES_AS_STRING =
      "{\"bigDecimalValue\":\"1.0\",\"bigIntegerValue\":\"1\",\"byteObjValue\":\"1\","
          + "\"byteValue\":\"1\",\"doubleObjValue\":\"1.0\",\"doubleValue\":\"1.0\","
          + "\"floatObjValue\":\"1.0\",\"floatValue\":\"1.0\",\"intObjValue\":\"1\","
          + "\"intValue\":\"1\",\"longObjValue\":\"1\",\"longValue\":\"1\",\"shortObjValue\":\"1\","
          + "\"shortValue\":\"1\",\"yetAnotherBigDecimalValue\":\"1\"}";

  public void testParser_numberTypes() throws IOException {
    JsonFactory factory = newFactory();
    JsonParser parser;
    // number types
    parser = factory.createJsonParser(NUMBER_TYPES);
    parser.nextToken();
    NumberTypes result = parser.parse(NumberTypes.class, null);
    assertEquals(NUMBER_TYPES, factory.toString(result));
    // number types as string
    parser = factory.createJsonParser(NUMBER_TYPES_AS_STRING);
    parser.nextToken();
    NumberTypesAsString resultAsString = parser.parse(NumberTypesAsString.class, null);
    assertEquals(NUMBER_TYPES_AS_STRING, factory.toString(resultAsString));
    // number types with @JsonString
    try {
      parser = factory.createJsonParser(NUMBER_TYPES_AS_STRING);
      parser.nextToken();
      parser.parse(NumberTypes.class, null);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
    // number types as string without @JsonString
    try {
      parser = factory.createJsonParser(NUMBER_TYPES);
      parser.nextToken();
      parser.parse(NumberTypesAsString.class, null);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
