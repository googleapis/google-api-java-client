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

package com.google.api.client.googleapis;

import com.google.api.client.googleapis.testing.MockGoogleClient;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import junit.framework.TestCase;

/**
 * Tests {@link AbstractGoogleClient}.
 *
 * @author Yaniv Inbar
 */
public class AbstractGoogleClientTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final JsonObjectParser JSON_HTTP_PARSER = new JsonObjectParser(JSON_FACTORY);
  private static final HttpTransport TRANSPORT = new NetHttpTransport();

  public static class MyC4lient extends AbstractGoogleClient {

    public MyC4lient(HttpTransport transport, String rootUrl, String servicePath) {
      super(transport, null, rootUrl, servicePath, null);
    }

    public static class Builder extends AbstractGoogleClient.Builder {

      protected Builder(HttpTransport transport) {
        super(transport, HttpTesting.SIMPLE_URL, "test/", null, null);
      }

      @Override
      public AbstractGoogleClient build() {
        return new MyC4lient(getTransport(), getRootUrl(), getServicePath());
      }

    }
  }

  static private class TestRemoteRequestInitializer implements GoogleClientRequestInitializer {

    boolean isCalled;

    TestRemoteRequestInitializer() {
    }

    public void initialize(AbstractGoogleClientRequest<?> request) {
      isCalled = true;
    }
  }

  public void testGoogleClientBuilder() {
    String rootUrl = "http://www.testgoogleapis.com/test/";
    String servicePath = "path/v1/";
    GoogleClientRequestInitializer jsonHttpRequestInitializer = new TestRemoteRequestInitializer();
    String applicationName = "Test Application";

    AbstractGoogleClient.Builder setApplicationName = new MockGoogleClient.Builder(
        TRANSPORT, rootUrl, servicePath, JSON_HTTP_PARSER, null).setApplicationName(applicationName)
        .setGoogleClientRequestInitializer(jsonHttpRequestInitializer);
    AbstractGoogleClient client = setApplicationName.build();

    assertEquals(rootUrl + servicePath, client.getBaseUrl());
    assertEquals(rootUrl, client.getRootUrl());
    assertEquals(servicePath, client.getServicePath());
    assertEquals(applicationName, client.getApplicationName());
    assertEquals(jsonHttpRequestInitializer, client.getGoogleClientRequestInitializer());
  }

  public void testBaseServerAndBasePathBuilder() {
    AbstractGoogleClient client = new MockGoogleClient.Builder(
        TRANSPORT, "http://www.testgoogleapis.com/test/", "path/v1/", JSON_HTTP_PARSER,
        null).setApplicationName("Test Application")
        .setRootUrl("http://www.googleapis.com/test/").setServicePath("path/v2/").build();

    assertEquals("http://www.googleapis.com/test/path/v2/", client.getBaseUrl());
  }

  public void testInitialize() throws Exception {
    TestRemoteRequestInitializer remoteRequestInitializer = new TestRemoteRequestInitializer();
    AbstractGoogleClient client = new MockGoogleClient.Builder(
        TRANSPORT, "http://www.test.com/", "", JSON_HTTP_PARSER, null).setApplicationName(
        "Test Application").setGoogleClientRequestInitializer(remoteRequestInitializer).build();
    client.initialize(null);
    assertTrue(remoteRequestInitializer.isCalled);
  }

  public void testExecute() throws Exception {
    final String testBaseUrl = "http://www.test.com/";
    final String testUriTemplate = "uri/template";
    HttpTransport transport = new MockHttpTransport() {
        @Override
      public LowLevelHttpRequest buildRequest(String name, final String url) {
        return new MockLowLevelHttpRequest() {
            @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            // Assert the requested URL is the expected one.
            assertEquals(testBaseUrl + testUriTemplate, url);
            return response;
          }
        };
      }
    };
    AbstractGoogleClient client = new MockGoogleClient.Builder(
        transport, testBaseUrl, "", JSON_HTTP_PARSER, null).setApplicationName("Test Application")
        .build();
    client.executeUnparsed(HttpMethods.GET, new GenericUrl(testBaseUrl + testUriTemplate), null);
  }
}
