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

import junit.framework.TestCase;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Tests {@link HttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class HttpRequestTest extends TestCase {

  private static final EnumSet<HttpMethod> BASIC_METHODS =
      EnumSet.of(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE);
  private static final EnumSet<HttpMethod> OTHER_METHODS =
      EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH);

  public HttpRequestTest(String name) {
    super(name);
  }

  public void testNotSupportedByDefault() throws IOException {
    MockLowLevelHttpTransport lowLevelTransport = new MockLowLevelHttpTransport();
    HttpTransport.setLowLevelHttpTransport(lowLevelTransport);
    HttpTransport transport = new HttpTransport();
    HttpRequest request = transport.buildHeadRequest();
    request.setUrl("http://www.google.com");
    for (HttpMethod method : BASIC_METHODS) {
      request.method = method;
      request.execute();
    }
    for (HttpMethod method : OTHER_METHODS) {
      lowLevelTransport.supportedOptionalMethods.remove(method);
      request.method = method;
      try {
        request.execute();
        fail("expected IllegalArgumentException");
      } catch (IllegalArgumentException e) {
      }
      lowLevelTransport.supportedOptionalMethods.add(method);
      request.execute();
    }
  }
}
