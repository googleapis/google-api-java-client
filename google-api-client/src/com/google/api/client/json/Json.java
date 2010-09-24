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
import com.google.api.client.util.DataUtil;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JSON utilities.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class Json {

  // TODO: investigate an alternative JSON parser, or slimmer Jackson?
  // or abstract out the JSON parser?

  // TODO: remove the feature to allow unquoted control chars when tab
  // escaping is fixed?

  // TODO: turn off INTERN_FIELD_NAMES???

  /** JSON factory. */
  public static final JsonFactory JSON_FACTORY =
      new JsonFactory().configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true).configure(
          JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

  /** {@code "application/json"} content type. */
  public static final String CONTENT_TYPE = "application/json";

  /**
   * Returns a debug JSON string representation for the given item intended for use in
   * {@link Object#toString()}.
   *
   * @param item data key/value pairs
   * @return debug JSON string representation
   */
  public static String toString(Object item) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try {
      JsonGenerator generator =
          Json.JSON_FACTORY.createJsonGenerator(byteStream, JsonEncoding.UTF8);
      try {
        serialize(generator, item);
      } finally {
        generator.close();
      }
    } catch (IOException e) {
      e.printStackTrace(new PrintStream(byteStream));
    }
    return byteStream.toString();
  }

  /** Serializes the given JSON value object using the given JSON generator. */
  public static void serialize(JsonGenerator generator, Object value) throws IOException {
    if (value == null) {
      generator.writeNull();
    }
    if (value instanceof String || value instanceof Long || value instanceof Double
        || value instanceof BigInteger || value instanceof BigDecimal) {
      // TODO: double: what about +- infinity?
      generator.writeString(value.toString());
    } else if (value instanceof Boolean) {
      generator.writeBoolean((Boolean) value);
    } else if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
      generator.writeNumber(((Number) value).intValue());
    } else if (value instanceof Float) {
      // TODO: what about +- infinity?
      generator.writeNumber((Float) value);
    } else if (value instanceof DateTime) {
      generator.writeString(((DateTime) value).toStringRfc3339());
    } else if (value instanceof List<?>) {
      generator.writeStartArray();
      @SuppressWarnings("unchecked")
      List<Object> listValue = (List<Object>) value;
      int size = listValue.size();
      for (int i = 0; i < size; i++) {
        serialize(generator, listValue.get(i));
      }
      generator.writeEndArray();
    } else {
      generator.writeStartObject();
      for (Map.Entry<String, Object> entry : DataUtil.mapOf(value).entrySet()) {
        Object fieldValue = entry.getValue();
        if (fieldValue != null) {
          String fieldName = entry.getKey();
          generator.writeFieldName(fieldName);
          serialize(generator, fieldValue);
        }
      }
      generator.writeEndObject();
    }
  }

  /**
   * Parse a JSON Object from the given JSON parser (which is closed after parsing completes) into
   * the given destination class, optionally using the given parser customizer.
   *
   * @param <T> destination class type
   * @param parser JSON parser
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   * @throws IOException I/O exception
   */
  public static <T> T parseAndClose(
      JsonParser parser, Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    T newInstance = ClassInfo.newInstance(destinationClass);
    parseAndClose(parser, newInstance, customizeParser);
    return newInstance;
  }

  /**
   * Skips the values of all keys in the current object until it finds the given key.
   * <p>
   * The current token will either be the {@link JsonToken#END_OBJECT} of the current object if the
   * key is not found, or the value of the key that was found.
   *
   * @param parser JSON parser
   * @param keyToFind key to find
   * @throws IOException I/O exception
   */
  public static void skipToKey(JsonParser parser, String keyToFind) throws IOException {
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String key = parser.getCurrentName();
      parser.nextToken();
      if (keyToFind.equals(key)) {
        break;
      }
      parser.skipChildren();
    }
  }

  /**
   * Parse a JSON Object from the given JSON parser (which is closed after parsing completes) into
   * the given destination object, optionally using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static void parseAndClose(
      JsonParser parser, Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    try {
      parse(parser, destination, customizeParser);
    } finally {
      parser.close();
    }
  }

  /**
   * Parse a JSON Object from the given JSON parser into the given destination class, optionally
   * using the given parser customizer.
   *
   * @param <T> destination class type
   * @param parser JSON parser
   * @param destinationClass destination class that has a public default constructor to use to
   *        create a new instance
   * @param customizeParser optional parser customizer or {@code null} for none
   * @return new instance of the parsed destination class
   * @throws IOException I/O exception
   */
  public static <T> T parse(
      JsonParser parser, Class<T> destinationClass, CustomizeJsonParser customizeParser)
      throws IOException {
    T newInstance = ClassInfo.newInstance(destinationClass);
    parse(parser, newInstance, customizeParser);
    return newInstance;
  }

  /**
   * Parse a JSON Object from the given JSON parser into the given destination object, optionally
   * using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destination destination object
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static void parse(
      JsonParser parser, Object destination, CustomizeJsonParser customizeParser)
      throws IOException {
    Class<?> destinationClass = destination.getClass();
    ClassInfo classInfo = ClassInfo.of(destinationClass);
    boolean isGenericData = GenericData.class.isAssignableFrom(destinationClass);
    if (!isGenericData && Map.class.isAssignableFrom(destinationClass)) {
      @SuppressWarnings("unchecked")
      Map<String, Object> destinationMap = (Map<String, Object>) destination;
      Class<?> valueClass = ClassInfo.getMapValueParameter(destinationClass.getGenericSuperclass());
      parseMap(parser, destinationMap, valueClass, customizeParser);
      return;
    }
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String key = parser.getCurrentName();
      JsonToken curToken = parser.nextToken();
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
            parseValue(parser, curToken, field, fieldInfo.type, destination, customizeParser);
        FieldInfo.setFieldValue(field, destination, fieldValue);
      } else if (isGenericData) {
        // store unknown field in generic JSON
        GenericData object = (GenericData) destination;
        object.set(key, parseValue(parser, curToken, null, null, destination, customizeParser));
      } else {
        // unrecognized field, skip value
        if (customizeParser != null) {
          customizeParser.handleUnrecognizedKey(destination, key);
        }
        parser.skipChildren();
      }
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static <T> Collection<T> parseArrayAndClose(JsonParser parser,
      Class<?> destinationCollectionClass, Class<T> destinationItemClass,
      CustomizeJsonParser customizeParser) throws IOException {
    try {
      return parseArray(parser, destinationCollectionClass, destinationItemClass, customizeParser);
    } finally {
      parser.close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser (which is closed after parsing completes) into
   * the given destination collection, optionally using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static <T> void parseArrayAndClose(JsonParser parser,
      Collection<? super T> destinationCollection, Class<T> destinationItemClass,
      CustomizeJsonParser customizeParser) throws IOException {
    try {
      parseArray(parser, destinationCollection, destinationItemClass, customizeParser);
    } finally {
      parser.close();
    }
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destinationCollectionClass class of destination collection (must have a public default
   *        constructor)
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static <T> Collection<T> parseArray(JsonParser parser, Class<?> destinationCollectionClass,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    @SuppressWarnings("unchecked")
    Collection<T> destinationCollection =
        (Collection<T>) ClassInfo.newCollectionInstance(destinationCollectionClass);
    parseArray(parser, destinationCollection, destinationItemClass, customizeParser);
    return destinationCollection;
  }

  /**
   * Parse a JSON Array from the given JSON parser into the given destination collection, optionally
   * using the given parser customizer.
   *
   * @param parser JSON parser
   * @param destinationCollection destination collection
   * @param destinationItemClass class of destination collection item (must have a public default
   *        constructor)
   * @param customizeParser optional parser customizer or {@code null} for none
   * @throws IOException I/O exception
   */
  public static <T> void parseArray(JsonParser parser, Collection<? super T> destinationCollection,
      Class<T> destinationItemClass, CustomizeJsonParser customizeParser) throws IOException {
    JsonToken listToken;
    while ((listToken = parser.nextToken()) != JsonToken.END_ARRAY) {
      @SuppressWarnings("unchecked")
      T parsedValue = (T) parseValue(parser,
          listToken,
          null,
          destinationItemClass,
          destinationCollection,
          customizeParser);
      destinationCollection.add(parsedValue);
    }
  }

  private static void parseMap(JsonParser parser, Map<String, Object> destinationMap,
      Class<?> valueClass, CustomizeJsonParser customizeParser) throws IOException {
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String key = parser.getCurrentName();
      JsonToken curToken = parser.nextToken();
      // stop at items for feeds
      if (customizeParser != null && customizeParser.stopAt(destinationMap, key)) {
        return;
      }
      Object value =
          parseValue(parser, curToken, null, valueClass, destinationMap, customizeParser);
      destinationMap.put(key, value);
    }
  }

  private static Object parseValue(JsonParser parser,
      JsonToken token,
      Field field,
      Class<?> fieldClass,
      Object destination,
      CustomizeJsonParser customizeParser) throws IOException {
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
          parseArray(parser, collectionValue, subFieldClass, customizeParser);
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
            parseMap(parser, destinationMap, valueClass, customizeParser);
            return newInstance;
          }
        }
        parse(parser, newInstance, customizeParser);
        return newInstance;
      case VALUE_TRUE:
      case VALUE_FALSE:
        if (fieldClass != null && fieldClass != Boolean.class && fieldClass != boolean.class) {
          throw new IllegalArgumentException(
              parser.getCurrentName() + ": expected type Boolean or boolean but got " + fieldClass
                  + " for field " + field);
        }
        return token == JsonToken.VALUE_TRUE ? Boolean.TRUE : Boolean.FALSE;
      case VALUE_NUMBER_FLOAT:
        if (fieldClass != null && fieldClass != Float.class && fieldClass != float.class) {
          throw new IllegalArgumentException(
              parser.getCurrentName() + ": expected type Float or float but got " + fieldClass
                  + " for field " + field);
        }
        return parser.getFloatValue();
      case VALUE_NUMBER_INT:
        if (fieldClass == null || fieldClass == Integer.class || fieldClass == int.class) {
          return parser.getIntValue();
        }
        if (fieldClass == Short.class || fieldClass == short.class) {
          return parser.getShortValue();
        }
        if (fieldClass == Byte.class || fieldClass == byte.class) {
          return parser.getByteValue();
        }
        throw new IllegalArgumentException(
            parser.getCurrentName() + ": expected type Integer/int/Short/short/Byte/byte but got "
                + fieldClass + " for field " + field);
      case VALUE_STRING:
        // TODO: "special" values like Double.POSITIVE_INFINITY?
        try {
          return FieldInfo.parsePrimitiveValue(fieldClass, parser.getText());
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(parser.getCurrentName() + " for field " + field, e);
        }
      case VALUE_NULL:
        return null;
      default:
        throw new IllegalArgumentException(
            parser.getCurrentName() + ": unexpected JSON node type: " + token);
    }
  }
}
