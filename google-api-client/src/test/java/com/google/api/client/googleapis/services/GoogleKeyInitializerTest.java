/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.googleapis.services;

import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.json.JsonHttpClient;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Key;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleKeyInitializer}.
 *
 * @author Yaniv Inbar
 */
@Deprecated
public class GoogleKeyInitializerTest extends TestCase {

  public static class MyRequest extends JsonHttpRequest {
    @Key
    String key;

    public MyRequest() {
      super(new JsonHttpClient(
          new MockHttpTransport(), new JacksonFactory(), HttpTesting.SIMPLE_URL, "test/", null),
          HttpMethod.GET, "", null);
    }
  }

  public void testInitialize() {
    GoogleKeyInitializer key = new GoogleKeyInitializer("foo");
    MyRequest request = new MyRequest();
    assertNull(request.key);
    key.initialize(request);
    assertEquals("foo", request.key);
  }
}
