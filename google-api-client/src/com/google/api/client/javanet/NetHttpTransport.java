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

package com.google.api.client.javanet;

import com.google.api.client.http.LowLevelHttpTransport;

import java.io.IOException;

/**
 * HTTP low-level transport based on the {@code java.net} package.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class NetHttpTransport extends LowLevelHttpTransport {

  /** Singleton instance of this transport. */
  public static final NetHttpTransport INSTANCE = new NetHttpTransport();

  NetHttpTransport() {
  }

  @Override
  public boolean supportsHead() {
    return true;
  }

  @Override
  public NetHttpRequest buildDeleteRequest(String url) throws IOException {
    return new NetHttpRequest("DELETE", url);
  }

  @Override
  public NetHttpRequest buildGetRequest(String url) throws IOException {
    return new NetHttpRequest("GET", url);
  }

  @Override
  public NetHttpRequest buildHeadRequest(String url) throws IOException {
    return new NetHttpRequest("HEAD", url);
  }

  @Override
  public NetHttpRequest buildPostRequest(String url) throws IOException {
    return new NetHttpRequest("POST", url);
  }

  @Override
  public NetHttpRequest buildPutRequest(String url) throws IOException {
    return new NetHttpRequest("PUT", url);
  }
}
