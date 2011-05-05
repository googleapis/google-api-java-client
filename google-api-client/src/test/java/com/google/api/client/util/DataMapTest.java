/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;

/**
 * Tests {@link DataMap}.
 *
 * @author Yaniv Inbar
 */
public class DataMapTest extends TestCase {
  static class A {
    @Key
    String r;
    @Key
    String s;
    @Key
    String t;
  }

  public void testSizeAndIsEmpty() {
    A a = new A();
    DataMap map = new DataMap(a);
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());
    a.s = "s";
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());
    a.r = "r";
    assertEquals(2, map.size());
    assertFalse(map.isEmpty());
    a.t = Data.NULL_STRING;
    assertEquals(3, map.size());
    assertFalse(map.isEmpty());
  }

  public void testIterator() {
    A a = new A();
    a.s = "value";
    DataMap map = new DataMap(a);
    Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
    assertTrue(iterator.hasNext());
    Map.Entry<String, Object> entry = iterator.next();
    assertEquals("s", entry.getKey());
    assertEquals("value", entry.getValue());
    iterator.remove();
    assertNull(a.s);
    assertFalse(iterator.hasNext());
  }

  public void testValues() {
    A a = new A();
    a.r = "r";
    a.s = "s";
    a.t = "t";
    DataMap map = new DataMap(a);
    assertEquals(ImmutableList.of("r", "s", "t"), Lists.newArrayList(map.values()));
    a.s = null;
    assertEquals(ImmutableList.of("r", "t"), Lists.newArrayList(map.values()));
    a.r = null;
    assertEquals(ImmutableList.of("t"), Lists.newArrayList(map.values()));
    a.t = null;
    assertEquals(ImmutableList.of(), Lists.newArrayList(map.values()));
  }

  public void testKeys() {
    A a = new A();
    a.r = "r";
    a.s = "s";
    a.t = "t";
    DataMap map = new DataMap(a);
    assertEquals(ImmutableList.of("r", "s", "t"), Lists.newArrayList(map.keySet()));
    a.s = null;
    assertEquals(ImmutableList.of("r", "t"), Lists.newArrayList(map.keySet()));
    a.r = null;
    assertEquals(ImmutableList.of("t"), Lists.newArrayList(map.keySet()));
    a.t = null;
    assertEquals(ImmutableList.of(), Lists.newArrayList(map.keySet()));
  }

  public void testClear() {
    A a = new A();
    a.r = "r";
    a.s = "s";
    DataMap map = new DataMap(a);
    map.clear();
    assertTrue(map.isEmpty());
    map.clear();
    assertTrue(map.isEmpty());
  }

  public void testGetKeyAndContainsKey() {
    A a = new A();
    a.r = "rv";
    DataMap map = new DataMap(a);
    assertNull(map.get("no"));
    assertFalse(map.containsKey("no"));
    assertNull(map.get("s"));
    assertFalse(map.containsKey("s"));
    assertEquals("rv", map.get("r"));
    assertTrue(map.containsKey("r"));
  }

  public void testPut() {
    A a = new A();
    a.r = "rv";
    DataMap map = new DataMap(a);
    assertNull(map.put("s", "sv"));
    assertEquals("sv", a.s);
    assertEquals("rv", map.put("r", "rv2"));
    assertEquals("rv2", a.r);
  }
}
