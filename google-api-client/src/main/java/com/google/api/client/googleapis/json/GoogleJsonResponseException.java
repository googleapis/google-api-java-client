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

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Exception thrown when an error status code is detected in an HTTP response to a Google API that
 * uses the JSON format, using the format specified in <a
 * href="http://code.google.com/apis/buzz/v1/using_rest.html#errors">Error Messages in Google
 * Buzz</a>.
 *
 * <p>
 * To get the structured details, use {@link #getDetails()}.
 * </p>
 *
 * @since 1.6
 * @author Yaniv Inbar
 */
public class GoogleJsonResponseException extends HttpResponseException {

  static final long serialVersionUID = 1;

  /** Google JSON error details or {@code null} for none (for example if response is not JSON). */
  private final GoogleJsonError details;

  /** JSON factory. */
  private final JsonFactory jsonFactory;

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

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns a new instance of {@link GoogleJsonResponseException}.
   *
   * <p>
   * If there is a JSON error response, it is parsed using {@link GoogleJsonError}, which can be
   * inspected using {@link #getDetails()}. Otherwise, response content (if any) is read and
   * ignored. Either way, any content is already parsed, and trying to parse
   * {@link HttpResponse#getContent()} of {@link #getResponse()} will likely result in an
   * {@link IOException}.
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
    String contentType = response.getContentType();
    if (!response.isSuccessStatusCode() && contentType != null
        && contentType.startsWith(Json.CONTENT_TYPE)) {
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
          }
        }
      } catch (IOException exception) {
        // it would be bad to throw an exception while throwing an exception
        exception.printStackTrace();
      } finally {
        try {
          if (parser == null) {
            response.getContent().close();
          } else if (details == null) {
            parser.close();
          }
        } catch (IOException exception) {
          // it would be bad to throw an exception while throwing an exception
          exception.printStackTrace();
        }
      }
    }
    // message
    String message = computeMessage(response);
    if (details != null) {
      message += Strings.LINE_SEPARATOR + details.toPrettyString();
    }
    // result
    return new GoogleJsonResponseException(jsonFactory, response, details, message);
  }
}
