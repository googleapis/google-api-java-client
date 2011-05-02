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
import com.google.common.base.Preconditions;

import java.io.IOException;
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

  /**
   * User agent suffix for all requests.
   *
   * @since 1.4
   */
  public static final String USER_AGENT_SUFFIX = "Google-API-Java-Client/" + Strings.VERSION;

  /**
   * HTTP request execute interceptor to intercept the start of {@link #execute()} (before executing
   * the HTTP request) or {@code null} for none.
   *
   * @since 1.4
   */
  public HttpExecuteInterceptor interceptor;

  /**
   * HTTP request headers.
   * <p>
   * For backwards compatibility, its value is initialized by calling {@code clone()} on the
   * {@link HttpTransport#defaultHeaders}, which by default is an instance of {@link HttpHeaders}.
   * </p>
   */
  public HttpHeaders headers;

  /**
   * HTTP response headers.
   * <p>
   * For example, this can be used if you want to use a subclass of {@link HttpHeaders} called
   * MyHeaders to process the response:
   * </p>
   *
   * <pre>
  static String executeAndGetValueOfSomeCustomHeader(HttpRequest request) {
    MyHeaders responseHeaders = new MyHeaders();
    request.responseHeaders = responseHeaders;
    HttpResponse response = request.execute();
    return responseHeaders.someCustomHeader;
  }
   * </pre>
   * <p>
   * For backwards compatibility, its value is initialized by calling {@code clone()} on the
   * {@link HttpTransport#defaultHeaders}, which by default is an instance of {@link HttpHeaders}.
   * </p>
   *
   * @since 1.4
   */
  public HttpHeaders responseHeaders;

  /**
   * Set the number of retries that will be allowed to execute as the result of an
   * {@link HttpUnsuccessfulResponseHandler} before being terminated or {@code 0} to not retry
   * requests. The default value is {@code 10}.
   *
   * @since 1.4
   */
  public int numRetries = 10;

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
   *
   * @since 1.3
   */
  public HttpMethod method;

  // TODO(yanivi): support more HTTP methods?

  /** HTTP request URL. */
  public GenericUrl url;

  /**
   * Timeout in milliseconds to establish a connection or {@code 0} for an infinite timeout.
   * <p>
   * By default it is 20 seconds.
   * </p>
   *
   * @since 1.4
   */
  public int connectTimeout = 20 * 1000;

  /**
   * Timeout in milliseconds to read data from an established connection or {@code 0} for an
   * infinite timeout.
   * <p>
   * By default it is 20 seconds.
   * </p>
   *
   * @since 1.4
   */
  public int readTimeout = 20 * 1000;

  /**
   * HTTP unsuccessful (non-2XX) response handler.
   *
   * @since 1.4
   */
  public HttpUnsuccessfulResponseHandler unsuccessfulResponseHandler;

  /**
   * Map from normalized content type to HTTP parser.
   *
   * @since 1.4
   */
  private final Map<String, HttpParser> contentTypeToParserMap;

  /**
   * @param transport HTTP transport
   * @param method HTTP request method (may be {@code null}
   */
  // using HttpTransport.defaultHeaders for backwards compatibility
  @SuppressWarnings("deprecation")
  HttpRequest(HttpTransport transport, HttpMethod method) {
    this.transport = transport;
    headers = transport.defaultHeaders.clone();
    responseHeaders = transport.defaultHeaders.clone();
    contentTypeToParserMap = transport.contentTypeToParserMap.clone();
    this.method = method;
  }

  /**
   * Sets the {@link #url} based on the given encoded URL string.
   *
   * @deprecated (scheduled to be removed in 1.5) Use {@link GenericUrl#GenericUrl(String)}
   */
  @Deprecated
  public void setUrl(String encodedUrl) {
    url = new GenericUrl(encodedUrl);
  }

  /**
   * Adds an HTTP response content parser.
   * <p>
   * If there is already a previous parser defined for this new parser (as defined by
   * {@link #getParser(String)} then the previous parser will be removed.
   * </p>
   *
   * @since 1.4
   */
  public void addParser(HttpParser parser) {
    String contentType = normalizeMediaType(parser.getContentType());
    contentTypeToParserMap.put(contentType, parser);
  }

  /**
   * Returns the HTTP response content parser to use for the given content type or {@code null} if
   * none is defined.
   *
   * @param contentType content type or {@code null} for {@code null} result
   * @return HTTP response content parser or {@code null} for {@code null} input
   * @since 1.4
   */
  public final HttpParser getParser(String contentType) {
    contentType = normalizeMediaType(contentType);
    return contentTypeToParserMap.get(contentType);
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
  @SuppressWarnings("deprecation")
  public HttpResponse execute() throws IOException {
    boolean requiresRetry = false;
    boolean retrySupported = false;
    Preconditions.checkArgument(numRetries >= 0);
    int retriesRemaining = numRetries;
    HttpResponse response = null;

    Preconditions.checkNotNull(method);
    Preconditions.checkNotNull(url);

    do {
      // Cleanup any unneeded response from a previous iteration
      if (response != null) {
        response.ignore();
      }
      // run the interceptor
      if (interceptor != null) {
        interceptor.intercept(this);
      }
      // first run the execute intercepters
      for (HttpExecuteIntercepter intercepter : transport.intercepters) {
        intercepter.intercept(this);
      }
      // build low-level HTTP request
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
          Preconditions.checkArgument(
              transport.supportsHead(), "HTTP transport doesn't support HEAD");
          lowLevelHttpRequest = transport.buildHeadRequest(urlString);
          break;
        case PATCH:
          Preconditions.checkArgument(
              transport.supportsPatch(), "HTTP transport doesn't support PATCH");
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
      if (headers.userAgent == null) {
        headers.userAgent = USER_AGENT_SUFFIX;
      } else {
        headers.userAgent += " " + USER_AGENT_SUFFIX;
      }
      // headers
      HashSet<String> headerNames = new HashSet<String>();
      for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
        String name = headerEntry.getKey();
        String lowerCase = name.toLowerCase();
        Preconditions.checkArgument(headerNames.add(lowerCase),
            "multiple headers of the same name (headers are case insensitive): %s", lowerCase);
        Object value = headerEntry.getValue();
        if (value != null) {
          Class<? extends Object> valueClass = value.getClass();
          if (value instanceof Iterable<?> || valueClass.isArray()) {
            for (Object repeatedValue : Types.iterableOf(value)) {
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
      lowLevelHttpRequest.setTimeout(connectTimeout, readTimeout);
      response = new HttpResponse(this, lowLevelHttpRequest.execute());

      // We need to make sure our content type can support retry
      // null content is inherently able to be retried
      retrySupported = retriesRemaining > 0 && (content == null || content.retrySupported());
      requiresRetry = false;

      // Even if we don't have the potential to retry, we might want to run the
      // handler to fix conditions (like expired tokens) that might cause us
      // trouble on our next request
      if (!response.isSuccessStatusCode && unsuccessfulResponseHandler != null) {
        requiresRetry = unsuccessfulResponseHandler.handleResponse(this, response, retrySupported);
      }

      // Once there are no more retries remaining, this will be -1
      retriesRemaining--;
    } while (requiresRetry && retrySupported);

    if (!response.isSuccessStatusCode) {
      throw new HttpResponseException(response);
    }
    return response;
  }

  private static void addHeader(Logger logger, StringBuilder logbuf,
      LowLevelHttpRequest lowLevelHttpRequest, String name, Object value) {
    // ignore nulls
    if (value == null || Data.isNull(value)) {
      return;
    }
    // compute value
    String stringValue =
        value instanceof Enum<?> ? FieldInfo.of((Enum<?>) value).getName() : value.toString();
    // log header
    if (logbuf != null) {
      logbuf.append(name).append(": ");
      if ("Authorization".equals(name) && !logger.isLoggable(Level.ALL)) {
        logbuf.append("<Not Logged>");
      } else {
        logbuf.append(stringValue);
      }
      logbuf.append(Strings.LINE_SEPARATOR);
    }
    // add header
    lowLevelHttpRequest.addHeader(name, stringValue);
  }

  /**
   * Returns the normalized media type without parameters of the form {@code type "/" subtype"} as
   * specified in <a href="http://tools.ietf.org/html/rfc2616#section-3.7">Media Types</a>.
   *
   * @param mediaType unnormalized media type with possible parameters or {@code null} for {@code
   *        null} result
   * @return normalized media type without parameters or {@code null} for {@code null} input
   * @since 1.4
   */
  public static String normalizeMediaType(String mediaType) {
    if (mediaType == null) {
      return null;
    }
    int semicolon = mediaType.indexOf(';');
    return semicolon == -1 ? mediaType : mediaType.substring(0, semicolon);
  }
}
