/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Exception thrown when an error status code is detected in an HTTP response to a Google API that
 * uses the JSON format, using the format specified in <a
 * href="http://code.google.com/apis/urlshortener/v1/getting_started.html#errors">Error
 * Responses</a>.
 *
 * <p>
 * To execute a request, call {@link #execute(JsonFactory, HttpRequest)}. This will throw a
 * {@link GoogleJsonResponseException} on an error response. To get the structured details, use
 * {@link #getDetails()}.
 * </p>
 *
 * <pre>
  static void executeShowingError(JsonFactory factory, HttpRequest request) throws IOException {
    try {
      GoogleJsonResponseException.execute(factory, request);
    } catch (GoogleJsonResponseException e) {
      System.err.println(e.getDetails());
    }
  }
 * </pre>
 *
 * @since 1.6
 * @author Yaniv Inbar
 */
public class GoogleJsonResponseException extends HttpResponseException {

  private static final long serialVersionUID = 409811126989994864L;

  /** Google JSON error details or {@code null} for none (for example if response is not JSON). */
  private final transient GoogleJsonError details;

  /** JSON factory. */
  @Deprecated
  private final transient JsonFactory jsonFactory;

  /**
   * @param jsonFactory JSON factory
   * @param response HTTP response
   * @param details Google JSON error details
   * @param message message details
   */
  private GoogleJsonResponseException(
      JsonFactory jsonFactory, HttpResponse response, GoogleJsonError details, String message) {
    super(response, message);
    this.jsonFactory = jsonFactory;
    this.details = details;
  }

  /**
   * Returns the Google JSON error details or {@code null} for none (for example if response is not
   * JSON).
   */
  public final GoogleJsonError getDetails() {
    return details;
  }

  /**
   * Returns the JSON factory.
   *
   * @deprecated (scheduled to be removed in 1.9)
   */
  @Deprecated
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns a new instance of {@link GoogleJsonResponseException}.
   *
   * <p>
   * If there is a JSON error response, it is parsed using {@link GoogleJsonError}, which can be
   * inspected using {@link #getDetails()}. Otherwise, the full response content is read and
   * included in the exception message.
   * </p>
   *
   * @param jsonFactory JSON factory
   * @param response HTTP response
   * @return new instance of {@link GoogleJsonResponseException}
   */
  public static GoogleJsonResponseException from(JsonFactory jsonFactory, HttpResponse response) {
    // details
    Preconditions.checkNotNull(jsonFactory);
    GoogleJsonError details = null;
    String detailString = null;
    String contentType = response.getContentType();
    try {
      if (!response.isSuccessStatusCode() && contentType != null
          && contentType.startsWith(Json.CONTENT_TYPE) && response.getContent() != null) {
        JsonParser parser = null;
        try {
          parser = JsonHttpParser.parserForResponse(jsonFactory, response);
          JsonToken currentToken = parser.getCurrentToken();
          // token is null at start, so get next token
          if (currentToken == null) {
            currentToken = parser.nextToken();
          }
          // check for empty content
          if (currentToken != null) {
            // make sure there is an "error" key
            parser.skipToKey("error");
            if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
              details = parser.parseAndClose(GoogleJsonError.class, null);
              detailString = details.toPrettyString();
            }
          }
        } catch (IOException exception) {
          // it would be bad to throw an exception while throwing an exception
          exception.printStackTrace();
        } finally {
          if (parser == null) {
            response.ignore();
          } else if (details == null) {
            parser.close();
          }
        }
      } else {
        detailString = response.parseAsString();
      }
    } catch (IOException exception) {
      // it would be bad to throw an exception while throwing an exception
      exception.printStackTrace();
    }
    // message
    StringBuilder message = HttpResponseException.computeMessageBuffer(response);
    if (!com.google.common.base.Strings.isNullOrEmpty(detailString)) {
      message.append(StringUtils.LINE_SEPARATOR).append(detailString);
    }
    // result
    return new GoogleJsonResponseException(jsonFactory, response, details, message.toString());
  }

  /**
   * Executes an HTTP request using {@link HttpRequest#execute()}, but throws a
   * {@link GoogleJsonResponseException} on error instead of {@link HttpResponseException}.
   *
   * @param jsonFactory JSON factory
   * @param request HTTP request
   * @return HTTP response for an HTTP success code (or error code if
   *         {@link HttpRequest#getThrowExceptionOnExecuteError()})
   * @throws GoogleJsonResponseException for an HTTP error code (only if not
   *         {@link HttpRequest#getThrowExceptionOnExecuteError()})
   * @throws IOException some other kind of I/O exception
   * @since 1.7
   */
  public static HttpResponse execute(JsonFactory jsonFactory, HttpRequest request)
      throws GoogleJsonResponseException, IOException {
    Preconditions.checkNotNull(jsonFactory);
    boolean originalThrowExceptionOnExecuteError = request.getThrowExceptionOnExecuteError();
    if (originalThrowExceptionOnExecuteError) {
      request.setThrowExceptionOnExecuteError(false);
    }
    HttpResponse response = request.execute();
    request.setThrowExceptionOnExecuteError(originalThrowExceptionOnExecuteError);
    if (!originalThrowExceptionOnExecuteError || response.isSuccessStatusCode()) {
      return response;
    }
    throw GoogleJsonResponseException.from(jsonFactory, response);
  }
}
