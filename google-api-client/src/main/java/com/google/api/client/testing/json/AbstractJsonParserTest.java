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

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonString;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.api.client.util.NullValue;
import com.google.api.client.util.Value;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract test case for {@link JsonParser}.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public abstract class AbstractJsonParserTest extends TestCase {

  public AbstractJsonParserTest(String name) {
    super(name);
  }

  protected abstract JsonFactory newFactory();

  public void testParse_emptyMap() throws IOException {
    JsonParser parser = newFactory().createJsonParser("{}");
    parser.nextToken();
    @SuppressWarnings("unchecked")
    HashMap<String, Object> map = parser.parseAndClose(HashMap.class, null);
    assertTrue(map.isEmpty());
  }

  public void testParse_emptyGenericJson() throws IOException {
    JsonParser parser = newFactory().createJsonParser("{}");
    parser.nextToken();
    GenericJson json = parser.parseAndClose(GenericJson.class, null);
    assertTrue(json.isEmpty());
  }

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

  public void testSkipToKey_missingEmpty() throws IOException {
    JsonParser parser = newFactory().createJsonParser("{}");
    parser.nextToken();
    parser.skipToKey("missing");
    assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
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

  public void testToFromString() throws IOException {
    JsonFactory factory = newFactory();
    NumberTypes result = factory.fromString(NUMBER_TYPES, NumberTypes.class);
    assertEquals(NUMBER_TYPES, factory.toString(result));
  }

  public static class AnyType {
    @Key
    public Object arr;
    @Key
    public Object bool;
    @Key
    public Object num;
    @Key
    public Object obj;
    @Key
    public Object str;
    @Key
    public Object nul;
  }

  static final String ANY_TYPE = "{\"arr\":[1],\"bool\":true,\"nul\":null,\"num\":5,"
      + "\"obj\":{\"key\":\"value\"},\"str\":\"value\"}";

  public void testParser_anyType() throws IOException {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ANY_TYPE);
    parser.nextToken();
    AnyType result = parser.parse(AnyType.class, null);
    assertEquals(ANY_TYPE, factory.toString(result));
  }

  public static class ArrayType {
    @Key
    int[] arr;

    @Key
    int[][] arr2;

    @Key
    public Integer[] integerArr;
  }

  static final String ARRAY_TYPE = "{\"arr\":[4,5],\"arr2\":[[1,2],[3]],\"integerArr\":[6,7]}";

  public void testParser_arrayType() throws IOException {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ARRAY_TYPE);
    parser.nextToken();
    ArrayType result = parser.parse(ArrayType.class, null);
    assertEquals(ARRAY_TYPE, factory.toString(result));
    // check types and values
    int[] arr = result.arr;
    assertTrue(Arrays.equals(new int[] {4, 5}, arr));
    int[][] arr2 = result.arr2;
    assertEquals(2, arr2.length);
    int[] arr20 = arr2[0];
    assertEquals(2, arr20.length);
    int arr200 = arr20[0];
    assertEquals(1, arr200);
    assertEquals(2, arr20[1]);
    int[] arr21 = arr2[1];
    assertEquals(1, arr21.length);
    assertEquals(3, arr21[0]);
    Integer[] integerArr = result.integerArr;
    assertEquals(2, integerArr.length);
    assertEquals(6, integerArr[0].intValue());
  }

  public static class CollectionOfCollectionType {
    @Key
    public LinkedList<LinkedList<String>> arr;
  }

  static final String COLLECTION_TYPE = "{\"arr\":[[\"a\",\"b\"],[\"c\"]]}";

  public void testParser_collectionType() throws IOException {
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(COLLECTION_TYPE);
    parser.nextToken();
    CollectionOfCollectionType result = parser.parse(CollectionOfCollectionType.class, null);
    assertEquals(COLLECTION_TYPE, factory.toString(result));
    // check that it is actually a linked list
    LinkedList<LinkedList<String>> arr = result.arr;
    @SuppressWarnings("unused")
    LinkedList<String> linkedlist = arr.get(0);
    assertEquals("a", linkedlist.get(0));
  }

  public static class MapOfMapType {
    @Key
    public Map<String, Map<String, Integer>>[] value;
  }

  static final String MAP_TYPE =
      "{\"value\":[{\"map1\":{\"k1\":1,\"k2\":2},\"map2\":{\"kk1\":3,\"kk2\":4}}]}";

  public void testParser_mapType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(MAP_TYPE);
    parser.nextToken();
    MapOfMapType result = parser.parse(MapOfMapType.class, null);
    // serialize
    assertEquals(MAP_TYPE, factory.toString(result));
    // check parsed result
    Map<String, Map<String, Integer>>[] value = result.value;
    Map<String, Map<String, Integer>> firstMap = value[0];
    Map<String, Integer> map1 = firstMap.get("map1");
    Integer integer = map1.get("k1");
    assertEquals(1, integer.intValue());
    Map<String, Integer> map2 = firstMap.get("map2");
    assertEquals(3, map2.get("kk1").intValue());
    assertEquals(4, map2.get("kk2").intValue());
  }

  public void testParser_hashmapForMapType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(MAP_TYPE);
    parser.nextToken();
    @SuppressWarnings("unchecked")
    HashMap<String, ArrayList<ArrayMap<String, ArrayMap<String, BigDecimal>>>> result =
        parser.parse(HashMap.class, null);
    // serialize
    assertEquals(MAP_TYPE, factory.toString(result));
    // check parsed result
    ArrayList<ArrayMap<String, ArrayMap<String, BigDecimal>>> value = result.get("value");
    ArrayMap<String, ArrayMap<String, BigDecimal>> firstMap = value.get(0);
    ArrayMap<String, BigDecimal> map1 = firstMap.get("map1");
    BigDecimal integer = map1.get("k1");
    assertEquals(1, integer.intValue());
  }

  public static class WildCardTypes {
    @Key
    public Collection<? super Integer>[] lower;
    @Key
    public Map<String, ?> map;
    @Key
    public Collection<? super TreeMap<String, ? extends Integer>> mapInWild;
    @Key
    public Map<String, ? extends Integer> mapUpper;
    @Key
    public Collection<?>[] simple;
    @Key
    public Collection<? extends Integer>[] upper;
  }

  static final String WILDCARD_TYPE =
      "{\"lower\":[[1,2,3]],\"map\":{\"v\":1},\"mapInWild\":[{\"v\":1}],"
          + "\"mapUpper\":{\"v\":1},\"simple\":[[1,2,3]],\"upper\":[[1,2,3]]}";

  @SuppressWarnings("unchecked")
  public void testParser_wildCardType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(WILDCARD_TYPE);
    parser.nextToken();
    WildCardTypes result = parser.parse(WildCardTypes.class, null);
    // serialize
    assertEquals(WILDCARD_TYPE, factory.toString(result));
    // check parsed result
    Collection<BigDecimal>[] simple = (Collection<BigDecimal>[]) result.simple;
    ArrayList<BigDecimal> wildcard = (ArrayList<BigDecimal>) simple[0];
    BigDecimal wildcardFirstValue = wildcard.get(0);
    assertEquals(1, wildcardFirstValue.intValue());
    Collection<Integer>[] upper = (Collection<Integer>[]) result.upper;
    ArrayList<Integer> wildcardUpper = (ArrayList<Integer>) upper[0];
    Integer wildcardFirstValueUpper = wildcardUpper.get(0);
    assertEquals(1, wildcardFirstValueUpper.intValue());
    Collection<Integer>[] lower = (Collection<Integer>[]) result.lower;
    ArrayList<Integer> wildcardLower = (ArrayList<Integer>) lower[0];
    Integer wildcardFirstValueLower = wildcardLower.get(0);
    assertEquals(1, wildcardFirstValueLower.intValue());
    Map<String, BigDecimal> map = (Map<String, BigDecimal>) result.map;
    BigDecimal mapValue = map.get("v");
    assertEquals(1, mapValue.intValue());
    Map<String, Integer> mapUpper = (Map<String, Integer>) result.mapUpper;
    Integer mapUpperValue = mapUpper.get("v");
    assertEquals(1, mapUpperValue.intValue());
    ArrayList<TreeMap<String, ? extends Integer>> mapInWild =
        (ArrayList<TreeMap<String, ? extends Integer>>) result.mapInWild;
    TreeMap<String, Integer> mapInWildFirst = (TreeMap<String, Integer>) mapInWild.get(0);
    Integer mapInWildFirstValue = mapInWildFirst.get("v");
    assertEquals(1, mapInWildFirstValue.intValue());
  }

  public static class TypeVariableType<T> {

    @Key
    public T[][] arr;

    @Key
    public LinkedList<LinkedList<T>> list;

    @Key
    public T nullValue;

    @Key
    public T value;
  }

  public static class IntegerTypeVariableType extends TypeVariableType<Integer> {
  }

  public static class IntArrayTypeVariableType extends TypeVariableType<int[]> {
  }

  public static class DoubleListTypeVariableType extends TypeVariableType<List<Double>> {
  }

  public static class FloatMapTypeVariableType extends TypeVariableType<Map<String, Float>> {
  }

  static final String INTEGER_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,1]],\"list\":[null,[null,1]],\"nullValue\":null,\"value\":1}";

  static final String INT_ARRAY_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,[1]]],\"list\":[null,[null,[1]]],\"nullValue\":null,\"value\":[1]}";

  static final String DOUBLE_LIST_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,[1.0]]],\"list\":[null,[null,[1.0]]],"
          + "\"nullValue\":null,\"value\":[1.0]}";

  static final String FLOAT_MAP_TYPE_VARIABLE_TYPE =
      "{\"arr\":[null,[null,{\"a\":1.0}]],\"list\":[null,[null,{\"a\":1.0}]],"
          + "\"nullValue\":null,\"value\":{\"a\":1.0}}";

  public void testParser_integerTypeVariableType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INTEGER_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    IntegerTypeVariableType result = parser.parse(IntegerTypeVariableType.class, null);
    // serialize
    assertEquals(INTEGER_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    Integer[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(Integer[].class), arr[0]);
    Integer[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.NULL_INTEGER, subArr[0]);
    Integer arrValue = subArr[1];
    assertEquals(1, arrValue.intValue());
    // collection
    LinkedList<LinkedList<Integer>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<Integer> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.NULL_INTEGER, subList.get(0));
    arrValue = subList.get(1);
    assertEquals(1, arrValue.intValue());
    // null value
    Integer nullValue = result.nullValue;
    assertEquals(Data.NULL_INTEGER, nullValue);
    // value
    Integer value = result.value;
    assertEquals(1, value.intValue());
  }

  public void testParser_intArrayTypeVariableType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INT_ARRAY_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    IntArrayTypeVariableType result = parser.parse(IntArrayTypeVariableType.class, null);
    // serialize
    assertEquals(INT_ARRAY_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    int[][][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(int[][].class), arr[0]);
    int[][] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(int[].class), subArr[0]);
    int[] arrValue = subArr[1];
    assertTrue(Arrays.equals(new int[] {1}, arrValue));
    // collection
    LinkedList<LinkedList<int[]>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<int[]> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(int[].class), subList.get(0));
    arrValue = subList.get(1);
    assertTrue(Arrays.equals(new int[] {1}, arrValue));
    // null value
    int[] nullValue = result.nullValue;
    assertEquals(Data.nullOf(int[].class), nullValue);
    // value
    int[] value = result.value;
    assertTrue(Arrays.equals(new int[] {1}, value));
  }

  public void testParser_doubleListTypeVariableType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(DOUBLE_LIST_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    DoubleListTypeVariableType result = parser.parse(DoubleListTypeVariableType.class, null);
    // serialize
    assertEquals(DOUBLE_LIST_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    List<Double>[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(List[].class), arr[0]);
    List<Double>[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(ArrayList.class), subArr[0]);
    List<Double> arrValue = subArr[1];
    assertEquals(1, arrValue.size());
    Double dValue = arrValue.get(0);
    assertEquals(1.0, dValue.doubleValue());
    // collection
    LinkedList<LinkedList<List<Double>>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<List<Double>> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(ArrayList.class), subList.get(0));
    arrValue = subList.get(1);
    assertEquals(ImmutableList.of(new Double(1)), arrValue);
    // null value
    List<Double> nullValue = result.nullValue;
    assertEquals(Data.nullOf(ArrayList.class), nullValue);
    // value
    List<Double> value = result.value;
    assertEquals(ImmutableList.of(new Double(1)), value);
  }

  public void testParser_floatMapTypeVariableType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(FLOAT_MAP_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    FloatMapTypeVariableType result = parser.parse(FloatMapTypeVariableType.class, null);
    // serialize
    assertEquals(FLOAT_MAP_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    Map<String, Float>[][] arr = result.arr;
    assertEquals(2, arr.length);
    assertEquals(Data.nullOf(Map[].class), arr[0]);
    Map<String, Float>[] subArr = arr[1];
    assertEquals(2, subArr.length);
    assertEquals(Data.nullOf(HashMap.class), subArr[0]);
    Map<String, Float> arrValue = subArr[1];
    assertEquals(1, arrValue.size());
    Float fValue = arrValue.get("a");
    assertEquals(1.0f, fValue.floatValue());
    // collection
    LinkedList<LinkedList<Map<String, Float>>> list = result.list;
    assertEquals(2, list.size());
    assertEquals(Data.nullOf(LinkedList.class), list.get(0));
    LinkedList<Map<String, Float>> subList = list.get(1);
    assertEquals(2, subList.size());
    assertEquals(Data.nullOf(HashMap.class), subList.get(0));
    arrValue = subList.get(1);
    assertEquals(1, arrValue.size());
    fValue = arrValue.get("a");
    assertEquals(1.0f, fValue.floatValue());
    // null value
    Map<String, Float> nullValue = result.nullValue;
    assertEquals(Data.nullOf(HashMap.class), nullValue);
    // value
    Map<String, Float> value = result.value;
    assertEquals(1, value.size());
    fValue = value.get("a");
    assertEquals(1.0f, fValue.floatValue());
  }

  @SuppressWarnings("unchecked")
  public void testParser_treemapForTypeVariableType() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(INTEGER_TYPE_VARIABLE_TYPE);
    parser.nextToken();
    TreeMap<String, Object> result = parser.parse(TreeMap.class, null);
    // serialize
    assertEquals(INTEGER_TYPE_VARIABLE_TYPE, factory.toString(result));
    // check parsed result
    // array
    ArrayList<Object> arr = (ArrayList<Object>) result.get("arr");
    assertEquals(2, arr.size());
    assertEquals(Data.nullOf(Object.class), arr.get(0));
    ArrayList<BigDecimal> subArr = (ArrayList<BigDecimal>) arr.get(1);
    assertEquals(2, subArr.size());
    assertEquals(Data.nullOf(Object.class), subArr.get(0));
    BigDecimal arrValue = subArr.get(1);
    assertEquals(1, arrValue.intValue());
    // null value
    Object nullValue = result.get("nullValue");
    assertEquals(Data.nullOf(Object.class), nullValue);
    // value
    BigDecimal value = (BigDecimal) result.get("value");
    assertEquals(1, value.intValue());
  }

  public static class StringNullValue {
    @Key
    public String[][] arr2;
    @Key
    public String[] arr;
    @Key
    public String value;
  }

  static final String NULL_VALUE = "{\"arr\":[null],\"arr2\":[null,[null]],\"value\":null}";

  public void testParser_nullValue() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(NULL_VALUE);
    parser.nextToken();
    StringNullValue result = parser.parse(StringNullValue.class, null);
    // serialize
    assertEquals(NULL_VALUE, factory.toString(result));
    // check parsed result
    assertEquals(Data.NULL_STRING, result.value);
    String[] arr = result.arr;
    assertEquals(1, arr.length);
    assertEquals(Data.nullOf(String.class), arr[0]);
    String[][] arr2 = result.arr2;
    assertEquals(2, arr2.length);
    assertEquals(Data.nullOf(String[].class), arr2[0]);
    String[] subArr2 = arr2[1];
    assertEquals(1, subArr2.length);
    assertEquals(Data.NULL_STRING, subArr2[0]);
  }

  public enum E {

    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
    @NullValue
    NULL, IGNORED_VALUE
  }

  public static class EnumValue {
    @Key
    public E value;
    @Key
    public E otherValue;
    @Key
    public E nullValue;
  }

  static final String ENUM_VALUE =
      "{\"nullValue\":null,\"otherValue\":\"other\",\"value\":\"VALUE\"}";

  public void testParser_enumValue() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(ENUM_VALUE);
    parser.nextToken();
    EnumValue result = parser.parse(EnumValue.class, null);
    // serialize
    assertEquals(ENUM_VALUE, factory.toString(result));
    // check parsed result
    assertEquals(E.VALUE, result.value);
    assertEquals(E.OTHER_VALUE, result.otherValue);
    assertEquals(E.NULL, result.nullValue);
  }

  public static class X<XT> {
    @Key
    Y<XT> y;
  }

  public static class Y<YT> {
    @Key
    Z<YT> z;
  }

  public static class Z<ZT> {
    @Key
    ZT f;
  }

  public static class TypeVariablesPassedAround extends X<LinkedList<String>> {
  }

  static final String TYPE_VARS = "{\"y\":{\"z\":{\"f\":[\"abc\"]}}}";

  public void testParser_typeVariablesPassAround() throws IOException {
    // parse
    JsonFactory factory = newFactory();
    JsonParser parser;
    parser = factory.createJsonParser(TYPE_VARS);
    parser.nextToken();
    TypeVariablesPassedAround result = parser.parse(TypeVariablesPassedAround.class, null);
    // serialize
    assertEquals(TYPE_VARS, factory.toString(result));
    // check parsed result
    LinkedList<String> f = result.y.z.f;
    assertEquals("abc", f.get(0));
  }
}
