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

  private final int size;
  private EntrySet entrySet;
  private final ClassInfo classInfo;
  private final Object object;

  public ReflectionMap(Object object) {
    this.object = object;
    ClassInfo classInfo = this.classInfo = ClassInfo.of(object.getClass());
    this.size = classInfo.getKeyCount();
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
      return new EntryIterator(ReflectionMap.this.classInfo,
          ReflectionMap.this.object);
    }

    @Override
    public int size() {
      return ReflectionMap.this.size;
    }
  }

  static final class EntryIterator implements
      Iterator<Map.Entry<String, Object>> {

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
      int size = this.numFields = keyNames.size();
      if (size == 0) {
        this.fieldNames = null;
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
      return this.fieldIndex < this.numFields;
    }

    public Map.Entry<String, Object> next() {
      int fieldIndex = this.fieldIndex;
      if (fieldIndex >= this.numFields) {
        throw new NoSuchElementException();
      }
      String fieldName = this.fieldNames[fieldIndex];
      this.fieldIndex++;
      return new Entry(this.object, fieldName);
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
      this.classInfo = ClassInfo.of(object.getClass());
      this.object = object;
      this.fieldName = fieldName;
    }

    public String getKey() {
      return this.fieldName;
    }

    public Object getValue() {
      if (this.isFieldValueComputed) {
        return this.fieldValue;
      }
      this.isFieldValueComputed = true;
      FieldInfo fieldInfo = this.classInfo.getFieldInfo(this.fieldName);
      return this.fieldValue = fieldInfo.getValue(this.object);
    }

    public Object setValue(Object value) {
      FieldInfo fieldInfo = this.classInfo.getFieldInfo(this.fieldName);
      Object oldValue = getValue();
      fieldInfo.setValue(this.object, value);
      this.fieldValue = value;
      return oldValue;
    }
  }
}
