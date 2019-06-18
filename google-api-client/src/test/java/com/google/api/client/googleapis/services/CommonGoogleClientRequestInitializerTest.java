/*
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

import com.google.api.client.googleapis.testing.services.MockGoogleClient;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link CommonGoogleClientRequestInitializer}.
 *
 * @author Yaniv Inbar
 */
public class CommonGoogleClientRequestInitializerTest extends TestCase {

  public static class MyRequest extends AbstractGoogleClientRequest<String> {
    @Key
    String key;

    protected MyRequest(MockGoogleClient client, String method, String uriTemplate,
        HttpContent content, Class<String> responseClass) {
      super(client, method, uriTemplate, content, responseClass);
    }
  }

  public void testInitialize() throws Exception {
    CommonGoogleClientRequestInitializer key = new CommonGoogleClientRequestInitializer("foo");
    MockGoogleClient client = new MockGoogleClient.Builder(new MockHttpTransport(),
        HttpTesting.SIMPLE_URL, "test/", null, null).setApplicationName("Test Application").build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    assertNull(request.key);
    key.initialize(request);
    assertEquals("foo", request.key);
  }

  public void testInitializeSetsUserAgent() throws IOException {
    GoogleClientRequestInitializer requestInitializer = CommonGoogleClientRequestInitializer.newBuilder()
        .setUserAgent("test user agent")
        .build();
    MockGoogleClient client = new MockGoogleClient.Builder(new MockHttpTransport(), HttpTesting.SIMPLE_URL, "test/", null, null)
        .setGoogleClientRequestInitializer(requestInitializer)
        .setApplicationName("My Application")
        .build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    requestInitializer.initialize(request);
    HttpHeaders headers = request.getRequestHeaders();
    assertEquals("test user agent", headers.getUserAgent());
  }

  public void testInitializeSetsUserProject() throws IOException {
    GoogleClientRequestInitializer requestInitializer = CommonGoogleClientRequestInitializer.newBuilder()
        .setUserProject("my quota project")
        .build();
    MockGoogleClient client = new MockGoogleClient.Builder(new MockHttpTransport(), HttpTesting.SIMPLE_URL, "test/", null, null)
        .setGoogleClientRequestInitializer(requestInitializer)
        .setApplicationName("My Application")
        .build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    requestInitializer.initialize(request);
    HttpHeaders headers = request.getRequestHeaders();
    assertEquals("my quota project", headers.get("X-Goog-User-Project"));
  }

  public void testInitializeSetsRequestReason() throws IOException {
    GoogleClientRequestInitializer requestInitializer = CommonGoogleClientRequestInitializer.newBuilder()
        .setRequestReason("some request reason")
        .build();
    MockGoogleClient client = new MockGoogleClient.Builder(new MockHttpTransport(), HttpTesting.SIMPLE_URL, "test/", null, null)
        .setGoogleClientRequestInitializer(requestInitializer)
        .setApplicationName("My Application")
        .build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    requestInitializer.initialize(request);
    HttpHeaders headers = request.getRequestHeaders();
    assertEquals("some request reason", headers.get("X-Goog-Request-Reason"));
  }
}
