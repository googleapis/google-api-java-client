/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http;

import com.google.api.client.util.ArrayMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * HTTP transport.
 * <p>
 * Warning: this class must not be sub-classed. It is scheduled to be made final
 * in version 1.1.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class HttpTransport {

  static final Logger LOGGER = Logger.getLogger(HttpTransport.class.getName());

  /**
   * Low level HTTP transport interface to use or {@code null} to use the
   * default of {@code java.net} transport.
   */
  private static LowLevelHttpTransport lowLevelHttpTransport;

  // TODO: lowLevelHttpTransport: system property or environment variable?

  /**
   * Sets to the given low level HTTP transport.
   * <p>
   * Must be set before the first HTTP transport is constructed or else the
   * default will be used as specified in {@link #useLowLevelHttpTransport()}.
   *
   * @param lowLevelHttpTransport low level HTTP transport or {@code null} to
   *        use the default of {@code java.net} transport
   */
  public static void setLowLevelHttpTransport(
      LowLevelHttpTransport lowLevelHttpTransport) {
    HttpTransport.lowLevelHttpTransport = lowLevelHttpTransport;
  }

  /**
   * Returns the low-level HTTP transport to use. If
   * {@link #setLowLevelHttpTransport(LowLevelHttpTransport)} hasn't been
   * called, it uses the default of {@code java.net} transport.
   */
  public static LowLevelHttpTransport useLowLevelHttpTransport() {
    LowLevelHttpTransport lowLevelHttpTransportInterface =
        HttpTransport.lowLevelHttpTransport;
    if (lowLevelHttpTransportInterface == null) {
      try {
        HttpTransport.lowLevelHttpTransport =
            lowLevelHttpTransportInterface =
                (LowLevelHttpTransport) Class.forName(
                    "com.google.api.client.javanet.NetHttpTransport").getField(
                    "INSTANCE").get(null);
      } catch (Exception e) {
        throw new IllegalStateException("unable to load NetHttpTrasnport");
      }
    }
    return lowLevelHttpTransportInterface;
  }

  /**
   * Default HTTP headers. These transport default headers are put into a
   * request's headers when its build method is called.
   */
  public HttpHeaders defaultHeaders = new HttpHeaders();

  /** Map from content type to HTTP parser. */
  private final ArrayMap<String, HttpParser> contentTypeToParserMap =
      ArrayMap.create();

  /**
   * HTTP request execute intercepters. The intercepters will be invoked in the
   * order of the {@link List#iterator()}.
   */
  public List<HttpExecuteIntercepter> intercepters =
      new ArrayList<HttpExecuteIntercepter>(1);

  /**
   * Adds an HTTP response content parser.
   * <p>
   * If there is already a previous parser defined for this new parser (as
   * defined by {@link #getParser(String)} then the previous parser will be
   * removed.
   */
  public void addParser(HttpParser parser) {
    String contentType = getNormalizedContentType(parser.getContentType());
    this.contentTypeToParserMap.put(contentType, parser);
  }

  /**
   * Returns the HTTP response content parser to use for the given content type
   * or {@code null} if none is defined.
   *
   * @param contentType content type or {@code null} for {@code null} result
   */
  public HttpParser getParser(String contentType) {
    if (contentType == null) {
      return null;
    }
    contentType = getNormalizedContentType(contentType);
    return this.contentTypeToParserMap.get(contentType);
  }

  private String getNormalizedContentType(String contentType) {
    int semicolon = contentType.indexOf(';');
    return semicolon == -1 ? contentType : contentType.substring(0, semicolon);
  }

  public HttpTransport() {
    useLowLevelHttpTransport();
  }

  /** Builds a request without specifying the HTTP method. */
  public HttpRequest buildRequest() {
    return new HttpRequest(this, null);
  }

  /** Builds a {@code DELETE} request. */
  public HttpRequest buildDeleteRequest() {
    return new HttpRequest(this, "DELETE");
  }

  /** Builds a {@code GET} request. */
  public HttpRequest buildGetRequest() {
    return new HttpRequest(this, "GET");
  }

  /** Builds a {@code POST} request. */
  public HttpRequest buildPostRequest() {
    return new HttpRequest(this, "POST");
  }

  /** Builds a {@code PUT} request. */
  public HttpRequest buildPutRequest() {
    return new HttpRequest(this, "PUT");
  }

  /** Builds a {@code PATCH} request. */
  public HttpRequest buildPatchRequest() {
    return new HttpRequest(this, "PATCH");
  }

  /** Builds a {@code HEAD} request. */
  public HttpRequest buildHeadRequest() {
    return new HttpRequest(this, "HEAD");
  }

  /**
   * Removes HTTP request execute intercepters of the given class or subclasses.
   *
   * @param intercepterClass intercepter class
   */
  public void removeIntercepters(Class<?> intercepterClass) {
    Iterator<HttpExecuteIntercepter> iterable = this.intercepters.iterator();
    while (iterable.hasNext()) {
      HttpExecuteIntercepter intercepter = iterable.next();
      if (intercepterClass.isAssignableFrom(intercepter.getClass())) {
        iterable.remove();
      }
    }
  }
}
