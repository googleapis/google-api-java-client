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

import com.google.api.client.escape.CharEscapers;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
 * static void setParser(HttpTransport transport) {
 *   transport.addParser(new UrlEncodedParser());
 * }
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
    T newInstance = ClassInfo.newInstance(dataClass);
    parse(response.parseAsString(), newInstance);
    return newInstance;
  }

  /**
   * Parses the given URL-encoded content into the given data object of data key name/value pairs,
   * including support for repeating data key names.
   * <p>
   * Declared fields of a "primitive" type (as defined by {@link FieldInfo#isPrimitive(Class)} are
   * parsed using {@link FieldInfo#parsePrimitiveValue(Class, String)} where the {@link Class}
   * parameter is the declared field class. Declared fields of type {@link Collection} are used to
   * support repeating data key names, so each member of the collection is an additional data key
   * value. They are parsed the same as "primitive" fields, except that the generic type parameter
   * of the collection is used as the {@link Class} parameter. For keys not represented by a
   * declared field, the field type is assumed to be {@link ArrayList}&lt;String&gt;.
   *
   * @param content URL-encoded content
   * @param data data key name/value pairs
   */
  @SuppressWarnings("unchecked")
  public static void parse(String content, Object data) {
    Class<?> clazz = data.getClass();
    ClassInfo classInfo = ClassInfo.of(clazz);
    GenericData genericData = GenericData.class.isAssignableFrom(clazz) ? (GenericData) data : null;
    @SuppressWarnings("unchecked")
    Map<Object, Object> map = Map.class.isAssignableFrom(clazz) ? (Map<Object, Object>) data : null;
    int cur = 0;
    int length = content.length();
    int nextEquals = content.indexOf('=');
    while (cur < length) {
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
        Class<?> type = fieldInfo.type;
        if (Collection.class.isAssignableFrom(type)) {
          Collection<Object> collection = (Collection<Object>) fieldInfo.getValue(data);
          if (collection == null) {
            collection = ClassInfo.newCollectionInstance(type);
            fieldInfo.setValue(data, collection);
          }
          Class<?> subFieldClass = ClassInfo.getCollectionParameter(fieldInfo.field);
          collection.add(FieldInfo.parsePrimitiveValue(subFieldClass, stringValue));
        } else {
          fieldInfo.setValue(data, FieldInfo.parsePrimitiveValue(type, stringValue));
        }
      } else {
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
  }
}
