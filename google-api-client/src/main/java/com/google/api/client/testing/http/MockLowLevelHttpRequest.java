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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.IOException;

/**
 * Mock for {@link LowLevelHttpRequest}.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockLowLevelHttpRequest extends LowLevelHttpRequest {

  /**
   * Request URL or {@code null} for none.
   *
   * @since 1.4
   */
  public String url;

  /**
   * Headers added in {@link #addHeader(String, String)}.
   *
   * @since 1.4
   */
  public final ListMultimap<String, String> headers = ArrayListMultimap.create();

  /**
   * HTTP content or {@code null} for none.
   *
   * @since 1.4
   */
  public HttpContent content;

  public MockLowLevelHttpRequest() {
  }

  /**
   * @param url Request URL or {@code null} for none
   * @since 1.4
   */
  public MockLowLevelHttpRequest(String url) {
    this.url = url;
  }

  @Override
  public void addHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    return new MockLowLevelHttpResponse();
  }

  @Override
  public void setContent(HttpContent content) throws IOException {
    this.content = content;
  }
}
