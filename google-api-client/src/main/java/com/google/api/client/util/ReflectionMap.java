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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Map that uses {@link ClassInfo} to parse the key/value pairs into a map.
 * <p>
 * Iteration order of the keys is based on the sorted (ascending) key names.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ReflectionMap extends AbstractMap<String, Object> {

  final int size;
  private EntrySet entrySet;
  final ClassInfo classInfo;
  final Object object;

  public ReflectionMap(Object object) {
    this.object = object;
    ClassInfo classInfo = this.classInfo = ClassInfo.of(object.getClass());
    size = classInfo.getKeyCount();
  }

  // TODO: implement more methods for faster implementation!

  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    EntrySet entrySet = this.entrySet;
    if (entrySet == null) {
      entrySet = this.entrySet = new EntrySet();
    }
    return entrySet;
  }

  final class EntrySet extends AbstractSet<Map.Entry<String, Object>> {

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
      return new EntryIterator(classInfo, object);
    }

    @Override
    public int size() {
      return size;
    }
  }

  static final class EntryIterator implements Iterator<Map.Entry<String, Object>> {

    private final String[] fieldNames;
    private final int numFields;
    private int fieldIndex = 0;
    private final Object object;
    final ClassInfo classInfo;

    EntryIterator(ClassInfo classInfo, Object object) {
      this.classInfo = classInfo;
      this.object = object;
      // sort the keys
      Collection<String> keyNames = this.classInfo.getKeyNames();
      int size = numFields = keyNames.size();
      if (size == 0) {
        fieldNames = null;
      } else {
        String[] fieldNames = this.fieldNames = new String[size];
        int i = 0;
        for (String keyName : keyNames) {
          fieldNames[i++] = keyName;
        }
        Arrays.sort(fieldNames);
      }
    }

    public boolean hasNext() {
      return fieldIndex < numFields;
    }

    public Map.Entry<String, Object> next() {
      int fieldIndex = this.fieldIndex;
      if (fieldIndex >= numFields) {
        throw new NoSuchElementException();
      }
      String fieldName = fieldNames[fieldIndex];
      this.fieldIndex++;
      return new Entry(object, fieldName);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  static final class Entry implements Map.Entry<String, Object> {

    private boolean isFieldValueComputed;

    private final String fieldName;

    private Object fieldValue;

    private final Object object;

    private final ClassInfo classInfo;

    public Entry(Object object, String fieldName) {
      classInfo = ClassInfo.of(object.getClass());
      this.object = object;
      this.fieldName = fieldName;
    }

    public String getKey() {
      return fieldName;
    }

    public Object getValue() {
      if (isFieldValueComputed) {
        return fieldValue;
      }
      isFieldValueComputed = true;
      FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
      return fieldValue = fieldInfo.getValue(object);
    }

    public Object setValue(Object value) {
      FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
      Object oldValue = getValue();
      fieldInfo.setValue(object, value);
      fieldValue = value;
      return oldValue;
    }
  }
}
