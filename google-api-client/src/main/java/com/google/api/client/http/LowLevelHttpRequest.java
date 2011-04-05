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
 * Low-level HTTP request.
 * <p>
 * This allows providing a different implementation of the HTTP request that is more compatible with
 * the Java environment used.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class LowLevelHttpRequest {

  /**
   * Adds a header to the HTTP request.
   * <p>
   * Note that multiple headers of the same name need to be supported, in which case
   * {@link #addHeader} will be called for each instance of the header.
   *
   * @param name header name
   * @param value header value
   */
  public abstract void addHeader(String name, String value);

  /**
   * Sets the HTTP request content.
   *
   * @throws IOException I/O exception
   */
  public abstract void setContent(HttpContent content) throws IOException;

  /**
   * Sets the connection and read timeouts.
   *
   * <p>
   * Default implementation does nothing, but subclasses should normally override.
   * </p>
   *
   * @param connectTimeout timeout in milliseconds to establish a connection or {@code 0} for an
   *        infinite timeout
   * @param readTimeout Timeout in milliseconds to read data from an established connection or
   *        {@code 0} for an infinite timeout
   * @since 1.4
   */
  @SuppressWarnings("unused")
  public void setTimeout(int connectTimeout, int readTimeout) throws IOException {

  }

  /** Executes the request and returns a low-level HTTP response object. */
  public abstract LowLevelHttpResponse execute() throws IOException;
}
