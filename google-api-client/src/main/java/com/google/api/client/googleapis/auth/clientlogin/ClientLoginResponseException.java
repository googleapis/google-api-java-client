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

package com.google.api.client.googleapis.auth.clientlogin;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin.ErrorInfo;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Exception thrown when an error status code is detected in an HTTP response to a Google
 * ClientLogin request in {@link ClientLogin} .
 *
 * <p>
 * To get the structured details, use {@link #getDetails()}.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
@Beta
public class ClientLoginResponseException extends HttpResponseException {

  private static final long serialVersionUID = 4974317674023010928L;

  /** Error details or {@code null} for none. */
  private final transient ErrorInfo details;

  /**
   * @param builder builder
   * @param details error details or {@code null} for none
   */
  ClientLoginResponseException(Builder builder, ErrorInfo details) {
    super(builder);
    this.details = details;
  }

  /** Return the error details or {@code null} for none. */
  public final ErrorInfo getDetails() {
    return details;
  }
}
