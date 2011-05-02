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

import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Strings;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Implements support for HTTP form content encoding serialization of type {@code
 * application/x-www-form-urlencoded} as specified in the <a href=
 * "http://www.w3.org/TR/REC-html40/interact/forms.html#h-17.13.4.1">HTML 4.0 Specification</a>.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, Object item) {
    UrlEncodedContent content = new UrlEncodedContent();
    content.data = item;
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class UrlEncodedContent implements HttpContent {

  /** Content type. Default value is {@link UrlEncodedParser#CONTENT_TYPE}. */
  public String contentType = UrlEncodedParser.CONTENT_TYPE;

  /** Key/value data or {@code null} for none. */
  public Object data;

  private byte[] content;

  public String getEncoding() {
    return null;
  }

  public long getLength() {
    return computeContent().length;
  }

  public String getType() {
    return contentType;
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(computeContent());
  }

  private byte[] computeContent() {
    if (content == null) {
      StringBuilder buf = new StringBuilder();
      boolean first = true;
      for (Map.Entry<String, Object> nameValueEntry : Data.mapOf(data).entrySet()) {
        Object value = nameValueEntry.getValue();
        if (value != null) {
          String name = CharEscapers.escapeUri(nameValueEntry.getKey());
          Class<? extends Object> valueClass = value.getClass();
          if (value instanceof Iterable<?> || valueClass.isArray()) {
            for (Object repeatedValue : Types.iterableOf(value)) {
              first = appendParam(first, buf, name, repeatedValue);
            }
          } else {
            first = appendParam(first, buf, name, value);
          }
        }
      }
      content = Strings.toBytesUtf8(buf.toString());
    }
    return content;
  }

  private static boolean appendParam(boolean first, StringBuilder buf, String name, Object value) {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return first;
    }
    // append value
    if (first) {
      first = false;
    } else {
      buf.append('&');
    }
    buf.append(name);
    String stringValue = CharEscapers.escapeUri(
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString());
    if (stringValue.length() != 0) {
      buf.append('=').append(stringValue);
    }
    return first;
  }

  public boolean retrySupported() {
    return true;
  }
}
