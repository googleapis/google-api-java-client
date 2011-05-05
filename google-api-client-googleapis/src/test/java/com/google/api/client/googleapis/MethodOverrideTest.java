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

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.EnumSet;

/**
 * Tests {@link MethodOverride}.
 *
 * @author Yaniv Inbar
 */
public class MethodOverrideTest extends TestCase {

  public MethodOverrideTest(String name) {
    super(name);
  }

  public void testIntercept() throws IOException {
    MockHttpTransport transport = new MockHttpTransport();
    subtestIntercept(EnumSet.noneOf(HttpMethod.class), transport, new MethodOverride());
    subtestIntercept(EnumSet.noneOf(HttpMethod.class), transport,
        new MethodOverride(EnumSet.noneOf(HttpMethod.class)));
    subtestIntercept(
        EnumSet.of(HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.PATCH), transport,
        new MethodOverride(EnumSet.allOf(HttpMethod.class)));
    transport.supportedOptionalMethods.clear();
    subtestIntercept(
        EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH), transport, new MethodOverride());
  }

  private void subtestIntercept(EnumSet<HttpMethod> methodsThatShouldOverride,
      HttpTransport transport, MethodOverride interceptor) throws IOException {
    for (HttpMethod method : HttpMethod.values()) {
      subtestIntercept(methodsThatShouldOverride.contains(method), transport, interceptor, method);
    }
  }

  private void subtestIntercept(boolean shouldOverride, HttpTransport transport,
      MethodOverride interceptor, HttpMethod method) throws IOException {
    HttpRequest request = transport.createRequestFactory().buildRequest(method, null, null);
    interceptor.intercept(request);
    assertEquals(method.toString(), shouldOverride ? HttpMethod.POST : method, request.method);
    assertEquals(method.toString(), shouldOverride ? method.toString() : null,
        request.headers.get("X-HTTP-Method-Override"));
  }
}
