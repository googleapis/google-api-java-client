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

package com.google.api.client.googleapis.batch;

import com.google.api.client.googleapis.batch.BatchRequest.RequestInfo;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link MultipartMixedContent}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class MultipartMixedContentTest extends TestCase {

  public void testMultipartMixedContent() throws Exception {
    String request1Method = HttpMethods.POST;
    String request1Url = "http://test/dummy/url1";
    String request1ContentType = "application/json";
    String request1Content = "{\"data\":{\"foo\":{\"v1\":{}}}}";

    String request2Method = HttpMethods.GET;
    String request2Url = "http://test/dummy/url2";

    StringBuilder expectedOutput = new StringBuilder();
    expectedOutput.append("--END_OF_PART\r\n");
    expectedOutput.append("Content-Type: application/http\r\n");
    expectedOutput.append("Content-Transfer-Encoding: binary\r\n");
    expectedOutput.append("Content-ID: 1\r\n\r\n");
    expectedOutput.append(request1Method.toString() + " " + request1Url + "\r\n");
    expectedOutput.append("Accept-Encoding: gzip\r\n");
    expectedOutput.append("Content-Type: " + request1ContentType + "\r\n");
    expectedOutput.append("Content-Length: " + request1Content.length() + "\r\n\r\n");
    expectedOutput.append(request1Content + "\r\n");
    expectedOutput.append("--END_OF_PART\r\n");
    expectedOutput.append("Content-Type: application/http\r\n");
    expectedOutput.append("Content-Transfer-Encoding: binary\r\n");
    expectedOutput.append("Content-ID: 2\r\n\r\n");
    expectedOutput.append(request2Method.toString() + " " + request2Url + "\r\n");
    expectedOutput.append("Accept-Encoding: gzip\r\n\r\n");
    expectedOutput.append("--END_OF_PART--\r\n");

    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request1 = transport.createRequestFactory().buildRequest(
        request1Method, new GenericUrl(request1Url),
        new ByteArrayContent(request1ContentType, request1Content.getBytes()));
    HttpRequest request2 = transport.createRequestFactory()
        .buildRequest(request2Method, new GenericUrl(request2Url), null);
    List<RequestInfo<?, ?>> requestInfos = new ArrayList<RequestInfo<?, ?>>();
    RequestInfo<?, ?> requestInfo1 = new RequestInfo<Object, Object>(null, null, null, request1);
    RequestInfo<?, ?> requestInfo2 = new RequestInfo<Object, Object>(null, null, null, request2);
    requestInfos.add(requestInfo1);
    requestInfos.add(requestInfo2);

    HttpContent content = new MultipartMixedContent(requestInfos, "END_OF_PART");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    content.writeTo(outputStream);

    assertEquals(expectedOutput.toString(), outputStream.toString());
  }

  public void testWriteTo_nullHeaders() throws Exception {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequest request1 = transport.createRequestFactory()
        .buildPostRequest(HttpTesting.SIMPLE_GENERIC_URL, new HttpContent() {

          public long getLength() {
            return -1;
          }

          @Deprecated
          public String getEncoding() {
            return null;
          }

          public String getType() {
            return null;
          }

          public void writeTo(OutputStream out) {
          }

          public boolean retrySupported() {
            return true;
          }
        });
    List<RequestInfo<?, ?>> requestInfos = new ArrayList<RequestInfo<?, ?>>();
    RequestInfo<?, ?> requestInfo1 = new RequestInfo<Object, Object>(null, null, null, request1);
    requestInfos.add(requestInfo1);
    HttpContent content = new MultipartMixedContent(requestInfos, "END_OF_PART");
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    content.writeTo(outputStream);
    assertEquals(new StringBuilder().append("--END_OF_PART\r\n")
        .append("Content-Type: application/http\r\n")
        .append("Content-Transfer-Encoding: binary\r\n")
        .append("Content-ID: 1\r\n")
        .append("\r\n")
        .append("POST http://google.com/\r\n")
        .append("Accept-Encoding: gzip\r\n")
        .append("\r\n")
        .append("\r\n")
        .append("--END_OF_PART--\r\n")
        .toString(), outputStream.toString());
  }
}
