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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Map that uses {@link ClassInfo} to parse the key/value pairs into a map for use in
 * {@link Data#mapOf(Object)}.
 *
 * @author Yaniv Inbar
 */
final class DataMap extends AbstractMap<String, Object> {

  // TODO(yanivi): implement more methods for faster implementation!

  /** Object being reflected. */
  final Object object;

  /** Object's class info. */
  final ClassInfo classInfo;

  /**
   * @param object object being reflected
   */
  DataMap(Object object) {
    this.object = object;
    classInfo = ClassInfo.of(object.getClass());
    Preconditions.checkArgument(!classInfo.isEnum());
  }

  @Override
  public EntrySet entrySet() {
    return new EntrySet();
  }

  @Override
  public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override
  public Object get(Object key) {
    if (!(key instanceof String)) {
      return null;
    }
    FieldInfo fieldInfo = classInfo.getFieldInfo((String) key);
    if (fieldInfo == null) {
      return null;
    }
    return fieldInfo.getValue(object);
  }

  @Override
  public Object put(String key, Object value) {
    FieldInfo fieldInfo = classInfo.getFieldInfo(key);
    Preconditions.checkNotNull(fieldInfo, "no field of key " + key);
    Object oldValue = fieldInfo.getValue(object);
    fieldInfo.setValue(object, Preconditions.checkNotNull(value));
    return oldValue;
  }

  /** Set of object data key/value map entries. */
  final class EntrySet extends AbstractSet<Map.Entry<String, Object>> {

    @Override
    public EntryIterator iterator() {
      return new EntryIterator();
    }

    @Override
    public int size() {
      int result = 0;
      for (String name : classInfo.names) {
        if (classInfo.getFieldInfo(name).getValue(object) != null) {
          result++;
        }
      }
      return result;
    }

    @Override
    public void clear() {
      for (String name : classInfo.names) {
        classInfo.getFieldInfo(name).setValue(object, null);
      }
    }

    @Override
    public boolean isEmpty() {
      for (String name : classInfo.names) {
        if (classInfo.getFieldInfo(name).getValue(object) != null) {
          return false;
        }
      }
      return true;
    }
  }

  /** Iterator over the object data key/value map entries. */
  final class EntryIterator implements Iterator<Map.Entry<String, Object>> {

    /**
     * Next index into key names array computed in {@link #hasNext()} or {@code -1} before
     * {@link #hasNext()} has been called.
     */
    private int nextKeyIndex = -1;

    /**
     * Next field info computed in {@link #hasNext()} or {@code null} before {@link #hasNext()} has
     * been called since the last {@link #next()}.
     */
    private FieldInfo nextFieldInfo;

    /**
     * Next field value computed in {@link #hasNext()} or {@code null} before {@link #hasNext()} has
     * been called since the last {@link #next()}.
     */
    private Object nextFieldValue;

    /** Whether {@link #remove()} has been called since last time {@link #next()} was called. */
    private boolean isRemoved;

    /** Whether the next field has been computed. */
    private boolean isComputed;

    /**
     * Current field info found by {@link #next()} or {@code null} before {@link #next()} has been
     * called.
     */
    private FieldInfo currentFieldInfo;

    public boolean hasNext() {
      if (!isComputed) {
        isComputed = true;
        nextFieldValue = null;
        while (nextFieldValue == null && ++nextKeyIndex < classInfo.names.size()) {
          nextFieldInfo = classInfo.getFieldInfo(classInfo.names.get(nextKeyIndex));
          nextFieldValue = nextFieldInfo.getValue(object);
        }
      }
      return nextFieldValue != null;
    }

    public Map.Entry<String, Object> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      currentFieldInfo = nextFieldInfo;
      Object currentFieldValue = nextFieldValue;
      isComputed = false;
      isRemoved = false;
      nextFieldInfo = null;
      nextFieldValue = null;
      return new Entry(currentFieldInfo, currentFieldValue);
    }

    public void remove() {
      Preconditions.checkState(currentFieldInfo != null && !isRemoved);
      isRemoved = true;
      currentFieldInfo.setValue(object, null);
    }
  }

  /**
   * Entry in the reflection map.
   * <p>
   * Null key or value is not allowed.
   * </p>
   */
  final class Entry implements Map.Entry<String, Object> {

    /**
     * Current field value, possibly modified only by {@link #setValue(Object)}. As specified
     * {@link Map.Entry}, behavior is undefined if the field value is modified by other means.
     */
    private Object fieldValue;

    /** Field info. */
    private final FieldInfo fieldInfo;

    Entry(FieldInfo fieldInfo, Object fieldValue) {
      this.fieldInfo = fieldInfo;
      this.fieldValue = Preconditions.checkNotNull(fieldValue);
    }

    public String getKey() {
      return fieldInfo.getName();
    }

    public Object getValue() {
      return fieldValue;
    }

    public Object setValue(Object value) {
      Object oldValue = fieldValue;
      fieldValue = Preconditions.checkNotNull(value);
      fieldInfo.setValue(object, value);
      return oldValue;
    }

    @Override
    public int hashCode() {
      return getKey().hashCode() ^ getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof Map.Entry<?, ?>)) {
        return false;
      }
      Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
      return getKey().equals(other.getKey()) && getValue().equals(other.getValue());
    }
  }
}
