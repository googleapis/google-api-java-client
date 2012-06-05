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

package com.google.api.client.googleapis.auth.clientlogin;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Types;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * HTTP parser for Google response to an Authorization request.
 *
 * @since 1.10
 * @author Yaniv Inbar
 */
@SuppressWarnings("deprecation")
final class AuthKeyValueParser implements com.google.api.client.http.HttpParser, ObjectParser {

  /** Singleton instance. */
  public static final AuthKeyValueParser INSTANCE = new AuthKeyValueParser();

  public String getContentType() {
    return "text/plain";
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    response.setContentLoggingLimit(0);
    InputStream content = response.getContent();
    try {
      return parse(content, dataClass);
    } finally {
      content.close();
    }
  }

  public <T> T parse(InputStream content, Class<T> dataClass) throws IOException {
    ClassInfo classInfo = ClassInfo.of(dataClass);
    T newInstance = Types.newInstance(dataClass);
    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      int equals = line.indexOf('=');
      String key = line.substring(0, equals);
      String value = line.substring(equals + 1);
      // get the field from the type information
      Field field = classInfo.getField(key);
      if (field != null) {
        Class<?> fieldClass = field.getType();
        Object fieldValue;
        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
          fieldValue = Boolean.valueOf(value);
        } else {
          fieldValue = value;
        }
        FieldInfo.setFieldValue(field, newInstance, fieldValue);
      } else if (GenericData.class.isAssignableFrom(dataClass)) {
        GenericData data = (GenericData) newInstance;
        data.set(key, value);
      } else if (Map.class.isAssignableFrom(dataClass)) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) newInstance;
        map.put(key, value);
      }
    }

    return newInstance;
  }

  private AuthKeyValueParser() {
  }

  public <T> T parseAndClose(InputStream in, Charset charset, Class<T> dataClass)
      throws IOException {
    Reader reader = new InputStreamReader(in, charset);
    return parseAndClose(reader, dataClass);
  }

  public Object parseAndClose(InputStream in, Charset charset, Type dataType) {
    throw new UnsupportedOperationException(
        "Type-based parsing is not yet supported -- use Class<T> instead");
  }

  public <T> T parseAndClose(Reader reader, Class<T> dataClass) throws IOException {
    try {
      ClassInfo classInfo = ClassInfo.of(dataClass);
      T newInstance = Types.newInstance(dataClass);
      BufferedReader breader = new BufferedReader(reader);
      while (true) {
        String line = breader.readLine();
        if (line == null) {
          break;
        }
        int equals = line.indexOf('=');
        String key = line.substring(0, equals);
        String value = line.substring(equals + 1);
        // get the field from the type information
        Field field = classInfo.getField(key);
        if (field != null) {
          Class<?> fieldClass = field.getType();
          Object fieldValue;
          if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            fieldValue = Boolean.valueOf(value);
          } else {
            fieldValue = value;
          }
          FieldInfo.setFieldValue(field, newInstance, fieldValue);
        } else if (GenericData.class.isAssignableFrom(dataClass)) {
          GenericData data = (GenericData) newInstance;
          data.set(key, value);
        } else if (Map.class.isAssignableFrom(dataClass)) {
          @SuppressWarnings("unchecked")
          Map<Object, Object> map = (Map<Object, Object>) newInstance;
          map.put(key, value);
        }
      }

      return newInstance;
    } finally {
      reader.close();
    }
  }

  public Object parseAndClose(Reader reader, Type dataType) {
    throw new UnsupportedOperationException(
        "Type-based parsing is not yet supported -- use Class<T> instead");
  }
}
