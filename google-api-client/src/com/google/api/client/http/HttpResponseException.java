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

import java.io.IOException;

/**
 * Exception thrown when an error status code is detected in an HTTP response.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HttpResponseException extends IOException {

  static final long serialVersionUID = 1;

  /** HTTP response. */
  public final HttpResponse response;

  /**
   * @param response HTTP response
   */
  public HttpResponseException(HttpResponse response) {
    super(computeMessage(response));
    this.response = response;
  }

  /** Returns an exception message to use for the given HTTP response. */
  public static String computeMessage(HttpResponse response) {
    String statusMessage = response.statusMessage;
    int statusCode = response.statusCode;
    if (statusMessage == null) {
      return String.valueOf(statusCode);
    }
    StringBuilder buf = new StringBuilder(4 + statusMessage.length());
    return buf.append(statusCode).append(' ').append(statusMessage).toString();
  }
}
