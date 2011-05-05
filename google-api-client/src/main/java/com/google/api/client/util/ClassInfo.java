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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
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

  /** Class information cache. */
  private static final Map<Class<?>, ClassInfo> CACHE = new WeakHashMap<Class<?>, ClassInfo>();

  /**
   * Class.
   *
   * @deprecated (scheduled to be made private in 1.5) Use {@link #getUnderlyingClass()}
   */
  @Deprecated
  public final Class<?> clazz;

  /** Map from {@link FieldInfo#getName()} to the field information. */
  private final IdentityHashMap<String, FieldInfo> nameToFieldInfoMap =
      new IdentityHashMap<String, FieldInfo>();

  /**
   * Unmodifiable sorted (with any possible {@code null} member first) list (without duplicates) of
   * {@link FieldInfo#getName()}.
   */
  final List<String> names;

  /**
   * Returns the class information for the given underlying class.
   *
   * @param underlyingClass underlying class or {@code null} for {@code null} result
   * @return class information or {@code null} for {@code null} input
   */
  public static ClassInfo of(Class<?> underlyingClass) {
    if (underlyingClass == null) {
      return null;
    }
    synchronized (CACHE) {
      ClassInfo classInfo = CACHE.get(underlyingClass);
      if (classInfo == null) {
        classInfo = new ClassInfo(underlyingClass);
        CACHE.put(underlyingClass, classInfo);
      }
      return classInfo;
    }
  }

  /**
   * Returns the underlying class.
   *
   * @since 1.4
   */
  public Class<?> getUnderlyingClass() {
    return clazz;
  }

  /**
   * Returns the information for the given {@link FieldInfo#getName()}.
   *
   * @param name {@link FieldInfo#getName()} or {@code null}
   * @return field information or {@code null} for none
   */
  public FieldInfo getFieldInfo(String name) {
    return nameToFieldInfoMap.get(name == null ? null : name.intern());
  }

  /**
   * Returns the field for the given {@link FieldInfo#getName()}.
   *
   * @param name {@link FieldInfo#getName()} or {@code null}
   * @return field or {@code null} for none
   */
  public Field getField(String name) {
    FieldInfo fieldInfo = getFieldInfo(name);
    return fieldInfo == null ? null : fieldInfo.getField();
  }

  /**
   * Returns the underlying class is an enum.
   *
   * @since 1.4
   */
  public boolean isEnum() {
    return clazz.isEnum();
  }

  /**
   * Returns the number of fields associated with this data class.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link #getNames()}{@code .size()}
   */
  @Deprecated
  public int getKeyCount() {
    return nameToFieldInfoMap.size();
  }

  /**
   * Returns the data key names associated with this data class.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link #getNames()}
   */
  @Deprecated
  public Collection<String> getKeyNames() {
    return names;
  }

  /**
   * Returns an unmodifiable sorted set (with any possible {@code null} member first) of
   * {@link FieldInfo#getName() names}.
   */
  public Collection<String> getNames() {
    return names;
  }

  /**
   * Creates a new instance of the given class using reflection.
   *
   * @deprecated (scheduled to be removed in 1.5) use {@link Types#newInstance(Class)}
   */
  @Deprecated
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
   * @deprecated (scheduled to be removed in 1.5) use {@link Data#newCollectionInstance(Type)}
   */
  @Deprecated
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

  /**
   * Returns a new instance of the given map class.
   *
   * @deprecated (scheduled to be removed in 1.5) use {@link Data#newMapInstance(Class)}
   */
  @Deprecated
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
   *
   * @deprecated (scheduled to be removed in 1.5) use {@link Types#getIterableParameter(Type)} on
   *             the {@link Field#getGenericType()}
   */
  @Deprecated
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
   *
   * @deprecated (scheduled to be removed in 1.5) use {@link Types#getMapValueParameter(Type)} on
   *             the {@link Field#getGenericType()}
   */
  @Deprecated
  public static Class<?> getMapValueParameter(Field field) {
    if (field != null) {
      return getMapValueParameter(field.getGenericType());
    }
    return null;
  }

  /**
   * Returns the type parameter for the given genericType assuming it is of type map.
   *
   * @deprecated (scheduled to be removed in 1.5) use {@link Types#getMapValueParameter(Type)}
   */
  @Deprecated
  public static Class<?> getMapValueParameter(Type genericType) {
    if (genericType instanceof ParameterizedType) {
      Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
      if (typeArgs.length == 2 && typeArgs[1] instanceof Class<?>) {
        return (Class<?>) typeArgs[1];
      }
    }
    return null;
  }

  private ClassInfo(Class<?> srcClass) {
    clazz = srcClass;
    // name set has a special comparator to keep null first
    TreeSet<String> nameSet = new TreeSet<String>(new Comparator<String>() {
      public int compare(String s0, String s1) {
        return s0 == s1 ? 0 : s0 == null ? -1 : s1 == null ? 1 : s0.compareTo(s1);
      }
    });
    // inherit from super class
    Class<?> superClass = srcClass.getSuperclass();
    if (superClass != null) {
      ClassInfo superClassInfo = ClassInfo.of(superClass);
      nameToFieldInfoMap.putAll(superClassInfo.nameToFieldInfoMap);
      nameSet.addAll(superClassInfo.names);
    }
    // iterate over declared fields
    for (Field field : srcClass.getDeclaredFields()) {
      FieldInfo fieldInfo = FieldInfo.of(field);
      if (fieldInfo == null) {
        continue;
      }
      String fieldName = fieldInfo.getName();
      FieldInfo conflictingFieldInfo = nameToFieldInfoMap.get(fieldName);
      Preconditions.checkArgument(conflictingFieldInfo == null,
          "two fields have the same name <%s>: %s and %s", fieldName, field,
          conflictingFieldInfo == null ? null : conflictingFieldInfo.getField());
      nameToFieldInfoMap.put(fieldName, fieldInfo);
      nameSet.add(fieldName);
    }
    names = nameSet.isEmpty() ? Collections.<String>emptyList() : Collections.unmodifiableList(
        new ArrayList<String>(nameSet));
  }
}
