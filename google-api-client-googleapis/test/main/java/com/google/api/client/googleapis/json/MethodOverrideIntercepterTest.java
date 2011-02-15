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

package com.google.api.client.googleapis.json;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.MethodOverrideIntercepter;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.util.EnumSet;

/**
 * Tests {@link MethodOverrideIntercepter}.
 *
 * @author Yaniv Inbar
 */
public class MethodOverrideIntercepterTest extends TestCase {

  public MethodOverrideIntercepterTest(String name) {
    super(name);
  }

  public void testOverride() {
    HttpTransport transport = new MockHttpTransport();
    MethodOverrideIntercepter intercepter = GoogleUtils.useMethodOverride(transport);
    assertEquals(EnumSet.of(HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.PATCH, HttpMethod.PUT),
        intercepter.override);
  }

  public void testIntercept() {
    MockHttpTransport transport = new MockHttpTransport();
    MethodOverrideIntercepter intercepter = GoogleUtils.useMethodOverride(transport);
    subtestIntercept(intercepter.override, transport, intercepter);
    intercepter.override = EnumSet.noneOf(HttpMethod.class);
    subtestIntercept(intercepter.override, transport, intercepter);
    transport.supportedOptionalMethods.clear();
    subtestIntercept(EnumSet.of(HttpMethod.HEAD, HttpMethod.PATCH), transport, intercepter);
  }

  private void subtestIntercept(EnumSet<HttpMethod> methodsThatShouldOverride,
      HttpTransport transport, MethodOverrideIntercepter intercepter) {
    for (HttpMethod method : HttpMethod.values()) {
      subtestIntercept(methodsThatShouldOverride.contains(method), transport, intercepter, method);
    }
  }

  private void subtestIntercept(boolean shouldOverride, HttpTransport transport,
      MethodOverrideIntercepter intercepter, HttpMethod method) {
    HttpRequest request = transport.buildRequest();
    request.method = method;
    intercepter.intercept(request);
    assertEquals(method.toString(), shouldOverride ? HttpMethod.POST : method, request.method);
    assertEquals(method.toString(), shouldOverride ? method.toString() : null,
        request.headers.get("X-HTTP-Method-Override"));
  }
}
