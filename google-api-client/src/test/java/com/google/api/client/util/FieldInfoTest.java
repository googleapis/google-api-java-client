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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Tests {@link FieldInfo}.
 *
 * @author Yaniv Inbar
 */
public class FieldInfoTest extends TestCase {

  public enum E {

    @Value
    VALUE,
    @Value("other")
    OTHER_VALUE,
    @NullValue
    NULL, IGNORED_VALUE
  }

  public void testOf_enum() throws Exception {
    assertEquals(E.class.getField("VALUE"), FieldInfo.of(E.VALUE).getField());
    assertEquals(E.class.getField("OTHER_VALUE"), FieldInfo.of(E.OTHER_VALUE).getField());
    assertEquals(E.class.getField("NULL"), FieldInfo.of(E.NULL).getField());
    try {
      FieldInfo.of(E.IGNORED_VALUE);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
  }

  public void testEnumValue() {
    assertEquals(E.VALUE, FieldInfo.of(E.VALUE).<E>enumValue());
    assertEquals(E.OTHER_VALUE, FieldInfo.of(E.OTHER_VALUE).<E>enumValue());
    assertEquals(E.NULL, FieldInfo.of(E.NULL).<E>enumValue());
  }

  public void testParsePrimitiveValue() {
    assertNull(Data.parsePrimitiveValue(Boolean.class, null));
    assertEquals("abc", Data.parsePrimitiveValue(null, "abc"));
    assertEquals("abc", Data.parsePrimitiveValue(String.class, "abc"));
    assertEquals("abc", Data.parsePrimitiveValue(Object.class, "abc"));
    assertEquals('a', Data.parsePrimitiveValue(Character.class, "a"));
    assertEquals(true, Data.parsePrimitiveValue(boolean.class, "true"));
    assertEquals(true, Data.parsePrimitiveValue(Boolean.class, "true"));
    assertEquals(new Byte(Byte.MAX_VALUE),
        Data.parsePrimitiveValue(Byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(new Byte(Byte.MAX_VALUE),
        Data.parsePrimitiveValue(byte.class, String.valueOf(Byte.MAX_VALUE)));
    assertEquals(new Short(Short.MAX_VALUE),
        Data.parsePrimitiveValue(Short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(new Short(Short.MAX_VALUE),
        Data.parsePrimitiveValue(short.class, String.valueOf(Short.MAX_VALUE)));
    assertEquals(new Integer(Integer.MAX_VALUE),
        Data.parsePrimitiveValue(Integer.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(new Integer(Integer.MAX_VALUE),
        Data.parsePrimitiveValue(int.class, String.valueOf(Integer.MAX_VALUE)));
    assertEquals(new Long(Long.MAX_VALUE),
        Data.parsePrimitiveValue(Long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(new Long(Long.MAX_VALUE),
        Data.parsePrimitiveValue(long.class, String.valueOf(Long.MAX_VALUE)));
    assertEquals(new Float(Float.MAX_VALUE),
        Data.parsePrimitiveValue(Float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(new Float(Float.MAX_VALUE),
        Data.parsePrimitiveValue(float.class, String.valueOf(Float.MAX_VALUE)));
    assertEquals(new Double(Double.MAX_VALUE),
        Data.parsePrimitiveValue(Double.class, String.valueOf(Double.MAX_VALUE)));
    assertEquals(new Double(Double.MAX_VALUE),
        Data.parsePrimitiveValue(double.class, String.valueOf(Double.MAX_VALUE)));
    BigInteger bigint = BigInteger.valueOf(Long.MAX_VALUE);
    assertEquals(
        bigint, Data.parsePrimitiveValue(BigInteger.class, String.valueOf(Long.MAX_VALUE)));
    BigDecimal bigdec = BigDecimal.valueOf(Double.MAX_VALUE);
    assertEquals(
        bigdec, Data.parsePrimitiveValue(BigDecimal.class, String.valueOf(Double.MAX_VALUE)));
    DateTime now = new DateTime(new Date());
    assertEquals(now, Data.parsePrimitiveValue(DateTime.class, now.toStringRfc3339()));
    try {
      Data.parsePrimitiveValue(char.class, "abc");
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
