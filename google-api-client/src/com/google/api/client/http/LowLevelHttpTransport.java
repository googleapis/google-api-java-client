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
 * Low-level HTTP transport.
 * <p>
 * This allows providing a different implementation of the HTTP transport that is more compatible
 * with the Java environment used.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class LowLevelHttpTransport {

  /**
   * Returns whether this HTTP transport implementation supports the {@code HEAD} request method.
   * <p>
   * Default implementation returns {@code false}.
   */
  public boolean supportsHead() {
    return false;
  }

  /**
   * Returns whether this HTTP transport implementation supports the {@code PATCH} request method.
   * <p>
   * Default implementation returns {@code false}.
   */
  public boolean supportsPatch() {
    return false;
  }

  /**
   * Builds a {@code DELETE} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public abstract LowLevelHttpRequest buildDeleteRequest(String url) throws IOException;

  /**
   * Builds a {@code GET} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public abstract LowLevelHttpRequest buildGetRequest(String url) throws IOException;

  /**
   * Builds a {@code HEAD} request. Won't be called if {@link #supportsHead()} returns {@code false}
   * .
   * <p>
   * Default implementation throws an {@link UnsupportedOperationException}.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Builds a {@code PATCH} request. Won't be called if {@link #supportsPatch()} returns {@code
   * false}.
   * <p>
   * Default implementation throws an {@link UnsupportedOperationException}.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    throw new UnsupportedOperationException();
  }

  /**
   * Builds a {@code POST} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public abstract LowLevelHttpRequest buildPostRequest(String url) throws IOException;

  /**
   * Builds a {@code PUT} request.
   *
   * @param url URL
   * @throws IOException I/O exception
   */
  public abstract LowLevelHttpRequest buildPutRequest(String url) throws IOException;
}
