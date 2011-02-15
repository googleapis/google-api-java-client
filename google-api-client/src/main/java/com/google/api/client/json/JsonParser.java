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

package com.google.api.client.json;

import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 * Abstract low-level JSON parser.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public abstract class JsonParser {

  /** Returns the JSON factory from which this generator was created. */
  public abstract JsonFactory getFactory();

  /**
   * Closes the parser and the underlying input stream or reader, and releases any memory associated
   * with it.
   *
   * @throws IOException if failed
   */
  public abstract void close() throws IOException;

  /** Returns the next token from the stream or {@code null} to indicate end of input. */
  public abstract JsonToken nextToken() throws IOException;

  /**
   * Returns the token the parser currently points to or {@code null} for none (at start of input or
   * after end of input).
   */
  public abstract JsonToken getCurrentToken();

  /**
   * Returns the most recent field name or {@code null} for array values or for root-level values.
   */
  public abstract String getCurrentName() throws IOException;

  /**
   * Skips to the matching {@link JsonToken#END_ARRAY} if current token is
   * {@link JsonToken#START_ARRAY}, the matching {@link JsonToken#END_OBJECT} if the current token
   * is {@link JsonToken#START_OBJECT}, else does nothing.
   */
  public abstract JsonParser skipChildren() throws IOException;

  /**
   * Returns a textual representation of the current token or {@code null} if
   * {@link #getCurrentToken()} is {@code null}.
   */
  public abstract String getText() throws IOException;

  // TODO: Jackson provides getTextCharacters(), getTextLength(), and getTextOffset()

  /** Returns the byte value of the current token. */
  public abstract byte getByteValue() throws IOException;

  /** Returns the short value of the current token. */
  public abstract short getShortValue() throws IOException;

  /** Returns the int value of the current token. */
  public abstract int getIntValue() throws IOException;

  /** Returns the float value of the current token. */
  public abstract float getFloatValue() throws IOException;

  /** Returns the long value of the current token. */
  public abstract long getLongValue() throws IOException;

  /** Returns the double value of the current token. */
  public abstract double getDoubleValue() throws IOException;

  /** Returns the {@link BigInteger} value of the current token. */
  public abstract BigInteger getBigIntegerValue() throws IOException;

  /** Returns the {@link BigDecimal} value of the current token. */
  public abstract BigDecimal getDecimalValue() throws IOException;

  /**
   * Parse a JSON Object from the given JSON parser (which is closed after parsing completes) into
   * the given destination class, optionally using the given parser customizer.
   *
   * @param <T> destination class type
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   * @throws IOException I/O exception
   */
  public final <T> T parseAndClose(Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    T newInstance = ClassInfo.newInstance(destinationClass);
    parseAndClose(newInstance, customizeParser);
    return newInstance;
  }

  /**
   * Skips the values of all keys in the current object until it finds the given key.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will either be the
   * {@link JsonToken#END_OBJECT} of the current object if the key is not found, or the value of the
   * key that was found.
   * </p>
   *
   * @param keyToFind key to find
   * @throws IOException I/O exception
   */
  public final void skipToKey(String keyToFind) throws IOException {
    startParsingObject();
    do {
      String key = getText();
      nextToken();
      if (keyToFind.equals(key)) {
        break;
      }
      skipChildren();
    } while (nextToken() == JsonToken.FIELD_NAME);
  }

  /**
   * Starts parsing an object by making sure the parser points to a field name or end of object.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name.
   * </p>
   *
   * @throws IOException I/O exception
   */
  private void startParsingObject() throws IOException {
    JsonToken currentToken = getCurrentToken();
    if (currentToken == JsonToken.START_OBJECT) {
      currentToken = nextToken();
    }
    Preconditions.checkArgument(
        currentToken == JsonToken.FIELD_NAME || currentToken == JsonToken.END_OBJECT, currentToken);
  }

  /**
   * Parse a JSON Object from the given JSON parser -- which is closed after parsing completes --
   * into the given destination object, optionally using the given parser customizer.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name.
   * </p>
   *
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final void parseAndClose(Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    try {
      parse(destination, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON Object from the given JSON parser into the given destination class, optionally
   * using the given parser customizer.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will be the
   * {@link JsonToken#END_OBJECT} of the current object.
   * </p>
   *
   * @param <T> destination class type
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   * @throws IOException I/O exception
   */
  public final <T> T parse(Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    T newInstance = ClassInfo.newInstance(destinationClass);
    parse(newInstance, customizeParser);
    return newInstance;
  }

  /**
   * Parse a JSON Object from the given JSON parser into the given destination object, optionally
   * using the given parser customizer.
   * <p>
   * Before this method is called, the parser must either point to the start or end of a JSON object
   * or to a field name. After this method ends, the current token will be the
   * {@link JsonToken#END_OBJECT} of the current object.
   * </p>
   *
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final void parse(Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    if (destination instanceof GenericJson) {
      ((GenericJson) destination).jsonFactory = getFactory();
    }
    startParsingObject();
    Class<?> destinationClass = destination.getClass();
    ClassInfo classInfo = ClassInfo.of(destinationClass);
    boolean isGenericData = GenericData.class.isAssignableFrom(destinationClass);
    if (!isGenericData && Map.class.isAssignableFrom(destinationClass)) {
      @SuppressWarnings("unchecked")
      Map<String, Object> destinationMap = (Map<String, Object>) destination;
      Class<?> valueClass = ClassInfo.getMapValueParameter(destinationClass.getGenericSuperclass());
      parseMap(destinationMap, valueClass, customizeParser);
      return;
    }
    do {
      String key = getText();
      JsonToken curToken = nextToken();
      // stop at items for feeds
      if (customizeParser != null && customizeParser.stopAt(destination, key)) {
        return;
      }
      // get the field from the type information
      FieldInfo fieldInfo = classInfo.getFieldInfo(key);
      if (fieldInfo != null) {
        // skip final fields
        if (fieldInfo.isFinal && !fieldInfo.isPrimitive) {
          throw new IllegalArgumentException("final array/object fields are not supported");
        }
        Field field = fieldInfo.field;
        Object fieldValue =
            parseValue(curToken, field, fieldInfo.type, destination, customizeParser);
        FieldInfo.setFieldValue(field, destination, fieldValue);
      } else if (isGenericData) {
        // store unknown field in generic JSON
        GenericData object = (GenericData) destination;
        object.set(key, parseValue(curToken, null, null, destination, customizeParser));
      } else {
        // unrecognized field, skip value
        if (customizeParser != null) {
          customizeParser.handleUnrecognizedKey(destination, key);
        }
        skipChildren();
      }
    } while (nextToken() == JsonToken.FIELD_NAME);
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final <T> Collection<T> parseArrayAndClose(Class<?> destinationCollectionClass,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    try {
      return parseArray(destinationCollectionClass, destinationItemClass, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final <T> void parseArrayAndClose(Collection<? super T> destinationCollection,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    try {
      parseArray(destinationCollection, destinationItemClass, customizeParser);
    } finally {
      close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final <T> Collection<T> parseArray(Class<?> destinationCollectionClass,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    @SuppressWarnings("unchecked")
    Collection<T> destinationCollection =
        (Collection<T>) ClassInfo.newCollectionInstance(destinationCollectionClass);
    parseArray(destinationCollection, destinationItemClass, customizeParser);
    return destinationCollection;
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public final <T> void parseArray(Collection<? super T> destinationCollection,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    JsonToken listToken;
    while ((listToken = nextToken()) != JsonToken.END_ARRAY) {
      @SuppressWarnings("unchecked")
      T parsedValue = (T) parseValue(
          listToken, null, destinationItemClass, destinationCollection, customizeParser);
      destinationCollection.add(parsedValue);
    }
  }

  private final void parseMap(
      Map<String, Object> destinationMap, Class<?> valueClass, CustomizeJsonParser customizeParser)
      throws IOException {
    startParsingObject();
    do {
      String key = getText();
      JsonToken curToken = nextToken();
      // stop at items for feeds
      if (customizeParser != null && customizeParser.stopAt(destinationMap, key)) {
        return;
      }
      Object value = parseValue(curToken, null, valueClass, destinationMap, customizeParser);
      destinationMap.put(key, value);
    } while (nextToken() == JsonToken.FIELD_NAME);
  }

  private final Object parseValue(JsonToken token, Field field, Class<?> fieldClass,
      Object destination, CustomizeJsonParser customizeParser) throws IOException {
    switch (token) {
      case START_ARRAY:
        if (fieldClass == null || Collection.class.isAssignableFrom(fieldClass)) {
          // TODO: handle JSON array of JSON array
          Collection<Object> collectionValue = null;
          if (customizeParser != null && field != null) {
            collectionValue = customizeParser.newInstanceForArray(destination, field);
          }
          if (collectionValue == null) {
            collectionValue = ClassInfo.newCollectionInstance(fieldClass);
          }
          Class<?> subFieldClass = ClassInfo.getCollectionParameter(field);
          parseArray(collectionValue, subFieldClass, customizeParser);
          return collectionValue;
        }
        throw new IllegalArgumentException(
            "expected field type that implements Collection but got " + fieldClass + " for field "
                + field);
      case START_OBJECT:
        Object newInstance = null;
        boolean isMap = fieldClass == null || Map.class.isAssignableFrom(fieldClass);
        if (fieldClass != null && customizeParser != null) {
          newInstance = customizeParser.newInstanceForObject(destination, fieldClass);
        }
        if (newInstance == null) {
          if (isMap) {
            newInstance = ClassInfo.newMapInstance(fieldClass);
          } else {
            newInstance = ClassInfo.newInstance(fieldClass);
          }
        }
        if (isMap && fieldClass != null) {
          Class<?> valueClass;
          if (field != null) {
            valueClass = ClassInfo.getMapValueParameter(field);
          } else {
            valueClass = ClassInfo.getMapValueParameter(fieldClass.getGenericSuperclass());
          }
          if (valueClass != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> destinationMap = (Map<String, Object>) newInstance;
            parseMap(destinationMap, valueClass, customizeParser);
            return newInstance;
          }
        }
        parse(newInstance, customizeParser);
        return newInstance;
      case VALUE_TRUE:
      case VALUE_FALSE:
        if (fieldClass != null && fieldClass != Boolean.class && fieldClass != boolean.class) {
          throw new IllegalArgumentException(
              getCurrentName() + ": expected type Boolean or boolean but got " + fieldClass
                  + " for field " + field);
        }
        return token == JsonToken.VALUE_TRUE ? Boolean.TRUE : Boolean.FALSE;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        Preconditions.checkArgument(field == null || field.getAnnotation(JsonString.class) == null);
        if (fieldClass == null || fieldClass == BigDecimal.class) {
          return getDecimalValue();
        }
        if (fieldClass == BigInteger.class) {
          return getBigIntegerValue();
        }
        if (fieldClass == Double.class || fieldClass == double.class) {
          return getDoubleValue();
        }
        if (fieldClass == Long.class || fieldClass == long.class) {
          return getLongValue();
        }
        if (fieldClass == Float.class || fieldClass == float.class) {
          return getFloatValue();
        }
        if (fieldClass == Integer.class || fieldClass == int.class) {
          return getIntValue();
        }
        if (fieldClass == Short.class || fieldClass == short.class) {
          return getShortValue();
        }
        if (fieldClass == Byte.class || fieldClass == byte.class) {
          return getByteValue();
        }
        throw new IllegalArgumentException(
            getCurrentName() + ": expected numeric type but got " + fieldClass + " for field "
                + field);
      case VALUE_STRING:
        Preconditions.checkArgument(field == null || !Number.class.isAssignableFrom(fieldClass)
            || field.getAnnotation(JsonString.class) != null);
        // TODO: "special" values like Double.POSITIVE_INFINITY?
        try {
          return FieldInfo.parsePrimitiveValue(fieldClass, getText());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(getCurrentName() + " for field " + field, e);
        }
      case VALUE_NULL:
        return null;
      default:
        throw new IllegalArgumentException(
            getCurrentName() + ": unexpected JSON node type: " + token);
    }
  }
}
