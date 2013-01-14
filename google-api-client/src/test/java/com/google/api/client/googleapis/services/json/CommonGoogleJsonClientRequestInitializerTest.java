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

package com.google.api.client.googleapis.services.json;

import com.google.api.client.googleapis.testing.services.json.MockGoogleJsonClient;
import com.google.api.client.http.HttpContent;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.Key;

import junit.framework.TestCase;

/**
 * Tests {@link CommonGoogleJsonClientRequestInitializer}.
 *
 * @author Yaniv Inbar
 */
public class CommonGoogleJsonClientRequestInitializerTest extends TestCase {

  public static class MyRequest extends AbstractGoogleJsonClientRequest<String> {
    @Key
    String key;

    protected MyRequest(MockGoogleJsonClient client, String method, String uriTemplate,
        HttpContent content, Class<String> responseClass) {
      super(client, method, uriTemplate, content, responseClass);
    }
  }

  public void testInitialize() throws Exception {
    CommonGoogleJsonClientRequestInitializer key =
        new CommonGoogleJsonClientRequestInitializer("foo");
    MockGoogleJsonClient client = new MockGoogleJsonClient.Builder(
        new MockHttpTransport(), new JacksonFactory(), HttpTesting.SIMPLE_URL, "test/", null,
        false).setApplicationName("Test Application").build();
    MyRequest request = new MyRequest(client, "GET", "", null, String.class);
    assertNull(request.key);
    key.initialize(request);
    assertEquals("foo", request.key);
  }
}
