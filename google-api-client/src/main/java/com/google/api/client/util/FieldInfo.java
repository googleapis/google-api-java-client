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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.WeakHashMap;

/**
 * Parses field information to determine data key name/value pair associated with the field.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class FieldInfo {

  private static final ThreadLocal<WeakHashMap<Field, FieldInfo>> CACHE =
      new ThreadLocal<WeakHashMap<Field, FieldInfo>>() {
        @Override
        protected WeakHashMap<Field, FieldInfo> initialValue() {
          return new WeakHashMap<Field, FieldInfo>();
        }
      };


  /**
   * Returns the field information for the given field.
   *
   * @param field field or {@code null} for {@code null} result
   * @return field information or {@code null} if the field has no {@link Key} annotation or for
   *         {@code null} input
   */
  public static FieldInfo of(Field field) {
    if (field == null) {
      return null;
    }
    WeakHashMap<Field, FieldInfo> cache = CACHE.get();
    FieldInfo fieldInfo = cache.get(field);
    if (fieldInfo == null && !Modifier.isStatic(field.getModifiers())) {
      // ignore if field it has no @Key annotation
      Key key = field.getAnnotation(Key.class);
      if (key == null) {
        return null;
      }
      String fieldName = key.value();
      if ("##default".equals(fieldName)) {
        fieldName = field.getName();
      }
      fieldInfo = new FieldInfo(field, fieldName);
      cache.put(field, fieldInfo);
      field.setAccessible(true);
    }
    return fieldInfo;
  }

  /** Whether the field is final. */
  public final boolean isFinal;

  /**
   * Whether the field class is "primitive" as defined by {@link FieldInfo#isPrimitive(Class)}.
   */
  public final boolean isPrimitive;

  /** Field class. */
  public final Class<?> type;

  /** Field. */
  public final Field field;

  /** Data key name associated with the field. This string is interned. */
  public final String name;

  FieldInfo(Field field, String name) {
    this.field = field;
    this.name = name.intern();
    isFinal = Modifier.isFinal(field.getModifiers());
    Class<?> type = this.type = field.getType();
    isPrimitive = FieldInfo.isPrimitive(type);
  }

  /**
   * Returns the value of the field in the given object instance using reflection.
   */
  public Object getValue(Object obj) {
    return getFieldValue(field, obj);
  }

  /**
   * Sets to the given value of the field in the given object instance using reflection.
   * <p>
   * If the field is final, it checks that value being set is identical to the existing value.
   */
  public void setValue(Object obj, Object value) {
    setFieldValue(field, obj, value);
  }

  /** Returns the class information of the field's declaring class. */
  public ClassInfo getClassInfo() {
    return ClassInfo.of(field.getDeclaringClass());
  }

  /**
   * Returns whether the given field class is one of the supported primitive types like number and
   * date/time.
   */
  public static boolean isPrimitive(Class<?> fieldClass) {
    return fieldClass.isPrimitive() || fieldClass == Character.class || fieldClass == String.class
        || fieldClass == Integer.class || fieldClass == Long.class || fieldClass == Short.class
        || fieldClass == Byte.class || fieldClass == Float.class || fieldClass == Double.class
        || fieldClass == BigInteger.class || fieldClass == BigDecimal.class
        || fieldClass == DateTime.class || fieldClass == Boolean.class;
  }

  // TODO: support java.net.URI as primitive type?

  /**
   * Returns whether to given value is {@code null} or its class is primitive as defined by
   * {@link #isPrimitive(Class)}.
   */
  public static boolean isPrimitive(Object fieldValue) {
    return fieldValue == null || isPrimitive(fieldValue.getClass());
  }

  /**
   * Returns the value of the given field in the given object instance using reflection.
   */
  public static Object getFieldValue(Field field, Object obj) {
    try {
      return field.get(obj);
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Sets to the given value of the given field in the given object instance using reflection.
   * <p>
   * If the field is final, it checks that value being set is identical to the existing value.
   */
  public static void setFieldValue(Field field, Object obj, Object value) {
    if (Modifier.isFinal(field.getModifiers())) {
      Object finalValue = getFieldValue(field, obj);
      if (value == null ? finalValue != null : !value.equals(finalValue)) {
        throw new IllegalArgumentException(
            "expected final value <" + finalValue + "> but was <" + value + "> on "
                + field.getName() + " field in " + obj.getClass().getName());
      }
    } else {
      try {
        field.set(obj, value);
      } catch (SecurityException e) {
        throw new IllegalArgumentException(e);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }

  /**
   * Parses the given string value based on the given primitive class.
   * <p>
   * Types are parsed as follows:
   * <ul>
   * <li>{@code null} or {@link String}: no parsing</li>
   * <li>{@code char} or {@link Character}: {@link String#charAt(int) String.charAt}(0) (requires
   * length to be exactly 1)</li>
   * <li>{@code boolean} or {@link Boolean}: {@link Boolean#valueOf(String)}</li>
   * <li>{@code byte} or {@link Byte}: {@link Byte#valueOf(String)}</li>
   * <li>{@code short} or {@link Short}: {@link Short#valueOf(String)}</li>
   * <li>{@code int} or {@link Integer}: {@link Integer#valueOf(String)}</li>
   * <li>{@code long} or {@link Long}: {@link Long#valueOf(String)}</li>
   * <li>{@code float} or {@link Float}: {@link Float#valueOf(String)}</li>
   * <li>{@code double} or {@link Double}: {@link Double#valueOf(String)}</li>
   * <li>{@link BigInteger}: {@link BigInteger#BigInteger(String) BigInteger(String)}</li>
   * <li>{@link BigDecimal}: {@link BigDecimal#BigDecimal(String) BigDecimal(String)}</li>
   * <li>{@link DateTime}: {@link DateTime#parseRfc3339(String)}</li>
   * </ul>
   * Note that this may not be the right behavior for some use cases.
   *
   * @param primitiveClass primitive class (see {@link #isPrimitive(Class)} or {@code null} to parse
   *        as a string
   * @param stringValue string value to parse or {@code null} for {@code null} result
   * @return parsed object or {@code null} for {@code null} input
   * @throws IllegalArgumentException if the given class is not a primitive class as defined by
   *         {@link #isPrimitive(Class)}
   */
  public static Object parsePrimitiveValue(Class<?> primitiveClass, String stringValue) {
    if (stringValue == null || primitiveClass == null || primitiveClass == String.class) {
      return stringValue;
    }
    if (primitiveClass == Character.class || primitiveClass == char.class) {
      if (stringValue.length() != 1) {
        throw new IllegalArgumentException(
            "expected type Character/char but got " + primitiveClass);
      }
      return stringValue.charAt(0);
    }
    if (primitiveClass == Boolean.class || primitiveClass == boolean.class) {
      return Boolean.valueOf(stringValue);
    }
    if (primitiveClass == Byte.class || primitiveClass == byte.class) {
      return Byte.valueOf(stringValue);
    }
    if (primitiveClass == Short.class || primitiveClass == short.class) {
      return Short.valueOf(stringValue);
    }
    if (primitiveClass == Integer.class || primitiveClass == int.class) {
      return Integer.valueOf(stringValue);
    }
    if (primitiveClass == Long.class || primitiveClass == long.class) {
      return Long.valueOf(stringValue);
    }
    if (primitiveClass == Float.class || primitiveClass == float.class) {
      return Float.valueOf(stringValue);
    }
    if (primitiveClass == Double.class || primitiveClass == double.class) {
      return Double.valueOf(stringValue);
    }
    if (primitiveClass == DateTime.class) {
      return DateTime.parseRfc3339(stringValue);
    }
    if (primitiveClass == BigInteger.class) {
      return new BigInteger(stringValue);
    }
    if (primitiveClass == BigDecimal.class) {
      return new BigDecimal(stringValue);
    }
    throw new IllegalArgumentException("expected primitive class, but got: " + primitiveClass);
  }
}
