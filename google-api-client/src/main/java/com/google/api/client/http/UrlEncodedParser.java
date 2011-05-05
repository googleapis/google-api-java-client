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

package com.google.api.client.http;

import com.google.api.client.util.ArrayValueMap;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Implements support for HTTP form content encoding parsing of type {@code
 * application/x-www-form-urlencoded} as specified in the <a href=
 * "http://www.w3.org/TR/1998/REC-html40-19980424/interact/forms.html#h-17.13.4.1" >HTML 4.0
 * Specification</a>.
 * <p>
 * The data is parsed using {@link #parse(String, Object)}.
 * </p>
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setParser(HttpTransport transport) {
    transport.addParser(new UrlEncodedParser());
  }
 * </code>
 * </pre>
 *
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class UrlEncodedParser implements HttpParser {

  /** {@code "application/x-www-form-urlencoded"} content type. */
  public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

  /**
   * Whether to disable response content logging (unless {@link Level#ALL} is loggable which forces
   * all logging).
   * <p>
   * Useful for example if content has sensitive data such as an authentication token. Defaults to
   * {@code false}.
   */
  public boolean disableContentLogging;

  /** Content type. Default value is {@link #CONTENT_TYPE}. */
  public String contentType = CONTENT_TYPE;

  public String getContentType() {
    return contentType;
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    if (disableContentLogging) {
      response.disableContentLogging = true;
    }
    T newInstance = Types.newInstance(dataClass);
    parse(response.parseAsString(), newInstance);
    return newInstance;
  }

  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs,
   * including support for repeating data key names.
   *
   * <p>
   * Declared fields of a "primitive" type (as defined by {@link Data#isPrimitive(Type)} are parsed
   * using {@link Data#parsePrimitiveValue(Type, String)} where the {@link Class} parameter is the
   * declared field class. Declared fields of type {@link Collection} are used to support repeating
   * data key names, so each member of the collection is an additional data key value. They are
   * parsed the same as "primitive" fields, except that the generic type parameter of the collection
   * is used as the {@link Class} parameter.
   * </p>
   *
   * <p>
   * If there is no declared field for an input parameter name, it will be ignored unless the input
   * {@code data} parameter is a {@link Map}. If it is a map, the parameter value will be stored
   * either as a string, or as a {@link ArrayList}&lt;String&gt; in the case of repeated parameters.
   * </p>
   *
   * @param content URL-encoded content or {@code null} to ignore content
   * @param data data key name/value pairs
   */
  public static void parse(String content, Object data) {
    if (content == null) {
      return;
    }
    Class<?> clazz = data.getClass();
    ClassInfo classInfo = ClassInfo.of(clazz);
    List<Type> context = Arrays.<Type>asList(clazz);
    GenericData genericData = GenericData.class.isAssignableFrom(clazz) ? (GenericData) data : null;
    @SuppressWarnings("unchecked")
    Map<Object, Object> map = Map.class.isAssignableFrom(clazz) ? (Map<Object, Object>) data : null;
    ArrayValueMap arrayValueMap = new ArrayValueMap(data);
    int cur = 0;
    int length = content.length();
    int nextEquals = content.indexOf('=');
    while (cur < length) {
      // parse next parameter
      int amp = content.indexOf('&', cur);
      if (amp == -1) {
        amp = length;
      }
      String name;
      String stringValue;
      if (nextEquals != -1 && nextEquals < amp) {
        name = content.substring(cur, nextEquals);
        stringValue = CharEscapers.decodeUri(content.substring(nextEquals + 1, amp));
        nextEquals = content.indexOf('=', amp + 1);
      } else {
        name = content.substring(cur, amp);
        stringValue = "";
      }
      name = CharEscapers.decodeUri(name);
      // get the field from the type information
      FieldInfo fieldInfo = classInfo.getFieldInfo(name);
      if (fieldInfo != null) {
        Type type = Data.resolveWildcardTypeOrTypeVariable(context, fieldInfo.getGenericType());
        // type is now class, parameterized type, or generic array type
        if (Types.isArray(type)) {
          // array that can handle repeating values
          Class<?> rawArrayComponentType =
              Types.getRawArrayComponentType(context, Types.getArrayComponentType(type));
          arrayValueMap.put(fieldInfo.getField(), rawArrayComponentType,
              parseValue(rawArrayComponentType, context, stringValue));
        } else if (Types.isAssignableToOrFrom(
            Types.getRawArrayComponentType(context, type), Iterable.class)) {
          // iterable that can handle repeating values
          @SuppressWarnings("unchecked")
          Collection<Object> collection = (Collection<Object>) fieldInfo.getValue(data);
          if (collection == null) {
            collection = Data.newCollectionInstance(type);
            fieldInfo.setValue(data, collection);
          }
          Type subFieldType = type == Object.class ? null : Types.getIterableParameter(type);
          collection.add(parseValue(subFieldType, context, stringValue));
        } else {
          // parse into a field that assumes it is a single value
          fieldInfo.setValue(data, parseValue(type, context, stringValue));
        }
      } else if (map != null) {
        // parse into a map: store as an ArrayList of values
        @SuppressWarnings("unchecked")
        ArrayList<String> listValue = (ArrayList<String>) map.get(name);
        if (listValue == null) {
          listValue = new ArrayList<String>();
          if (genericData != null) {
            genericData.set(name, listValue);
          } else {
            map.put(name, listValue);
          }
        }
        listValue.add(stringValue);
      }
      cur = amp + 1;
    }
    arrayValueMap.setValues();
  }

  private static Object parseValue(Type valueType, List<Type> context, String value) {
    Type resolved = Data.resolveWildcardTypeOrTypeVariable(context, valueType);
    return Data.parsePrimitiveValue(resolved, value);
  }
}
