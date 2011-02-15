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

package com.google.api.client.googleapis.auth;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.GenericData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * HTTP parser for Google response to an Authorization request.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AuthKeyValueParser implements HttpParser {

  /** Singleton instance. */
  public static final AuthKeyValueParser INSTANCE = new AuthKeyValueParser();

  public String getContentType() {
    return "text/plain";
  }

  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
    T newInstance = ClassInfo.newInstance(dataClass);
    ClassInfo classInfo = ClassInfo.of(dataClass);
    response.disableContentLogging = true;
    InputStream content = response.getContent();
    try {
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
    } finally {
      content.close();
    }
    return newInstance;
  }

  private AuthKeyValueParser() {
  }
}
