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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Utilities for working with key/value data.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class DataUtil {

  /**
   * Returns the map to use for the given key/value data.
   *
   * @param data any key value data, represented by an object or a map, or {@code null}
   * @return if {@code data} is a map returns {@code data}; else if {@code data} is {@code null},
   *         returns an empty map; else returns {@link ReflectionMap} on the data object
   */
  public static Map<String, Object> mapOf(Object data) {
    if (data == null) {
      return Collections.emptyMap();
    }
    if (data instanceof Map<?, ?>) {
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) data;
      return result;
    }
    return new ReflectionMap(data);
  }

  /**
   * Returns a deep clone of the given key/value data, such that the result is a completely
   * independent copy.
   * <p>
   * Note that final fields cannot be changed and therefore their value won't be copied.
   *
   * @param data key/value data object or map to clone or {@code null} for a {@code null} return
   *        value
   * @return deep clone or {@code null} for {@code null} input
   */
  public static <T> T clone(T data) {
    // don't need to clone primitive
    if (FieldInfo.isPrimitive(data)) {
      return data;
    }
    T copy;
    if (data instanceof GenericData) {
      // use clone method if possible
      @SuppressWarnings("unchecked")
      T copyTmp = (T) ((GenericData) data).clone();
      copy = copyTmp;
    } else if (data instanceof ArrayMap<?, ?>) {
      // use ArrayMap's clone method if possible
      @SuppressWarnings("unchecked")
      T copyTmp = (T) ((ArrayMap) data).clone();
      copy = copyTmp;
    } else {
      // else new to use default constructor
      @SuppressWarnings("unchecked")
      T copyTmp = (T) ClassInfo.newInstance(data.getClass());
      copy = copyTmp;
    }
    cloneInternal(data, copy);
    return copy;
  }

  static void cloneInternal(Object src, Object dest) {
    // TODO: support Java arrays?
    Class<?> srcClass = src.getClass();
    if (Collection.class.isAssignableFrom(srcClass)) {
      @SuppressWarnings("unchecked")
      Collection<Object> srcCollection = (Collection<Object>) src;
      if (ArrayList.class.isAssignableFrom(srcClass)) {
        @SuppressWarnings("unchecked")
        ArrayList<Object> destArrayList = (ArrayList<Object>) dest;
        destArrayList.ensureCapacity(srcCollection.size());
      }
      @SuppressWarnings("unchecked")
      Collection<Object> destCollection = (Collection<Object>) dest;
      for (Object srcValue : srcCollection) {
        destCollection.add(clone(srcValue));
      }
    } else {
      boolean isGenericData = GenericData.class.isAssignableFrom(srcClass);
      if (isGenericData || !Map.class.isAssignableFrom(srcClass)) {
        ClassInfo classInfo = ClassInfo.of(srcClass);
        for (String fieldName : classInfo.getKeyNames()) {
          FieldInfo fieldInfo = classInfo.getFieldInfo(fieldName);
          // skip final fields
          if (!fieldInfo.isFinal) {
            // generic data already has primitive types copied by clone()
            if (!isGenericData || !fieldInfo.isPrimitive) {
              Object srcValue = fieldInfo.getValue(src);
              if (srcValue != null) {
                fieldInfo.setValue(dest, clone(srcValue));
              }
            }
          }
        }
      } else if (ArrayMap.class.isAssignableFrom(srcClass)) {
        @SuppressWarnings("unchecked")
        ArrayMap<Object, Object> destMap = (ArrayMap<Object, Object>) dest;
        @SuppressWarnings("unchecked")
        ArrayMap<Object, Object> srcMap = (ArrayMap<Object, Object>) src;
        int size = srcMap.size();
        for (int i = 0; i < size; i++) {
          Object srcValue = srcMap.getValue(i);
          if (!FieldInfo.isPrimitive(srcValue)) {
            destMap.set(i, clone(srcValue));
          }
        }
      } else {
        @SuppressWarnings("unchecked")
        Map<String, Object> destMap = (Map<String, Object>) dest;
        @SuppressWarnings("unchecked")
        Map<String, Object> srcMap = (Map<String, Object>) src;
        for (Map.Entry<String, Object> srcEntry : srcMap.entrySet()) {
          destMap.put(srcEntry.getKey(), clone(srcEntry.getValue()));

        }
      }
    }
  }

  private DataUtil() {
  }
}
