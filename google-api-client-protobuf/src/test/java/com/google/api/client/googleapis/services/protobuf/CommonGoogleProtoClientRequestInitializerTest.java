/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.services.protobuf;

import com.google.api.client.googleapis.testing.services.protobuf.MockGoogleProtoClient;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Key;
import com.google.protobuf.MessageLite;

import junit.framework.TestCase;

/**
 * Tests {@link CommonGoogleProtoClientRequestInitializer}.
 *
 * @author Yaniv Inbar
 */
public class CommonGoogleProtoClientRequestInitializerTest extends TestCase {

  public static class MyRequest extends AbstractGoogleProtoClientRequest<String> {
    @Key
    String key;

    protected MyRequest(MockGoogleProtoClient client, String method, String uriTemplate,
        MessageLite message, Class<String> responseClass) {
      super(client, method, uriTemplate, message, responseClass);
    }
  }

  public void testInitialize() throws Exception {
    CommonGoogleProtoClientRequestInitializer key =
        new CommonGoogleProtoClientRequestInitializer("foo");
    MockGoogleProtoClient client =
        new MockGoogleProtoClient.Builder(new MockHttpTransport(), HttpTesting.SIMPLE_URL, "test/",
            null).setApplicationName("Test Application").build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    assertNull(request.key);
    key.initialize(request);
    assertEquals("foo", request.key);
  }
}
