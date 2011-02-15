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

import com.google.api.client.util.Key;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Tests {@link GoogleUrl}.
 *
 * @author Tony Aiuto
 */
public class GoogleUrlTest extends TestCase {

  final String SERVER = "http://google.com";

  public GoogleUrlTest() {
  }

  public GoogleUrlTest(String name) {
    super(name);
  }

  public void testConstructorBasic() {
    final String PATH = "/a/b";
    GoogleUrl url = GoogleUrl.create(SERVER, PATH, new Object());
    assertEquals(SERVER + PATH, url.toString());
  }

  class Parameters {
    @Key
    int a;
    @Key
    String b;
  }

  public void testConstructorWithTemplate() {
    final String PATH = "/{a}/{b}/c";
    Parameters p = new Parameters();
    p.a = 123;
    p.b = "456";
    GoogleUrl url = GoogleUrl.create(SERVER, PATH, p);
    assertEquals(SERVER + "/123/456/c", url.toString());
  }

  public void testExpansionOfKeys() {
    final String PATH = "/{a}/{b}/c";
    Parameters p = new Parameters();
    p.a = 123;
    p.b = "456";
    GoogleUrl url = GoogleUrl.create(SERVER, PATH, p);
    url.prettyprint = true;
    url.alt = "json";
    url.fields = "x,y,z";
    assertEquals(SERVER + "/123/456/c?alt=json&fields=x,y,z&prettyprint=true", url.toString());
  }

  public void testExpandTemplates_basic() {
    HashMap<String, String> requestMap = new HashMap<String, String>();
    requestMap.put("abc", "xyz");
    requestMap.put("def", "123");
    requestMap.put("unused", "not going to be expanded");
    assertEquals(
        "foo/xyz/bar/123", GoogleUrl.expandUriTemplates("foo/{abc}/bar/{def}", requestMap));
    // "abc" and "def" should be removed from the map
    assertEquals(1, requestMap.size());

    assertFalse(requestMap.containsKey("abc"));
    assertFalse(requestMap.containsKey("def"));
    assertTrue(requestMap.containsKey("unused"));
  }

  public void testExpandTemplates_noParameters() {
    // Should not raise an exception.
    GoogleUrl url = GoogleUrl.create("http://host/",  "abc/def", null);
    assertEquals("http://host/abc/def", url.toString());
    try {
      GoogleUrl.create("http://host/",  "abc/{def}/", null);
    } catch (IllegalArgumentException expectedException) {
      return;
    }
    fail();
  }

  public void testExpandTemplates_missingParameter() {
    HashMap<String, String> requestMap = new HashMap<String, String>();
    requestMap.put("abc", "xyz");
    try {
      GoogleUrl.expandUriTemplates("foo/{abc}/bar/{def}", requestMap);
    } catch (IllegalArgumentException expectedException) {
      assertEquals("missing required path parameter: def", expectedException.getMessage());
      return;
    }
    fail();
  }
}
