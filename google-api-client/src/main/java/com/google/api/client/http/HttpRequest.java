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

import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP request.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpRequest {

  /** User agent suffix for all requests. */
  private static final String USER_AGENT_SUFFIX = "Google-API-Java-Client/" + Strings.VERSION;

  /**
   * HTTP request headers.
   * <p>
   * Its value is initialized by calling {@code clone()} on the
   * {@link HttpTransport#defaultHeaders}. Therefore, it is initialized to be of the same Java
   * class, i.e. of the same {@link Object#getClass()}.
   */
  public HttpHeaders headers;

  /**
   * Whether to disable request content logging during {@link #execute()} (unless {@link Level#ALL}
   * is loggable which forces all logging).
   * <p>
   * Useful for example if content has sensitive data such as an authentication information.
   * Defaults to {@code false}.
   */
  public boolean disableContentLogging;

  /** HTTP request content or {@code null} for none. */
  public HttpContent content;

  /** HTTP transport. */
  public final HttpTransport transport;

  /**
   * HTTP request method.
   * <p>
   * <b>Upgrade warning:</b> prior to version 1.3 of the library, this was a string.
   * </p>
   *
   * @since 1.3
   */
  public HttpMethod method;

  // TODO: support more HTTP methods?

  /** HTTP request URL. */
  public GenericUrl url;

  /**
   * @param transport HTTP transport
   * @param method HTTP request method (may be {@code null}
   */
  HttpRequest(HttpTransport transport, HttpMethod method) {
    this.transport = transport;
    headers = transport.defaultHeaders.clone();
    this.method = method;
  }

  /** Sets the {@link #url} based on the given encoded URL string. */
  public void setUrl(String encodedUrl) {
    url = new GenericUrl(encodedUrl);
  }

  /**
   * Execute the HTTP request and returns the HTTP response.
   * <p>
   * Note that regardless of the returned status code, the HTTP response content has not been parsed
   * yet, and must be parsed by the calling code.
   * <p>
   * Almost all details of the request and response are logged if {@link Level#CONFIG} is loggable.
   * The only exception is the value of the {@code Authorization} header which is only logged if
   * {@link Level#ALL} is loggable.
   *
   * @return HTTP response for an HTTP success code
   * @throws HttpResponseException for an HTTP error code
   * @see HttpResponse#isSuccessStatusCode
   */
  public HttpResponse execute() throws IOException {
    Preconditions.checkNotNull(method);
    Preconditions.checkNotNull(url);
    // first run the execute intercepters
    for (HttpExecuteIntercepter intercepter : transport.intercepters) {
      intercepter.intercept(this);
    }
    // build low-level HTTP request
    HttpMethod method = this.method;
    GenericUrl url = this.url;
    String urlString = url.build();
    LowLevelHttpRequest lowLevelHttpRequest;
    switch (method) {
      case DELETE:
        lowLevelHttpRequest = transport.buildDeleteRequest(urlString);
        break;
      default:
        lowLevelHttpRequest = transport.buildGetRequest(urlString);
        break;
      case HEAD:
        if (!transport.supportsHead()) {
          throw new IllegalArgumentException("HTTP transport doesn't support HEAD");
        }
        lowLevelHttpRequest = transport.buildHeadRequest(urlString);
        break;
      case PATCH:
        if (!transport.supportsPatch()) {
          throw new IllegalArgumentException("HTTP transport doesn't support PATCH");
        }
        lowLevelHttpRequest = transport.buildPatchRequest(urlString);
        break;
      case POST:
        lowLevelHttpRequest = transport.buildPostRequest(urlString);
        break;
      case PUT:
        lowLevelHttpRequest = transport.buildPutRequest(urlString);
        break;
    }
    Logger logger = HttpTransport.LOGGER;
    boolean loggable = logger.isLoggable(Level.CONFIG);
    StringBuilder logbuf = null;
    // log method and URL
    if (loggable) {
      logbuf = new StringBuilder();
      logbuf.append("-------------- REQUEST  --------------").append(Strings.LINE_SEPARATOR);
      logbuf.append(method).append(' ').append(urlString).append(Strings.LINE_SEPARATOR);
    }
    // add to user agent
    HttpHeaders headers = this.headers;
    if (headers.userAgent == null) {
      headers.userAgent = USER_AGENT_SUFFIX;
    } else {
      headers.userAgent += " " + USER_AGENT_SUFFIX;
    }
    // headers
    HashSet<String> headerNames = new HashSet<String>();
    for (Map.Entry<String, Object> headerEntry : this.headers.entrySet()) {
      String name = headerEntry.getKey();
      String lowerCase = name.toLowerCase();
      if (!headerNames.add(lowerCase)) {
        throw new IllegalArgumentException(
            "multiple headers of the same name (headers are case insensitive): " + lowerCase);
      }
      Object value = headerEntry.getValue();
      if (value != null) {
        if (value instanceof Collection<?>) {
          for (Object repeatedValue : (Collection<?>) value) {
            addHeader(logger, logbuf, lowLevelHttpRequest, name, repeatedValue);
          }
        } else {
          addHeader(logger, logbuf, lowLevelHttpRequest, name, value);
        }
      }
    }
    // content
    HttpContent content = this.content;
    if (content != null) {
      // check if possible to log content or gzip content
      String contentEncoding = content.getEncoding();
      long contentLength = content.getLength();
      String contentType = content.getType();
      if (contentLength != 0 && contentEncoding == null
          && LogContent.isTextBasedContentType(contentType)) {
        // log content?
        if (loggable && !disableContentLogging || logger.isLoggable(Level.ALL)) {
          content = new LogContent(content, contentType, contentEncoding, contentLength);
        }
        // gzip?
        if (contentLength >= 256) {
          content = new GZipContent(content, contentType);
          contentEncoding = content.getEncoding();
          contentLength = content.getLength();
        }
      }
      // append content headers to log buffer
      if (loggable) {
        if (contentType != null) {
          logbuf.append("Content-Type: " + contentType).append(Strings.LINE_SEPARATOR);
        }
        if (contentEncoding != null) {
          logbuf.append("Content-Encoding: " + contentEncoding).append(Strings.LINE_SEPARATOR);
        }
        if (contentLength >= 0) {
          logbuf.append("Content-Length: " + contentLength).append(Strings.LINE_SEPARATOR);
        }
      }
      lowLevelHttpRequest.setContent(content);
    }
    // log from buffer
    if (loggable) {
      logger.config(logbuf.toString());
    }
    // execute
    HttpResponse response = new HttpResponse(transport, lowLevelHttpRequest.execute());
    if (!response.isSuccessStatusCode) {
      throw new HttpResponseException(response);
    }
    return response;
  }

  private static void addHeader(Logger logger, StringBuilder logbuf,
      LowLevelHttpRequest lowLevelHttpRequest, String name, Object value) {
    String stringValue = value.toString();
    if (logbuf != null) {
      logbuf.append(name).append(": ");
      if ("Authorization".equals(name) && !logger.isLoggable(Level.ALL)) {
        logbuf.append("<Not Logged>");
      } else {
        logbuf.append(stringValue);
      }
      logbuf.append(Strings.LINE_SEPARATOR);
    }
    lowLevelHttpRequest.addHeader(name, stringValue);
  }
}
