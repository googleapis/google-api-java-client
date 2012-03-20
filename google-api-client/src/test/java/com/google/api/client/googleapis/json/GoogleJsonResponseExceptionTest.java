/*
 * Copyright (c) 2011 Google Inc.
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

import com.google.api.client.googleapis.json.GoogleJsonErrorTest.ErrorTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.util.StringUtils;
import com.google.api.client.xml.atom.Atom;

import junit.framework.TestCase;

import java.io.IOException;


/**
 * Tests {@link GoogleJsonResponseException}.
 *
 * @author Yaniv Inbar
 */
@SuppressWarnings("deprecation")
public class GoogleJsonResponseExceptionTest extends TestCase {

  public void testFrom_noDetails() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    HttpResponse response = request.execute();
    HttpResponseException e = new HttpResponseException(response);
    GoogleJsonResponseException ge = GoogleJsonResponseException.from(
        GoogleJsonErrorTest.FACTORY, e.getResponse());
    assertNull(ge.getDetails());
    assertEquals("200", ge.getMessage());
  }

  public void testFrom_withDetails() throws IOException {
    HttpTransport transport = new ErrorTransport();
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    GoogleJsonResponseException ge =
        GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, response);
    assertEquals(GoogleJsonErrorTest.ERROR, GoogleJsonErrorTest.FACTORY.toString(ge.getDetails()));
    assertTrue(
        ge.getMessage(), ge.getMessage().startsWith("403" + StringUtils.LINE_SEPARATOR + "{"));
  }

  public void testFrom_detailsMissingContent() throws IOException {
    HttpTransport transport = new ErrorTransport(null, Json.CONTENT_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    try {
      request.execute();
      fail();
    } catch (HttpResponseException e) {
      GoogleJsonResponseException ge =
          GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, e.getResponse());
      assertNull(ge.getDetails());
      assertEquals("403", ge.getMessage());
    }
  }

  public void testFrom_detailsArbitraryJsonContent() throws IOException {
    HttpTransport transport = new ErrorTransport("{\"foo\":\"bar\"}", Json.CONTENT_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    try {
      request.execute();
      fail();
    } catch (HttpResponseException e) {
      GoogleJsonResponseException ge =
          GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, e.getResponse());
      assertNull(ge.getDetails());
      assertEquals("403", ge.getMessage());
    }
  }

  public void testFrom_detailsArbitraryXmlContent() throws IOException {
    HttpTransport transport = new ErrorTransport("<foo>", Atom.CONTENT_TYPE);
    HttpRequest request =
        transport.createRequestFactory().buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    try {
      request.execute();
      fail();
    } catch (HttpResponseException e) {
      GoogleJsonResponseException ge =
          GoogleJsonResponseException.from(GoogleJsonErrorTest.FACTORY, e.getResponse());
      assertNull(ge.getDetails());
      assertEquals("403", ge.getMessage());
    }
  }
}
