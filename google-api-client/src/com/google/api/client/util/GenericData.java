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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Generic data that stores all unknown data key name/value pairs.
 * <p>
 * Subclasses can declare fields for known data keys using the {@link Key} annotation. Each field
 * can be of any visibility (private, package private, protected, or public) and must not be static.
 * {@code null} unknown data key names are not allowed, but {@code null} data values are allowed.
 * <p>
 * Iteration order of the data keys is based on the sorted (ascending) key names of the declared
 * fields, followed by the iteration order of all of the unknown data key name/value pairs.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class GenericData extends AbstractMap<String, Object> implements Cloneable {

  // TODO: type parameter to specify value type?

  private EntrySet entrySet;

  /** Map of unknown fields. */
  public ArrayMap<String, Object> unknownFields = ArrayMap.create();

  // TODO: implement more methods for faster implementation

  final ClassInfo classInfo = ClassInfo.of(getClass());

  @Override
  public int size() {
    return classInfo.getKeyCount() + unknownFields.size();
  }

  @Override
  public final Object get(Object name) {
    if (!(name instanceof String)) {
      return null;
    }
    String fieldName = (String) name;
    FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
    if (fieldInfo != null) {
      return fieldInfo.getValue(this);
    }
    return unknownFields.get(fieldName);
  }

  @Override
  public final Object put(String name, Object value) {
    FieldInfo fieldInfo = classInfo.getFieldInfo(name);
    if (fieldInfo != null) {
      Object oldValue = fieldInfo.getValue(this);
      fieldInfo.setValue(this, value);
      return oldValue;
    }
    return unknownFields.put(name, value);
  }

  /**
   * Sets the given field value (may be {@code null}) for the given field name. Any existing value
   * for the field will be overwritten. It may be more slightly more efficient than
   * {@link #put(String, Object)} because it avoids accessing the field's original value.
   */
  public final void set(String name, Object value) {
    FieldInfo fieldInfo = classInfo.getFieldInfo(name);
    if (fieldInfo != null) {
      fieldInfo.setValue(this, value);
      return;
    }
    unknownFields.put(name, value);
  }

  @Override
  public final void putAll(Map<? extends String, ?> map) {
    for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
      set(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public final Object remove(Object name) {
    if (name instanceof String) {
      String fieldName = (String) name;
      FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
      if (fieldInfo != null) {
        throw new UnsupportedOperationException();
      }
      return unknownFields.remove(name);
    }
    return null;
  }

  @Override
  public Set<Map.Entry<String, Object>> entrySet() {
    EntrySet entrySet = this.entrySet;
    if (entrySet == null) {
      entrySet = this.entrySet = new EntrySet();
    }
    return entrySet;
  }

  @Override
  public GenericData clone() {
    try {
      @SuppressWarnings("unchecked")
      GenericData result = (GenericData) super.clone();
      result.entrySet = null;
      DataUtil.cloneInternal(this, result);
      result.unknownFields = DataUtil.clone(unknownFields);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException(e);
    }
  }

  final class EntrySet extends AbstractSet<Map.Entry<String, Object>> {

    @Override
    public Iterator<Map.Entry<String, Object>> iterator() {
      return new EntryIterator();
    }

    @Override
    public int size() {
      return GenericData.this.size();
    }
  }

  final class EntryIterator implements Iterator<Map.Entry<String, Object>> {

    private boolean startedUnknown;
    private final Iterator<Map.Entry<String, Object>> unknownIterator;
    private final ReflectionMap.EntryIterator fieldIterator;

    EntryIterator() {
      fieldIterator = new ReflectionMap.EntryIterator(classInfo, GenericData.this);
      unknownIterator = unknownFields.entrySet().iterator();
    }

    public boolean hasNext() {
      return !startedUnknown && fieldIterator.hasNext() || unknownIterator.hasNext();
    }

    public Map.Entry<String, Object> next() {
      if (!startedUnknown) {
        ReflectionMap.EntryIterator fieldIterator = this.fieldIterator;
        if (fieldIterator.hasNext()) {
          return fieldIterator.next();
        }
        startedUnknown = true;
      }
      return unknownIterator.next();
    }

    public void remove() {
      if (startedUnknown) {
        unknownIterator.remove();
      }
      throw new UnsupportedOperationException();
    }
  }
}
