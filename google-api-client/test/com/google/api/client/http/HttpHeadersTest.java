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

import com.google.api.client.util.ArrayMap;

import junit.framework.TestCase;

import java.util.Collections;

/**
 * Tests {@link HttpHeaders}.
 *
 * @author Yaniv Inbar
 */
public class HttpHeadersTest extends TestCase {

  public HttpHeadersTest() {
  }

  public HttpHeadersTest(String name) {
    super(name);
  }

  public void testCanonicalMap() {
    HttpHeaders headers = new HttpHeaders();
    headers.acceptEncoding = null;
    assertTrue(headers.canonicalMap().isEmpty());
    headers.userAgent = "foo";
    assertEquals(ArrayMap.of("user-agent", Collections.singleton("foo")), headers.canonicalMap());
    headers.set("a", "b");
    assertEquals(
        ArrayMap.of("a", Collections.singleton("b"), "user-agent", Collections.singleton("foo")),
        headers.canonicalMap());
  }

  public void testBasicAuthentication() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuthentication("Aladdin", "open sesame");
    assertEquals("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==", headers.authorization);
  }
}
