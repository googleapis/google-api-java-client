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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Tests {@link MethodOverride}.
 *
 * @author Yaniv Inbar
 */
@SuppressWarnings("deprecation")
public class MethodOverrideTest extends TestCase {

  private static final List<String> OVERRIDDEN_METHODS = ImmutableList.of(
      "FOO", HttpMethods.DELETE, HttpMethods.HEAD, HttpMethods.OPTIONS, HttpMethods.PATCH,
      HttpMethods.PUT, HttpMethods.TRACE);

  private static final List<String> SUPPORTED_METHODS = ImmutableList.<String>builder()
      .addAll(OVERRIDDEN_METHODS).add(HttpMethods.GET, HttpMethods.POST).build();

  public MethodOverrideTest(String name) {
    super(name);
  }

  public void testIntercept() throws Exception {
    subtestIntercept(ImmutableSet.<String>of(), new MockHttpTransport(), new MethodOverride());
    subtestIntercept(OVERRIDDEN_METHODS, new MockHttpTransport(),
        new MethodOverride.Builder().setOverrideAllMethods(true).build());
    subtestIntercept(OVERRIDDEN_METHODS, MockHttpTransport.builder()
        .setSupportedMethods(ImmutableSet.<String>of(HttpMethods.GET, HttpMethods.POST)).build(),
        new MethodOverride());
  }

  private void subtestIntercept(Collection<String> methodsThatShouldOverride,
      HttpTransport transport, MethodOverride interceptor) throws Exception {
    for (String requestMethod : SUPPORTED_METHODS) {
      subtestIntercept(
          methodsThatShouldOverride.contains(requestMethod), transport, interceptor, requestMethod);
    }
  }

  private void subtestIntercept(boolean shouldOverride, HttpTransport transport,
      MethodOverride interceptor, String requestMethod) throws Exception {
    HttpRequest request = transport.createRequestFactory()
        .buildRequest(requestMethod, HttpTesting.SIMPLE_GENERIC_URL, null);
    interceptor.intercept(request);
    assertEquals(requestMethod, shouldOverride ? HttpMethods.POST : requestMethod,
        request.getRequestMethod());
    assertEquals(requestMethod, shouldOverride ? requestMethod : null,
        request.getHeaders().get(MethodOverride.HEADER));
  }

  public void testInterceptMaxLength() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    GenericUrl url = new GenericUrl(HttpTesting.SIMPLE_URL);
    url.set("a", "foo");
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    new MethodOverride().intercept(request);
    assertEquals(HttpMethods.GET, request.getRequestMethod());
    assertNull(request.getHeaders().get(MethodOverride.HEADER));
    assertNull(request.getContent());
    char[] arr = new char[MethodOverride.MAX_URL_LENGTH];
    Arrays.fill(arr, 'x');
    url.set("a", new String(arr));
    request.setUrl(url);
    new MethodOverride().intercept(request);
    assertEquals(HttpMethods.POST, request.getRequestMethod());
    assertEquals(HttpMethods.GET, request.getHeaders().get(MethodOverride.HEADER));
    char[] arr2 = new char[arr.length + 2];
    Arrays.fill(arr2, 'x');
    arr2[0] = 'a';
    arr2[1] = '=';
    UrlEncodedContent content = (UrlEncodedContent) request.getContent();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    content.writeTo(out);
    assertEquals(new String(arr2), out.toString());
  }
}
