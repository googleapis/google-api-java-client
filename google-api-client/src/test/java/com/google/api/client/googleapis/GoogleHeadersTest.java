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

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import java.io.InputStream;

/**
 * Tests for the {@link GoogleHeaders} class.
 *
 * @author Matthias Linder (mlinder)
 */
@Deprecated
public class GoogleHeadersTest extends TestCase {
  private static class MockLowLevelHttpResponse extends LowLevelHttpResponse {
    private String[] headerNames;
    private String[] headerValues;

    public MockLowLevelHttpResponse(String[] headers, String[] headerValues) {
      this.headerNames = headers;
      this.headerValues = headerValues;
    }

    @Override
    public InputStream getContent() {
      return null;
    }

    @Override
    public String getContentEncoding() {
      return null;
    }

    @Override
    public long getContentLength() {
      return 0;
    }

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public String getStatusLine() {
      return null;
    }

    @Override
    public int getStatusCode() {
      return 0;
    }

    @Override
    public String getReasonPhrase() {
      return null;
    }

    @Override
    public int getHeaderCount() {
      return headerNames.length;
    }

    @Override
    public String getHeaderName(int index) {
      return headerNames[index];
    }

    @Override
    public String getHeaderValue(int index) {
      return headerValues[index];
    }

  }

  public void testFromHttpResponse_normalFlow() throws Exception {
    String[] names = new String[] { "Content-Type", "Slug" };
    String[] values = new String[] { "foo/bar", "123456789" };
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse(names, values);

    // Test the normal HttpHeaders class
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.fromHttpResponse(httpResponse, null);
    assertEquals("foo/bar", httpHeaders.getContentType());
    assertEquals(ImmutableList.of("123456789"), httpHeaders.get("Slug"));

    // Test creating a GoogleHeaders obj using the HttpHeaders' data
    GoogleHeaders googleHeaders = new GoogleHeaders();
    googleHeaders.fromHttpHeaders(httpHeaders);
    assertEquals("foo/bar", googleHeaders.getContentType());
    assertEquals("123456789", googleHeaders.getSlug());
  }

  public void testFromHttpResponse_doubleConvert() throws Exception {
    String[] names = new String[] { "Content-Type", "Slug" };
    String[] values = new String[] { "foo/bar", "123456789" };
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse(names, values);

    // Test the normal HttpHeaders class
    GoogleHeaders googleHeaders = new GoogleHeaders();
    googleHeaders.fromHttpResponse(httpResponse, null);
    assertEquals("foo/bar", googleHeaders.getContentType());
    assertEquals("123456789", googleHeaders.getSlug());

    // Test creating a GoogleHeaders obj using the HttpHeaders' data
    GoogleHeaders googleHeaders2 = new GoogleHeaders();
    googleHeaders2.fromHttpHeaders(googleHeaders);
    assertEquals("foo/bar", googleHeaders2.getContentType());
    assertEquals("123456789", googleHeaders2.getSlug());
  }
}
