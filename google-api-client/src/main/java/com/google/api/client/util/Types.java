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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utilities for working with Java types.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class Types {

  /**
   * Returns the parameterized type that is or extends the given type that matches the given super
   * class.
   *
   * <p>
   * For example, if the input type is {@code HashMap<String,Integer>} and the input super class is
   * {@code Map.class}, it will return the extended parameterized type {@link Map}, but which
   * retains the actual type information from the original {@code HashMap}.
   * </p>
   *
   * @param type class or parameterized type
   * @param superClass super class
   * @return matching parameterized type or {@code null}
   */
  public static ParameterizedType getSuperParameterizedType(Type type, Class<?> superClass) {
    if (type instanceof Class<?> || type instanceof ParameterizedType) {
      outer: while (type != null && type != Object.class) {
        Class<?> rawType;
        if (type instanceof Class<?>) {
          // type is a class
          rawType = (Class<?>) type;
        } else {
          // current is a parameterized type
          ParameterizedType parameterizedType = (ParameterizedType) type;
          rawType = getRawClass(parameterizedType);
          // check if found Collection
          if (rawType == superClass) {
            // return the actual collection parameter
            return parameterizedType;
          }
          if (superClass.isInterface()) {
            for (Type interfaceType : rawType.getGenericInterfaces()) {
              // interface type is class or parameterized type
              Class<?> interfaceClass =
                  interfaceType instanceof Class<?> ? (Class<?>) interfaceType : getRawClass(
                      (ParameterizedType) interfaceType);
              if (superClass.isAssignableFrom(interfaceClass)) {
                type = interfaceType;
                continue outer;
              }
            }
          }
        }
        // move on to the super class
        type = rawType.getGenericSuperclass();
      }
    }
    return null;
  }

  /**
   * Returns whether a class is either assignable to or from another class
   *
   * @param classToCheck class to check
   * @param anotherClass another class
   */
  public static boolean isAssignableToOrFrom(Class<?> classToCheck, Class<?> anotherClass) {
    return classToCheck.isAssignableFrom(anotherClass)
        || anotherClass.isAssignableFrom(classToCheck);
  }

  /**
   * Creates a new instance of the given class by invoking its default constructor.
   *
   * <p>
   * The given class must be public and must have a public default constructor, and must not be an
   * array or an interface or be abstract. If an enclosing class, it must be static.
   * </p>
   */
  public static <T> T newInstance(Class<T> clazz) {
    // TODO(yanivi): investigate "sneaky" options for allocating the class that GSON uses, like
    // setting the constructor to be accessible, or possibly provide a factory method of a special
    // name
    try {
      return clazz.newInstance();
    } catch (IllegalAccessException e) {
      throw handleExceptionForNewInstance(e, clazz);
    } catch (InstantiationException e) {
      throw handleExceptionForNewInstance(e, clazz);
    }
  }

  private static IllegalArgumentException handleExceptionForNewInstance(
      Exception e, Class<?> clazz) {
    StringBuilder buf =
        new StringBuilder("unable to create new instance of class ").append(clazz.getName());
    ArrayList<String> reasons = new ArrayList<String>();
    if (clazz.isArray()) {
      reasons.add("because it is an array");
    } else if (clazz.isPrimitive()) {
      reasons.add("because it is primitive");
    } else if (clazz == Void.class) {
      reasons.add("because it is void");
    } else {
      if (Modifier.isInterface(clazz.getModifiers())) {
        reasons.add("because it is an interface");
      } else if (Modifier.isAbstract(clazz.getModifiers())) {
        reasons.add("because it is abstract");
      }
      if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
        reasons.add("because it is not static");
      }
      // we don't know what visibility is necessary, but we can give a hint
      if (!Modifier.isPublic(clazz.getModifiers())) {
        reasons.add("possibly because it is not public");
      } else {
        try {
          clazz.getConstructor();
        } catch (NoSuchMethodException e1) {
          reasons.add("because it has no accessible default constructor");
        }
      }
    }
    // append reasons
    boolean and = false;
    for (String reason : reasons) {
      if (and) {
        buf.append(" and");
      } else {
        and = true;
      }
      buf.append(" ").append(reason);
    }
    return new IllegalArgumentException(buf.toString(), e);
  }

  /** Returns whether the given type is an array. */
  public static boolean isArray(Type type) {
    return type instanceof GenericArrayType || type instanceof Class<?>
        && ((Class<?>) type).isArray();
  }

  /**
   * Returns the component type of the given array type, assuming {@link #isArray(Type)}.
   *
   * <p>
   * Return type will either be class, parameterized type, generic array type, or type variable, but
   * not a wildcard type.
   * </p>
   *
   * @throws ClassCastException if {@link #isArray(Type)} is false
   */
  public static Type getArrayComponentType(Type array) {
    return array instanceof GenericArrayType ? ((GenericArrayType) array).getGenericComponentType()
        : ((Class<?>) array).getComponentType();
  }

  /**
   * Returns the raw class for the given parameter type as defined in
   * {@link ParameterizedType#getRawType()}.
   *
   * @param parameterType parameter type
   * @return raw class
   */
  public static Class<?> getRawClass(ParameterizedType parameterType) {
    return (Class<?>) parameterType.getRawType();
  }

  /**
   * Returns the only bound of the given wildcard type.
   *
   * @param wildcardType wildcard type
   * @return only bound or {@code Object.class} for none
   */
  public static Type getBound(WildcardType wildcardType) {
    Type[] lowerBounds = wildcardType.getLowerBounds();
    if (lowerBounds.length != 0) {
      return lowerBounds[0];
    }
    return wildcardType.getUpperBounds()[0];
  }

  /**
   * Resolves the actual type of the given type variable that comes from a field type based on the
   * given context list.
   * <p>
   * In case the type variable can be resolved partially, it will return the partially resolved type
   * variable.
   * </p>
   *
   * @param context context list, ordering from least specific to most specific type context, for
   *        example container class and then its field
   * @param typeVariable type variable
   * @return resolved or partially resolved actual type (type variable, class, parameterized type,
   *         or generic array type, but not wildcard type) or {@code null} if unable to resolve at
   *         all
   */
  public static Type resolveTypeVariable(List<Type> context, TypeVariable<?> typeVariable) {
    // determine where the type variable was declared
    GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
    if (genericDeclaration instanceof Class<?>) {
      Class<?> rawGenericDeclaration = (Class<?>) genericDeclaration;
      // check if the context extends that declaration
      int contextIndex = context.size();
      ParameterizedType parameterizedType = null;
      while (parameterizedType == null && --contextIndex >= 0) {
        parameterizedType =
            getSuperParameterizedType(context.get(contextIndex), rawGenericDeclaration);
      }
      if (parameterizedType != null) {
        // find the type variable's index in the declaration's type parameters
        TypeVariable<?>[] typeParameters = genericDeclaration.getTypeParameters();
        int index = 0;
        for (; index < typeParameters.length; index++) {
          TypeVariable<?> typeParameter = typeParameters[index];
          if (typeParameter.equals(typeVariable)) {
            break;
          }
        }
        // use that index to get the actual type argument
        Type result = parameterizedType.getActualTypeArguments()[index];
        if (result instanceof TypeVariable<?>) {
          // attempt to resolve type variable
          Type resolve = resolveTypeVariable(context, (TypeVariable<?>) result);
          if (resolve != null) {
            return resolve;
          }
          // partially resolved type variable is okay
        }
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the raw array component type to use -- for example for the first parameter of
   * {@link Array#newInstance(Class, int)} -- for the given component type.
   *
   * @param context context list, ordering from least specific to most specific type context, for
   *        example container class and then its field
   * @param componentType array component type or {@code null} for {@code Object.class} result
   * @return raw array component type
   */
  public static Class<?> getRawArrayComponentType(List<Type> context, Type componentType) {
    if (componentType instanceof TypeVariable<?>) {
      componentType = Types.resolveTypeVariable(context, (TypeVariable<?>) componentType);
    }
    if (componentType instanceof GenericArrayType) {
      Class<?> raw = getRawArrayComponentType(context, Types.getArrayComponentType(componentType));
      return Array.newInstance(raw, 0).getClass();
    }
    if (componentType instanceof Class<?>) {
      return (Class<?>) componentType;
    }
    if (componentType instanceof ParameterizedType) {
      return Types.getRawClass((ParameterizedType) componentType);
    }
    Preconditions.checkArgument(
        componentType == null, "wildcard type is not supported: %s", componentType);
    return Object.class;
  }

  /**
   * Returns the type parameter of {@link Iterable} that is assignable from the given iterable type.
   *
   * <p>
   * For example, for the type {@code ArrayList<Integer>} -- or for a class that extends {@code
   * ArrayList<Integer>} -- it will return {@code Integer}.
   * </p>
   *
   * @param iterableType iterable type (must extend {@link Iterable})
   * @return type parameter, which may be any type
   */
  public static Type getIterableParameter(Type iterableType) {
    return getActualParameterAtPosition(iterableType, Iterable.class, 0);
  }

  /**
   * Returns the value type parameter of {@link Map} that is assignable from the given map type.
   *
   * <p>
   * For example, for the type {@code Map<String, Integer>} -- or for a class that extends {@code
   * Map<String, Integer>} -- it will return {@code Integer}.
   * </p>
   *
   * @param mapType map type (must extend {@link Map})
   * @return type parameter, which may be any type
   */
  public static Type getMapValueParameter(Type mapType) {
    return getActualParameterAtPosition(mapType, Map.class, 1);
  }

  private static Type getActualParameterAtPosition(Type type, Class<?> superClass, int position) {
    ParameterizedType parameterizedType = Types.getSuperParameterizedType(type, superClass);
    Type valueType = parameterizedType.getActualTypeArguments()[position];
    // this is normally a type variable, except in the case where the class of iterableType is
    // superClass, e.g. Iterable<String>
    if (valueType instanceof TypeVariable<?>) {
      Type resolve = Types.resolveTypeVariable(Arrays.asList(type), (TypeVariable<?>) valueType);
      if (resolve != null) {
        return resolve;
      }
    }
    return valueType;
  }

  /**
   * Returns an iterable for an input iterable or array value.
   *
   * <p>
   * If the input value extends {@link Iterable}, it will just return the input value. Otherwise, it
   * will return an iterable that can handle arrays of primitive and non-primitive component type.
   * </p>
   *
   * @param value iterable (extends {@link Iterable}) or array value
   * @return iterable
   */
  @SuppressWarnings("unchecked")
  public static <T> Iterable<T> iterableOf(final Object value) {
    if (value instanceof Iterable<?>) {
      return (Iterable<T>) value;
    }
    Class<?> valueClass = value.getClass();
    Preconditions.checkArgument(valueClass.isArray(), "not an array or Iterable: " + valueClass);
    Class<?> subClass = valueClass.getComponentType();
    if (!subClass.isPrimitive()) {
      return Arrays.<T>asList((T[]) value);
    }
    return new Iterable<T>() {

      public Iterator<T> iterator() {
        return new Iterator<T>() {

          final int length = Array.getLength(value);
          int index = 0;

          public boolean hasNext() {
            return index < length;
          }

          public T next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            return (T) Array.get(value, index++);
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  /**
   * Returns a new array of the given component type (possibly a Java primitive) that is a copy of
   * the content of the given collection.
   *
   * @param collection collection
   * @param componentType component type (possibly a Java primitive)
   * @return new array
   */
  public static Object toArray(Collection<?> collection, Class<?> componentType) {
    if (componentType.isPrimitive()) {
      Object array = Array.newInstance(componentType, collection.size());
      int index = 0;
      for (Object value : collection) {
        Array.set(array, index++, value);
      }
      return array;
    }
    return collection.toArray((Object[]) Array.newInstance(componentType, collection.size()));
  }

  private Types() {
  }
}
