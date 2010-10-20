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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.MockLowLevelHttpRequest;
import com.google.api.client.http.MockLowLevelHttpResponse;
import com.google.api.client.http.MockLowLevelHttpTransport;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link DiscoveryDocument}.
 *
 * @author Yaniv Inbar
 */
public class DiscoveryDocumentTest extends TestCase {

  public DiscoveryDocumentTest(String name) {
    super(name);
  }

  static class MyTransport extends MockLowLevelHttpTransport {
    String getUrl;

    @Override
    public LowLevelHttpRequest buildGetRequest(String url) {
      getUrl = url;
      return new MockLowLevelHttpRequest() {

        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContent("{\"data\":{}}");
          return response;
        }

      };
    }
  }

  public void test() throws IOException {
    MyTransport myTransport = new MyTransport();
    HttpTransport.setLowLevelHttpTransport(myTransport);
    DiscoveryDocument.load("foo");
    assertEquals("https://www.googleapis.com/discovery/0.1/describe?api=foo", myTransport.getUrl);
  }
}
