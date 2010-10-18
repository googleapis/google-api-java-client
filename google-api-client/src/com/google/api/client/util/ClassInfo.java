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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * Parses class information to determine data key name/value pairs associated with the class.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ClassInfo {
  private static final ThreadLocal<WeakHashMap<Class<?>, ClassInfo>> CACHE =
      new ThreadLocal<WeakHashMap<Class<?>, ClassInfo>>() {
        @Override
        protected WeakHashMap<Class<?>, ClassInfo> initialValue() {
          return new WeakHashMap<Class<?>, ClassInfo>();
        }
      };

  /** Class. */
  public final Class<?> clazz;

  /** Map from data key name to its field information or {@code null} for none. */
  private final IdentityHashMap<String, FieldInfo> keyNameToFieldInfoMap;

  /**
   * Returns the class information for the given class.
   *
   * @param clazz class or {@code null} for {@code null} result
   * @return class information or {@code null} for {@code null} input
   */
  public static ClassInfo of(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    WeakHashMap<Class<?>, ClassInfo> cache = CACHE.get();
    ClassInfo classInfo = cache.get(clazz);
    if (classInfo == null) {
      classInfo = new ClassInfo(clazz);
      cache.put(clazz, classInfo);
    }
    return classInfo;
  }

  /**
   * Returns the information for the given data key name.
   *
   * @param keyName data key name or {@code null} for {@code null} result
   * @return field information or {@code null} for none or for {@code null} input
   */
  public FieldInfo getFieldInfo(String keyName) {
    if (keyName == null) {
      return null;
    }
    IdentityHashMap<String, FieldInfo> keyNameToFieldInfoMap = this.keyNameToFieldInfoMap;
    if (keyNameToFieldInfoMap == null) {
      return null;
    }
    return keyNameToFieldInfoMap.get(keyName.intern());
  }

  /**
   * Returns the field for the given data key name.
   *
   * @param keyName data key name or {@code null} for {@code null} result
   * @return field or {@code null} for none or for {@code null} input
   */
  public Field getField(String keyName) {
    FieldInfo fieldInfo = getFieldInfo(keyName);
    return fieldInfo == null ? null : fieldInfo.field;
  }

  /**
   * Returns the number of data key name/value pairs associated with this data class.
   */
  public int getKeyCount() {
    IdentityHashMap<String, FieldInfo> keyNameToFieldInfoMap = this.keyNameToFieldInfoMap;
    if (keyNameToFieldInfoMap == null) {
      return 0;
    }
    return keyNameToFieldInfoMap.size();
  }

  /** Returns the data key names associated with this data class. */
  public Collection<String> getKeyNames() {
    IdentityHashMap<String, FieldInfo> keyNameToFieldInfoMap = this.keyNameToFieldInfoMap;
    if (keyNameToFieldInfoMap == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(keyNameToFieldInfoMap.keySet());
  }

  /** Creates a new instance of the given class using reflection. */
  public static <T> T newInstance(Class<T> clazz) {
    T newInstance;
    try {
      newInstance = clazz.newInstance();
    } catch (IllegalAccessException e) {
      throw handleExceptionForNewInstance(e, clazz);
    } catch (InstantiationException e) {
      throw handleExceptionForNewInstance(e, clazz);
    }
    return newInstance;
  }

  private static IllegalArgumentException handleExceptionForNewInstance(
      Exception e, Class<?> clazz) {
    StringBuilder buf =
        new StringBuilder("unable to create new instance of class ").append(clazz.getName());
    if (Modifier.isAbstract(clazz.getModifiers())) {
      buf.append(" (and) because it is abstract");
    }
    if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
      buf.append(" (and) because it is not static");
    }
    if (!Modifier.isPublic(clazz.getModifiers())) {
      buf.append(" (and) because it is not public");
    } else {
      try {
        clazz.getConstructor();
      } catch (NoSuchMethodException e1) {
        buf.append(" (and) because it has no public default constructor");
      }
    }
    throw new IllegalArgumentException(buf.toString(), e);
  }

  /**
   * Returns a new instance of the given collection class.
   * <p>
   * If a concrete collection class in the The class of the returned collection instance depends on
   * the input collection class as follows (first that matches):
   * <ul>
   * <li>{@code null} or {@link ArrayList} is an instance of the collection class: returns an
   * {@link ArrayList}</li>
   * <li>Concrete subclass of {@link Collection}: returns an instance of that collection class</li>
   * <li>{@link HashSet} is an instance of the collection class: returns a {@link HashSet}</li>
   * <li>{@link TreeSet} is an instance of the collection class: returns a {@link TreeSet}</li>
   * </ul>
   *
   * @param collectionClass collection class or {@code null} for {@link ArrayList}.
   * @return new collection instance
   */
  public static Collection<Object> newCollectionInstance(Class<?> collectionClass) {
    if (collectionClass == null || collectionClass.isAssignableFrom(ArrayList.class)) {
      return new ArrayList<Object>();
    }
    if (0 == (collectionClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE))) {
      @SuppressWarnings("unchecked")
      Collection<Object> result = (Collection<Object>) ClassInfo.newInstance(collectionClass);
      return result;
    }
    if (collectionClass.isAssignableFrom(HashSet.class)) {
      return new HashSet<Object>();
    }
    if (collectionClass.isAssignableFrom(TreeSet.class)) {
      return new TreeSet<Object>();
    }
    throw new IllegalArgumentException(
        "no default collection class defined for class: " + collectionClass.getName());
  }

  /** Returns a new instance of the given map class. */
  public static Map<String, Object> newMapInstance(Class<?> mapClass) {
    if (mapClass != null
        && 0 == (mapClass.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE))) {
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) ClassInfo.newInstance(mapClass);
      return result;
    }
    if (mapClass == null || mapClass.isAssignableFrom(ArrayMap.class)) {
      return ArrayMap.create();
    }
    if (mapClass.isAssignableFrom(TreeMap.class)) {
      return new TreeMap<String, Object>();
    }
    throw new IllegalArgumentException(
        "no default map class defined for class: " + mapClass.getName());
  }

  /**
   * Returns the type parameter for the given field assuming it is of type collection.
   */
  public static Class<?> getCollectionParameter(Field field) {
    if (field != null) {
      Type genericType = field.getGenericType();
      if (genericType instanceof ParameterizedType) {
        Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
        if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?>) {
          return (Class<?>) typeArgs[0];
        }
      }
    }
    return null;
  }

  /**
   * Returns the type parameter for the given field assuming it is of type map.
   */
  public static Class<?> getMapValueParameter(Field field) {
    if (field != null) {
      return getMapValueParameter(field.getGenericType());
    }
    return null;
  }

  /**
   * Returns the type parameter for the given genericType assuming it is of type map.
   */
  public static Class<?> getMapValueParameter(Type genericType) {
    if (genericType instanceof ParameterizedType) {
      Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
      if (typeArgs.length == 2 && typeArgs[1] instanceof Class<?>) {
        return (Class<?>) typeArgs[1];
      }
    }
    return null;
  }

  private ClassInfo(Class<?> clazz) {
    this.clazz = clazz;
    // clone map from super class
    Class<?> superClass = clazz.getSuperclass();
    IdentityHashMap<String, FieldInfo> keyNameToFieldInfoMap =
        new IdentityHashMap<String, FieldInfo>();
    if (superClass != null) {
      IdentityHashMap<String, FieldInfo> superKeyNameToFieldInfoMap =
          ClassInfo.of(superClass).keyNameToFieldInfoMap;
      if (superKeyNameToFieldInfoMap != null) {
        keyNameToFieldInfoMap.putAll(superKeyNameToFieldInfoMap);
      }
    }
    Field[] fields = clazz.getDeclaredFields();
    int fieldsSize = fields.length;
    for (int fieldsIndex = 0; fieldsIndex < fieldsSize; fieldsIndex++) {
      Field field = fields[fieldsIndex];
      FieldInfo fieldInfo = FieldInfo.of(field);
      if (fieldInfo == null) {
        continue;
      }
      String fieldName = fieldInfo.name;
      FieldInfo conflictingFieldInfo = keyNameToFieldInfoMap.get(fieldName);
      if (conflictingFieldInfo != null) {
        throw new IllegalArgumentException(
            "two fields have the same data key name: " + field + " and "
                + conflictingFieldInfo.field);
      }
      keyNameToFieldInfoMap.put(fieldName, fieldInfo);
    }
    if (keyNameToFieldInfoMap.isEmpty()) {
      this.keyNameToFieldInfoMap = null;
    } else {
      this.keyNameToFieldInfoMap = keyNameToFieldInfoMap;
    }
  }
}
