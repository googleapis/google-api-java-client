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
 */
public class MockHttpTransport extends HttpTransport {

  public EnumSet<HttpMethod> supportedOptionalMethods =
      EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH);

  @Override
  public LowLevelHttpRequest buildDeleteRequest(String url) {
    return new MockLowLevelHttpRequest();
  }

  @Override
  public LowLevelHttpRequest buildGetRequest(String url) {
    return new MockLowLevelHttpRequest();
  }

  @Override
  public LowLevelHttpRequest buildHeadRequest(String url) throws IOException {
    if (!supportsHead()) {
      return super.buildHeadRequest(url);
    }
    return new MockLowLevelHttpRequest();
  }

  @Override
  public LowLevelHttpRequest buildPatchRequest(String url) throws IOException {
    if (!supportsPatch()) {
      return super.buildPatchRequest(url);
    }
    return new MockLowLevelHttpRequest();
  }

  @Override
  public LowLevelHttpRequest buildPostRequest(String url) {
    return new MockLowLevelHttpRequest();
  }

  @Override
  public LowLevelHttpRequest buildPutRequest(String url) {
    return new MockLowLevelHttpRequest();
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
