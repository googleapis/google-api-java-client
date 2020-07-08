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

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest.ApiClientVersion;
import com.google.api.client.googleapis.testing.services.MockGoogleClient;
import com.google.api.client.googleapis.testing.services.MockGoogleClientRequest;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.io.BaseEncoding;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import junit.framework.TestCase;

/**
 * Tests {@link AbstractGoogleClientRequest}.
 *
 * @author Yaniv Inbar
 */
public class AbstractGoogleClientRequestTest extends TestCase {

  private static final String ROOT_URL = "https://www.googleapis.com/test/";
  private static final String SERVICE_PATH = "path/v1/";
  private static final String URI_TEMPLATE = "tests/{testId}";
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final JsonObjectParser JSON_OBJECT_PARSER = new JsonObjectParser(JSON_FACTORY);
  private static final String ERROR_CONTENT =
      "{\"error\":{\"code\":401,\"errors\":[{\"domain\":\"global\","
      + "\"location\":\"Authorization\",\"locationType\":\"header\","
      + "\"message\":\"me\",\"reason\":\"authError\"}],\"message\":\"me\"}}";
  private String originalOsName;
  private String originalOsVersion;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // save the original system properties so we can mock them out
    this.originalOsName = System.getProperty("os.name");
    this.originalOsVersion = System.getProperty("os.version");
  }

  @Override
  protected void tearDown() throws Exception {
    // restore the original system properties
    System.setProperty("os.name", originalOsName);
    System.setProperty("os.version", originalOsVersion);

    super.tearDown();
  }

  public void testExecuteUnparsed_error() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            assertEquals("GET", method);
            assertEquals("https://www.googleapis.com/test/path/v1/tests/foo", url);
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setStatusCode(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
            result.setContentType(Json.MEDIA_TYPE);
            result.setContent(ERROR_CONTENT);
            return result;
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    try {
      request.put("testId", "foo");
      request.executeUnparsed();
      fail("expected " + HttpResponseException.class);
    } catch (HttpResponseException e) {
      // expected
      assertTrue(e.getMessage().startsWith("401"));
    }
  }

  public void testExecuteUsingHead() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            assertEquals("HEAD", method);
            assertEquals("https://www.googleapis.com/test/path/v1/tests/foo", url);
            return new MockLowLevelHttpResponse();
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    request.put("testId", "foo");
    request.executeUsingHead();
  }

  public void testBuildHttpRequest_emptyContent() throws Exception {
    for (String method : Arrays.asList("GET", "HEAD", "DELETE", "FOO")) {
      subtestBuildHttpRequest_emptyContent(method, false);
    }
    for (String method : Arrays.asList("POST", "PUT", "PATCH")) {
      subtestBuildHttpRequest_emptyContent(method, true);
    }
  }

  private void subtestBuildHttpRequest_emptyContent(String method, boolean expectEmptyContent)
      throws Exception {
    HttpTransport transport = new MockHttpTransport();
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request =
        new MockGoogleClientRequest<String>(client, method, URI_TEMPLATE, null, String.class);
    HttpRequest httpRequest = request.buildHttpRequest();
    if (expectEmptyContent) {
      assertTrue(httpRequest.getContent() instanceof EmptyContent);
    } else {
      assertNull(httpRequest.getContent());
    }
  }

  public void testCheckRequiredParameter() throws Exception {
    HttpTransport transport = new MockHttpTransport();
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);

    // Should not throw an Exception.
    request.checkRequiredParameter("Not Null", "notNull()");

    try {
      request.checkRequiredParameter(null, "content.getTest().getAnotherTest()");
      fail("Expected " + IllegalArgumentException.class);
    } catch (IllegalArgumentException iae) {
      // Expected.
    }
  }

  public void testExecute_void() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            return new MockLowLevelHttpResponse().setContent("{\"a\":\"ignored\"}")
                .setContentType(Json.MEDIA_TYPE);
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<Void> request =
        new MockGoogleClientRequest<Void>(client, HttpMethods.GET, URI_TEMPLATE, null, Void.class);
    Void v = request.execute();
    assertNull(v);
  }

  public void testUserAgentSuffix() throws Exception {
    AssertUserAgentTransport transport = new AssertUserAgentTransport();
    // Specify an Application Name.
    String applicationName = "Test Application";
    transport.expectedUserAgent = applicationName + " "
        + "Google-API-Java-Client/" + GoogleUtils.VERSION + " "
        + HttpRequest.USER_AGENT_SUFFIX;
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).setApplicationName(
            applicationName).build();
    MockGoogleClientRequest<Void> request =
        new MockGoogleClientRequest<Void>(client, HttpMethods.GET, URI_TEMPLATE, null, Void.class);
    request.executeUnparsed();
  }

  public void testUserAgent() throws IOException {
    AssertUserAgentTransport transport = new AssertUserAgentTransport();
    transport.expectedUserAgent = "Google-API-Java-Client/" + GoogleUtils.VERSION + " "
        + HttpRequest.USER_AGENT_SUFFIX;
    // Don't specify an Application Name.
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).build();
    MockGoogleClientRequest<Void> request =
        new MockGoogleClientRequest<>(client, HttpMethods.GET, URI_TEMPLATE, null, Void.class);
    request.executeUnparsed();
  }

  public void testSetsApiClientHeader() throws IOException {
    HttpTransport transport = new AssertHeaderTransport("X-Goog-Api-Client", "gl-java/\\d+\\.\\d+\\.\\d+.*");
    MockGoogleClient client = new MockGoogleClient.Builder(
        transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).build();
    MockGoogleClientRequest<Void> request =
        new MockGoogleClientRequest<>(client, HttpMethods.GET, URI_TEMPLATE, null, Void.class);
    request.executeUnparsed();
  }

  public void testSetsApiClientHeaderWithOsVersion() {
    System.setProperty("os.name", "My OS");
    System.setProperty("os.version", "1.2.3");

    String version = new ApiClientVersion().toString();
    assertTrue("Api version should contain the os version", version.matches(".* my-os/1.2.3"));
  }

  public void testSetsApiClientHeaderWithoutOsVersion() {
    System.setProperty("os.name", "My OS");
    System.clearProperty("os.version");
    assertNull(System.getProperty("os.version"));

    String version = new ApiClientVersion().toString();
    assertFalse("Api version should not contain the os version", version.matches(".*my-os.*"));
  }

  public void testSetsApiClientHeaderDiscoveryVersion() throws IOException {
    HttpTransport transport = new AssertHeaderTransport("X-Goog-Api-Client", ".*gdcl/\\d+\\.\\d+\\.\\d+.*");
    MockGoogleClient client = new MockGoogleClient.Builder(
            transport, ROOT_URL, SERVICE_PATH, JSON_OBJECT_PARSER, null).build();
    MockGoogleClientRequest<Void> request =
            new MockGoogleClientRequest<>(client, HttpMethods.GET, URI_TEMPLATE, null, Void.class);
    request.executeUnparsed();
  }

  public void testReturnRawInputStream_defaultFalse() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            return new MockLowLevelHttpResponse().setContentEncoding("gzip").setContent(new ByteArrayInputStream(
                BaseEncoding.base64()
                    .decode("H4sIAAAAAAAAAPNIzcnJV3DPz0/PSVVwzskvTVEILskvSkxPVQQA/LySchsAAAA=")));
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(transport, ROOT_URL, SERVICE_PATH,
        JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    InputStream inputStream = request.executeAsInputStream();
    // The response will be wrapped because of gzip encoding
    assertFalse(inputStream instanceof ByteArrayInputStream);
  }

  public void testReturnRawInputStream_True() throws Exception {
    HttpTransport transport = new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(final String method, final String url) {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() {
            return new MockLowLevelHttpResponse().setContentEncoding("gzip").setContent(new ByteArrayInputStream(
                BaseEncoding.base64()
                    .decode("H4sIAAAAAAAAAPNIzcnJV3DPz0/PSVVwzskvTVEILskvSkxPVQQA/LySchsAAAA=")));
          }
        };
      }
    };
    MockGoogleClient client = new MockGoogleClient.Builder(transport, ROOT_URL, SERVICE_PATH,
        JSON_OBJECT_PARSER, null).setApplicationName(
        "Test Application").build();
    MockGoogleClientRequest<String> request = new MockGoogleClientRequest<String>(
        client, HttpMethods.GET, URI_TEMPLATE, null, String.class);
    request.setReturnRawInputStream(true);
    InputStream inputStream = request.executeAsInputStream();
    // The response will not be wrapped due to setReturnRawInputStream(true)
    assertTrue(inputStream instanceof ByteArrayInputStream);
  }

  private class AssertHeaderTransport extends MockHttpTransport {
    String expectedHeader;
    String expectedHeaderValue;

    AssertHeaderTransport(String header, String value) {
      expectedHeader = header;
      expectedHeaderValue = value;
    }

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() throws IOException {
          String firstHeader = getFirstHeaderValue(expectedHeader);
          assertTrue(
              String.format(
                  "Expected header value to match %s, instead got %s.",
                  expectedHeaderValue,
                  firstHeader
              ),
              firstHeader.matches(expectedHeaderValue)
          );
          return new MockLowLevelHttpResponse();
        }
      };
    }
  }

  private class AssertUserAgentTransport extends MockHttpTransport {
    String expectedUserAgent;

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
      return new MockLowLevelHttpRequest() {
        @Override
        public LowLevelHttpResponse execute() throws IOException {
          assertEquals(expectedUserAgent, getFirstHeaderValue("User-Agent"));
          return new MockLowLevelHttpResponse();
        }
      };
    }
  }
}
