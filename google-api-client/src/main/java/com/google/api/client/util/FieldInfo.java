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

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Parses field information to determine data key name/value pair associated with the field.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class FieldInfo {

  /** Cached field information. */
  private static final Map<Field, FieldInfo> CACHE = new WeakHashMap<Field, FieldInfo>();

  /**
   * Returns the field information for the given enum value.
   *
   * @param enumValue enum value
   * @return field information
   * @throws IllegalArgumentException if the enum value has no value annotation
   * @since 1.4
   */
  public static FieldInfo of(Enum<?> enumValue) {
    try {
      FieldInfo result = FieldInfo.of(enumValue.getClass().getField(enumValue.name()));
      Preconditions.checkArgument(
          result != null, "enum constant missing @Value or @NullValue annotation: %s", enumValue);
      return result;
    } catch (NoSuchFieldException e) {
      // not possible
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the field information for the given field.
   *
   * @param field field or {@code null} for {@code null} result
   * @return field information or {@code null} if the field has no {@link #name} or for {@code null}
   *         input
   */
  public static FieldInfo of(Field field) {
    if (field == null) {
      return null;
    }
    synchronized (CACHE) {
      FieldInfo fieldInfo = CACHE.get(field);
      boolean isEnumContant = field.isEnumConstant();
      if (fieldInfo == null && (isEnumContant || !Modifier.isStatic(field.getModifiers()))) {
        String fieldName;
        if (isEnumContant) {
          // check for @Value annotation
          Value value = field.getAnnotation(Value.class);
          if (value != null) {
            fieldName = value.value();
          } else {
            // check for @NullValue annotation
            NullValue nullValue = field.getAnnotation(NullValue.class);
            if (nullValue != null) {
              fieldName = null;
            } else {
              // else ignore
              return null;
            }
          }
        } else {
          // check for @Key annotation
          Key key = field.getAnnotation(Key.class);
          if (key == null) {
            // else ignore
            return null;
          }
          fieldName = key.value();
          field.setAccessible(true);
        }
        if ("##default".equals(fieldName)) {
          fieldName = field.getName();
        }
        fieldInfo = new FieldInfo(field, fieldName);
        CACHE.put(field, fieldInfo);
      }
      return fieldInfo;
    }
  }

  /**
   * Whether the field is final.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link #isFinal()}
   */
  @Deprecated
  public final boolean isFinal;

  /**
   * Whether the field class is "primitive" as defined by {@link Data#isPrimitive(Type)}.
   *
   * @deprecated (scheduled to be made private in 1.5) Use {@link #isPrimitive()}
   */
  @Deprecated
  public final boolean isPrimitive;

  /**
   * Field class.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link #getType()}
   */
  @Deprecated
  public final Class<?> type;

  /**
   * Field.
   *
   * @deprecated (scheduled to be made private in 1.5) Use {@link #getField()}
   */
  @Deprecated
  public final Field field;

  /**
   * Data key name associated with the field for a non-enum-constant with a {@link Key} annotation,
   * or data key value associated with the enum constant with a {@link Value} annotation or {@code
   * null} for an enum constant with a {@link NullValue} annotation.
   *
   * <p>
   * This string is interned.
   * </p>
   *
   * @deprecated (scheduled to be made private in 1.5) Use {@link #getName()}
   */
  @Deprecated
  public final String name;

  FieldInfo(Field field, String name) {
    this.field = field;
    this.name = name == null ? null : name.intern();
    isFinal = Modifier.isFinal(field.getModifiers());
    type = field.getType();
    isPrimitive = Data.isPrimitive(getType());
  }

  /**
   * Returns the field.
   *
   * @since 1.4
   */
  public Field getField() {
    return field;
  }

  /**
   * Returns the data key name associated with the field for a non-enum-constant with a {@link Key}
   * annotation, or data key value associated with the enum constant with a {@link Value} annotation
   * or {@code null} for an enum constant with a {@link NullValue} annotation.
   *
   * <p>
   * This string is interned.
   * </p>
   *
   * @since 1.4
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the field's type.
   *
   * @since 1.4
   */
  public Class<?> getType() {
    return field.getType();
  }

  /**
   * Returns the field's generic type, which is a class, parameterized type, generic array type, or
   * type variable, but not a wildcard type.
   *
   * @since 1.4
   */
  public Type getGenericType() {
    return field.getGenericType();
  }

  /**
   * Returns whether the field is final.
   *
   * @since 1.4
   */
  public boolean isFinal() {
    return Modifier.isFinal(field.getModifiers());
  }

  /**
   * Returns whether the field is primitive as defined by {@link Data#isPrimitive(Type)}.
   *
   * @since 1.4
   */
  public boolean isPrimitive() {
    return isPrimitive;
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

  @SuppressWarnings("unchecked")
  public <T extends Enum<T>> T enumValue() {
    return Enum.valueOf((Class<T>) field.getDeclaringClass(), field.getName());
  }

  /**
   * Returns whether the given field class is one of the supported primitive types like number and
   * date/time.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link Data#isPrimitive(Type)}
   */
  @Deprecated
  public static boolean isPrimitive(Class<?> fieldClass) {
    return fieldClass.isPrimitive() || fieldClass == Character.class || fieldClass == String.class
        || fieldClass == Integer.class || fieldClass == Long.class || fieldClass == Short.class
        || fieldClass == Byte.class || fieldClass == Float.class || fieldClass == Double.class
        || fieldClass == BigInteger.class || fieldClass == BigDecimal.class
        || fieldClass == DateTime.class || fieldClass == Boolean.class;
  }

  /**
   * Returns whether to given value is {@code null} or its class is primitive as defined by
   * {@link #isPrimitive(Class)}.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link Data#isPrimitive(Type)} on the
   *             {@link Object#getClass()}
   */
  @Deprecated
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
   * @deprecated (scheduled to be removed in 1.5) Use {@link Data#parsePrimitiveValue(Type, String)}
   */
  @Deprecated
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
