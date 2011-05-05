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

import com.google.common.base.Preconditions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

/**
 * Collects the array values of a key/value data object, writing the fields or map values only after
 * all values have been collected.
 *
 * <p>
 * The typical application for this is when parsing JSON or XML when the value type is known to be
 * an array. It stores the values in a collection during the parsing, and only when the parsing of
 * an object is finished does it convert the collection into an array and stores it.
 * </p>
 *
 * <p>
 * Use {@link #put(String, Class, Object)} when the destination object is a map with string keys and
 * whose values accept an array of objects. Use {@link #put(Field, Class, Object)} when setting the
 * value of a field using reflection, assuming its type accepts an array of objects. One can
 * potentially use both {@code put} methods for example on an instance of {@link GenericData}.
 * </p>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public final class ArrayValueMap {

  /** Array values on a particular field. */
  static class ArrayValue {

    /** Array component type. */
    final Class<?> componentType;

    /** Values to be stored in the array. */
    final ArrayList<Object> values = new ArrayList<Object>();

    /**
     * @param componentType array component type
     */
    ArrayValue(Class<?> componentType) {
      this.componentType = componentType;
    }

    /** Creates a new array whose content matches that of the {@link #values}. */
    Object toArray() {
      return Types.toArray(values, componentType);
    }

    /**
     * Adds a given value to the array, checking the given component type matches the previously
     * stored component type.
     */
    void addValue(Class<?> componentType, Object value) {
      Preconditions.checkArgument(componentType == this.componentType);
      values.add(value);
    }
  }

  /** Map from key name to its array values. */
  private final Map<String, ArrayValueMap.ArrayValue> keyMap = ArrayMap.create();

  /** Map from field to its array values. */
  private final Map<Field, ArrayValueMap.ArrayValue> fieldMap = ArrayMap.create();

  /** Destination object whose fields must be set, or destination map whose values must be set. */
  private final Object destination;

  /**
   * @param destination destination object whose fields must be set, or destination map whose values
   *        must be set
   */
  public ArrayValueMap(Object destination) {
    this.destination = destination;
  }

  /**
   * Sets the fields of the given object using the values collected during parsing of the object's
   * fields.
   */
  public void setValues() {
    for (Map.Entry<String, ArrayValueMap.ArrayValue> entry : keyMap.entrySet()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> destinationMap = (Map<String, Object>) destination;
      destinationMap.put(entry.getKey(), entry.getValue().toArray());
    }
    for (Map.Entry<Field, ArrayValueMap.ArrayValue> entry : fieldMap.entrySet()) {
      FieldInfo.setFieldValue(entry.getKey(), destination, entry.getValue().toArray());
    }
  }

  /**
   * Puts an additional value for the given field, accumulating values on repeated calls on the same
   * field.
   *
   * @param field field
   * @param arrayComponentType array component type
   * @param value value
   */
  public void put(Field field, Class<?> arrayComponentType, Object value) {
    ArrayValueMap.ArrayValue arrayValue = fieldMap.get(field);
    if (arrayValue == null) {
      arrayValue = new ArrayValue(arrayComponentType);
      fieldMap.put(field, arrayValue);
    }
    arrayValue.addValue(arrayComponentType, value);
  }

  /**
   * Puts an additional value for the given key name, accumulating values on repeated calls on the
   * same key name.
   *
   * @param keyName key name
   * @param arrayComponentType array component type
   * @param value value
   */
  public void put(String keyName, Class<?> arrayComponentType, Object value) {
    ArrayValueMap.ArrayValue arrayValue = keyMap.get(keyName);
    if (arrayValue == null) {
      arrayValue = new ArrayValue(arrayComponentType);
      keyMap.put(keyName, arrayValue);
    }
    arrayValue.addValue(arrayComponentType, value);
  }
}
