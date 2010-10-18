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

import com.google.api.client.util.Base64;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Strings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores HTTP headers used in an HTTP request or response, as defined in <a
 * href="http://tools.ietf.org/html/rfc2616#section-14">Header Field Definitions</a>.
 * <p>
 * {@code null} is not allowed as a name or value of a header. Names are case-insensitive.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpHeaders extends GenericData {

  /** {@code "Accept"} header. */
  @Key("Accept")
  public String accept;

  /** {@code "Accept-Encoding"} header. By default, this is {@code "gzip"}. */
  @Key("Accept-Encoding")
  public String acceptEncoding = "gzip";

  /** {@code "Authorization"} header. */
  @Key("Authorization")
  public String authorization;

  /** {@code "Cache-Control"} header. */
  @Key("Cache-Control")
  public String cacheControl;

  /** {@code "Content-Encoding"} header. */
  @Key("Content-Encoding")
  public String contentEncoding;

  /** {@code "Content-Length"} header. */
  @Key("Content-Length")
  public String contentLength;

  /** {@code "Content-MD5"} header. */
  @Key("Content-MD5")
  public String contentMD5;

  /** {@code "Content-Range"} header. */
  @Key("Content-Range")
  public String contentRange;

  /** {@code "Content-Type"} header. */
  @Key("Content-Type")
  public String contentType;

  /** {@code "Date"} header. */
  @Key("Date")
  public String date;

  /** {@code "ETag"} header. */
  @Key("ETag")
  public String etag;

  /** {@code "Expires"} header. */
  @Key("Expires")
  public String expires;

  /** {@code "If-Modified-Since"} header. */
  @Key("If-Modified-Since")
  public String ifModifiedSince;

  /** {@code "If-Match"} header. */
  @Key("If-Match")
  public String ifMatch;

  /** {@code "If-None-Match"} header. */
  @Key("If-None-Match")
  public String ifNoneMatch;

  /** {@code "If-Unmodified-Since"} header. */
  @Key("If-Unmodified-Since")
  public String ifUnmodifiedSince;

  /** {@code "Last-Modified"} header. */
  @Key("Last-Modified")
  public String lastModified;

  /** {@code "Location"} header. */
  @Key("Location")
  public String location;

  /** {@code "MIME-Version"} header. */
  @Key("MIME-Version")
  public String mimeVersion;

  /** {@code "Range"} header. */
  @Key("Range")
  public String range;

  /** {@code "Retry-After"} header. */
  @Key("Retry-After")
  public String retryAfter;

  /** {@code "User-Agent"} header. */
  @Key("User-Agent")
  public String userAgent;

  /** {@code "WWW-Authenticate"} header. */
  @Key("WWW-Authenticate")
  public String authenticate;

  @Override
  public HttpHeaders clone() {
    return (HttpHeaders) super.clone();
  }

  /**
   * Sets the {@link #authorization} header as specified in <a
   * href="http://tools.ietf.org/html/rfc2617#section-2">Basic Authentication Scheme</a>.
   *
   * @since 1.2
   */
  public void setBasicAuthentication(String username, String password) {
    String encoded =
        Strings.fromBytesUtf8(Base64.encode(Strings.toBytesUtf8(username + ":" + password)));
    authorization = "Basic " + encoded;
  }

  /**
   * Computes a canonical map from lower-case header name to its values.
   *
   * @return canonical map from lower-case header name to its values
   */
  public Map<String, Collection<Object>> canonicalMap() {
    Map<String, Collection<Object>> result = new HashMap<String, Collection<Object>>();
    for (Map.Entry<String, Object> entry : entrySet()) {
      String canonicalName = entry.getKey().toLowerCase();
      if (result.containsKey(canonicalName)) {
        throw new IllegalArgumentException(
            "multiple headers of the same name (headers are case insensitive): " + canonicalName);
      }
      Object value = entry.getValue();
      if (value != null) {
        if (value instanceof Collection<?>) {
          @SuppressWarnings("unchecked")
          Collection<Object> collectionValue = (Collection<Object>) value;
          result.put(canonicalName, Collections.unmodifiableCollection(collectionValue));
        } else {
          result.put(canonicalName, Collections.singleton(value));
        }
      }
    }
    return result;
  }

  /**
   * Returns the map from lower-case field name to field name used to allow for case insensitive
   * HTTP headers for the given HTTP headers class.
   */
  static HashMap<String, String> getFieldNameMap(Class<? extends HttpHeaders> headersClass) {
    HashMap<String, String> fieldNameMap = new HashMap<String, String>();
    for (String keyName : ClassInfo.of(headersClass).getKeyNames()) {
      fieldNameMap.put(keyName.toLowerCase(), keyName);
    }
    return fieldNameMap;
  }

  // TODO: override equals and hashCode
}
