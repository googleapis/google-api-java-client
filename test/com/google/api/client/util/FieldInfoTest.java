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

package com.google.api.client.util;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Tests {@link FieldInfo}.
 * 
 * @author Yaniv Inbar
 */
public class FieldInfoTest extends TestCase {

  public FieldInfoTest() {
  }

  public FieldInfoTest(String testName) {
    super(testName);
  }

  public void testParsePrimitiveValue() {
    assertNull(FieldInfo.parsePrimitiveValue(Boolean.class, null));
    assertEquals("abc", FieldInfo.parsePrimitiveValue(null, "abc"));
    assertEquals("abc", FieldInfo.parsePrimitiveValue(String.class, "abc"));
    assertEquals('a', FieldInfo.parsePrimitiveValue(Character.class, "a"));
    assertEquals(true, FieldInfo.parsePrimitiveValue(boolean.class, "true"));
    assertEquals(true, FieldInfo.parsePrimitiveValue(Boolean.class, "true"));
    assertEquals(new Byte(Byte.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(new Byte(Byte.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(new Short(Short.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(new Short(Short.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(new Integer(Integer.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Integer.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(new Integer(Integer.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        int.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(new Long(Long.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(new Long(Long.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(new Float(Float.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(new Float(Float.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(new Double(Double.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        Double.class, String.valueOf(Double.MAX_VALUE)));
    assertEquals(new Double(Double.MAX_VALUE), FieldInfo.parsePrimitiveValue(
        double.class, String.valueOf(Double.MAX_VALUE)));
    BigInteger bigint = BigInteger.valueOf(Long.MAX_VALUE);
    assertEquals(bigint, FieldInfo.parsePrimitiveValue(BigInteger.class, String
        .valueOf(Long.MAX_VALUE)));
    BigDecimal bigdec = BigDecimal.valueOf(Double.MAX_VALUE);
    assertEquals(bigdec, FieldInfo.parsePrimitiveValue(BigDecimal.class, String
        .valueOf(Double.MAX_VALUE)));
    DateTime now = new DateTime(new Date());
    assertEquals(now, FieldInfo.parsePrimitiveValue(DateTime.class, now
        .toStringRfc3339()));
    try {
      FieldInfo.parsePrimitiveValue(char.class, "abc");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
    try {
      FieldInfo.parsePrimitiveValue(Object.class, "a");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
