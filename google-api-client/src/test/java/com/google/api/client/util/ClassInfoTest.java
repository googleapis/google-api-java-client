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

package com.google.api.client.util;

import junit.framework.TestCase;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

/**
 * Tests {@link ClassInfo}.
 *
 * @author Yaniv Inbar
 */
public class ClassInfoTest extends TestCase {

  public ClassInfoTest() {
  }

  public ClassInfoTest(String testName) {
    super(testName);
  }

  public void testIsAssignableToOrFrom() {
    assertTrue(ClassInfo.isAssignableToOrFrom(String.class, Object.class));
    assertTrue(ClassInfo.isAssignableToOrFrom(String.class, String.class));
    assertTrue(ClassInfo.isAssignableToOrFrom(Object.class, String.class));
    assertFalse(ClassInfo.isAssignableToOrFrom(String.class, List.class));
  }

  public void testNewCollectionInstance() {
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(null).getClass());
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(String[].class).getClass());
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(Object.class).getClass());
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(List.class).getClass());
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(AbstractList.class).getClass());
    assertEquals(ArrayList.class, ClassInfo.newCollectionInstance(ArrayList.class).getClass());
    assertEquals(LinkedList.class, ClassInfo.newCollectionInstance(LinkedList.class).getClass());
    assertEquals(HashSet.class, ClassInfo.newCollectionInstance(Set.class).getClass());
    assertEquals(HashSet.class, ClassInfo.newCollectionInstance(AbstractSet.class).getClass());
    assertEquals(HashSet.class, ClassInfo.newCollectionInstance(HashSet.class).getClass());
    assertEquals(TreeSet.class, ClassInfo.newCollectionInstance(SortedSet.class).getClass());
    assertEquals(TreeSet.class, ClassInfo.newCollectionInstance(TreeSet.class).getClass());
    try {
      ClassInfo.newMapInstance(AbstractQueue.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
    }
    try {
      ClassInfo.newMapInstance(String.class);
      fail("expected " + ClassCastException.class);
    } catch (ClassCastException e) {
      // expected
    }
  }

  public void testNewMapInstance() {
    assertEquals(ArrayMap.class, ClassInfo.newMapInstance(Object.class).getClass());
    assertEquals(ArrayMap.class, ClassInfo.newMapInstance(Map.class).getClass());
    assertEquals(ArrayMap.class, ClassInfo.newMapInstance(Cloneable.class).getClass());
    assertEquals(ArrayMap.class, ClassInfo.newMapInstance(AbstractMap.class).getClass());
    assertEquals(ArrayMap.class, ClassInfo.newMapInstance(ArrayMap.class).getClass());
    assertEquals(TreeMap.class, ClassInfo.newMapInstance(SortedMap.class).getClass());
    assertEquals(TreeMap.class, ClassInfo.newMapInstance(TreeMap.class).getClass());
    assertEquals(HashMap.class, ClassInfo.newMapInstance(HashMap.class).getClass());
    try {
      ClassInfo.newMapInstance(ConcurrentMap.class);
      fail("expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      ClassInfo.newMapInstance(String.class);
      fail("expected " + ClassCastException.class);
    } catch (ClassCastException e) {
      // expected
    }
  }

  static class CollectionParameter {
    public String s;
    public List<String> list;
    public String[] array;
  }

  public void testGetCollectionParameter() throws Exception {
    assertNull(ClassInfo.getCollectionParameter(CollectionParameter.class.getField("s")));
    assertEquals(
        String.class, ClassInfo.getCollectionParameter(CollectionParameter.class.getField("list")));
    assertEquals(String.class,
        ClassInfo.getCollectionParameter(CollectionParameter.class.getField("array")));
  }
}
