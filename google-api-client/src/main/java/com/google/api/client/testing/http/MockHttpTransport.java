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

package com.google.api.client.testing.http;

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;

import java.io.IOException;
import java.util.EnumSet;


/**
 * Mock for {@link HttpTransport}.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockHttpTransport extends HttpTransport {

  /**
   * Set of supported optional methods or {@link HttpMethod#HEAD} and {@link HttpMethod#PATCH} by
   * default.
   */
  public EnumSet<HttpMethod> supportedOptionalMethods =
      EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH);

  @Override
  public LowLevelHttpRequest buildDeleteRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    if (!supportsHead()) {
      return super.buildHeadRequest(url);
    }
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    if (!supportsPatch()) {
      return super.buildPatchRequest(url);
    }
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public LowLevelHttpRequest buildPutRequest(String url) throws IOException {
    return new MockLowLevelHttpRequest(url);
  }

  @Override
  public boolean supportsHead() {
    return supportedOptionalMethods.contains(HttpMethod.HEAD);
  }

  @Override
  public boolean supportsPatch() {
    return supportedOptionalMethods.contains(HttpMethod.PATCH);
  }
}
