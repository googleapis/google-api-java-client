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

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;

/**
 * Mock for {@link LowLevelHttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class MockLowLevelHttpRequest extends LowLevelHttpRequest {

  @Override
  public void addHeader(String name, String value) {
  }

  @Override
  public LowLevelHttpResponse execute() {
    return new MockLowLevelHttpResponse();
  }

  @Override
  public void setContent(HttpContent content) {
  }
}
