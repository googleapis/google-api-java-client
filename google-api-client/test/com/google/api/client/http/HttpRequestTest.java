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

import junit.framework.TestCase;

/**
 * Tests {@link HttpRequest}.
 *
 * @author Yaniv Inbar
 */
public class HttpRequestTest extends TestCase {

  private static final String[] BASIC_METHODS = new String[] {"GET", "PUT", "POST", "DELETE"};
  private static final String[] OTHER_METHODS = new String[] {"HEAD", "PATCH"};

  public HttpRequestTest(String name) {
    super(name);
  }

  public void testNotSupportedByDefault() throws IOException {
    MockLowLevelHttpTransport lowLevelTransport = new MockLowLevelHttpTransport();
    HttpTransport.setLowLevelHttpTransport(lowLevelTransport);
    HttpTransport transport = new HttpTransport();
    HttpRequest request = transport.buildHeadRequest();
    request.setUrl("http://www.google.com");
    for (String method : BASIC_METHODS) {
      request.method = method;
      request.execute();
    }
    for (String method : OTHER_METHODS) {
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
