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

package com.google.api.client.javanet;

import com.google.api.client.http.HttpTransport;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * HTTP low-level transport based on the {@code java.net} package.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends HttpTransport {

  /**
   * Singleton instance of this transport.
   *
   * @deprecated (scheduled to be removed in 1.4) Use {@link #NetHttpTransport()}
   */
  @Deprecated
  public static final NetHttpTransport INSTANCE = new NetHttpTransport();

  /**
   * Sets the connection timeout to a specified timeout in milliseconds by calling
   * {@link HttpURLConnection#setConnectTimeout(int)}, or a negative value avoid calling that
   * method. By default it is 20 seconds.
   *
   * @since 1.1
   */
  public int connectTimeout = 20 * 1000;

  /**
   * Sets the read timeout to a specified timeout in milliseconds by calling
   * {@link HttpURLConnection#setReadTimeout(int)}, or a negative value avoid calling that method.
   * By default it is 20 seconds.
   *
   * @since 1.1
   */
  public int readTimeout = 20 * 1000;

  @Override
  public boolean supportsHead() {
    return true;
  }

  @Override
  public NetHttpRequest buildDeleteRequest(String url) throws IOException {
    return new NetHttpRequest(this, "DELETE", url);
  }

  @Override
  public NetHttpRequest buildGetRequest(String url) throws IOException {
    return new NetHttpRequest(this, "GET", url);
  }

  @Override
  public NetHttpRequest buildHeadRequest(String url) throws IOException {
    return new NetHttpRequest(this, "HEAD", url);
  }

  @Override
  public NetHttpRequest buildPostRequest(String url) throws IOException {
    return new NetHttpRequest(this, "POST", url);
  }

  @Override
  public NetHttpRequest buildPutRequest(String url) throws IOException {
    return new NetHttpRequest(this, "PUT", url);
  }
}
