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

package com.google.api.client.googleapis.auth.storage;

import com.google.api.client.auth.HmacSha;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Google Storage for Developers has a custom authentication method described in <a href=
 * "https://code.google.com/apis/storage/docs/developer-guide.html#authentication"
 * >Authentication</a> .
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    return transport.createRequestFactory(new GoogleStorageAuthentication(...));
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, take a look at the sample usage for
 * {@link HttpExecuteInterceptor}, which this class also implements.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.6) Use OAuth 2.0 instead
 */
@Deprecated
public final class GoogleStorageAuthentication
    implements HttpExecuteInterceptor, HttpRequestInitializer {

  static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  /** 20 character access key that identifies the client accessing the stored data. */
  private final String accessKey;

  /** Secret associated with the access key. */
  private final String secret;

  /**
   * @param accessKey 20 character access key that identifies the client accessing the stored data
   * @param secret secret associated with the access key
   * @since 1.4
   */
  public GoogleStorageAuthentication(String accessKey, String secret) {
    this.accessKey = accessKey;
    this.secret = secret;
  }

  private static final String HOST = "commondatastorage.googleapis.com";

  public void initialize(HttpRequest request) {
    request.interceptor = this;
  }

  public void intercept(HttpRequest request) throws IOException {
    HttpHeaders headers = request.headers;
    StringBuilder messageBuf = new StringBuilder();
    // canonical headers
    // HTTP method
    messageBuf.append(request.method).append('\n');
    // content MD5
    String contentMD5 = headers.contentMD5;
    if (contentMD5 != null) {
      messageBuf.append(contentMD5);
    }
    messageBuf.append('\n');
    // content type
    HttpContent content = request.content;
    if (content != null) {
      String contentType = content.getType();
      if (contentType != null) {
        messageBuf.append(contentType);
      }
    }
    messageBuf.append('\n');
    // date
    Object date = headers.date;
    if (date != null) {
      messageBuf.append(headers.date);
    }
    messageBuf.append('\n');
    // canonical extension headers
    TreeMap<String, String> extensions = new TreeMap<String, String>();
    for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
      String name = headerEntry.getKey().toLowerCase();
      Object value = headerEntry.getValue();
      if (value != null && name.startsWith("x-goog-")) {
        if (value instanceof Collection<?>) {
          @SuppressWarnings("unchecked")
          Collection<Object> collectionValue = (Collection<Object>) value;
          StringBuilder buf = new StringBuilder();
          boolean first = true;
          for (Object repeatedValue : collectionValue) {
            if (first) {
              first = false;
            } else {
              buf.append(',');
            }
            buf.append(WHITESPACE_PATTERN.matcher(repeatedValue.toString()).replaceAll(" "));
          }
          extensions.put(name, buf.toString());
        } else {
          extensions.put(name, WHITESPACE_PATTERN.matcher(value.toString()).replaceAll(" "));
        }
      }
    }
    for (Map.Entry<String, String> extensionEntry : extensions.entrySet()) {
      messageBuf
          .append(extensionEntry.getKey())
          .append(':')
          .append(extensionEntry.getValue())
          .append('\n');
    }
    // canonical resource
    GenericUrl url = request.url;
    String host = url.host;
    if (!host.endsWith(HOST)) {
      throw new IllegalArgumentException("missing host " + HOST);
    }
    int bucketNameLength = host.length() - HOST.length() - 1;
    if (bucketNameLength > 0) {
      String bucket = host.substring(0, bucketNameLength);
      if (!bucket.equals("c")) {
        messageBuf.append('/').append(bucket);
      }
    }
    if (url.pathParts != null) {
      messageBuf.append(url.getRawPath());
    }
    if (url.get("acl") != null) {
      messageBuf.append("?acl");
    } else if (url.get("location") != null) {
      messageBuf.append("?location");
    } else if (url.get("logging") != null) {
      messageBuf.append("?logging");
    } else if (url.get("requestPayment") != null) {
      messageBuf.append("?requestPayment");
    } else if (url.get("torrent") != null) {
      messageBuf.append("?torrent");
    }
    try {
      request.headers.authorization =
          "GOOG1 " + accessKey + ":" + HmacSha.sign(secret, messageBuf.toString());
    } catch (GeneralSecurityException e) {
      IOException io = new IOException();
      io.initCause(e);
      throw io;
    }
  }

}
