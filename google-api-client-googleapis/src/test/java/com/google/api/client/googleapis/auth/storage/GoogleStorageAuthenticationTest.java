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

package com.google.api.client.googleapis.auth.storage;

import com.google.api.client.auth.HmacSha;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpContent;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests {@link GoogleStorageAuthentication}.
 *
 * @author Yaniv Inbar
 */
public class GoogleStorageAuthenticationTest extends TestCase {

  private static final String ACCESS_KEY = "GOOGTS7C7FUP3AIRVJTE";
  private static final String SECRET = "abc";

  public GoogleStorageAuthenticationTest(String name) {
    super(name);
  }

  public void test() throws Exception {
    subtest("http://travel-maps.commondatastorage.googleapis.com/europe/france/paris.jpg",
        "PUT\n" + "\n" + "image/jpg\n" + "Mon, 15 Feb  2010 21:30:39 GMT\n"
            + "x-goog-acl:public-read\n" + "x-goog-meta-reviewer:bob,jane\n"
            + "/travel-maps/europe/france/paris.jpg");
    subtest("http://travel-maps.commondatastorage.googleapis.com/europe/france/paris.jpg?acl",
        "PUT\n" + "\n" + "image/jpg\n" + "Mon, 15 Feb  2010 21:30:39 GMT\n"
            + "x-goog-acl:public-read\n" + "x-goog-meta-reviewer:bob,jane\n"
            + "/travel-maps/europe/france/paris.jpg?acl");
    subtest("http://travel-maps.commondatastorage.googleapis.com?acl",
        "PUT\n" + "\n" + "image/jpg\n" + "Mon, 15 Feb  2010 21:30:39 GMT\n"
            + "x-goog-acl:public-read\n" + "x-goog-meta-reviewer:bob,jane\n" + "/travel-maps?acl");
    subtest("http://travel-maps.commondatastorage.googleapis.com",
        "PUT\n" + "\n" + "image/jpg\n" + "Mon, 15 Feb  2010 21:30:39 GMT\n"
            + "x-goog-acl:public-read\n" + "x-goog-meta-reviewer:bob,jane\n" + "/travel-maps");
    subtest("http://travel-maps.commondatastorage.googleapis.com/",
        "PUT\n" + "\n" + "image/jpg\n" + "Mon, 15 Feb  2010 21:30:39 GMT\n"
            + "x-goog-acl:public-read\n" + "x-goog-meta-reviewer:bob,jane\n" + "/travel-maps/");
  }

  private void subtest(String url, String messageToSign) throws Exception {
    HttpTransport transport = new MockHttpTransport();
    GoogleStorageAuthentication.authorize(transport, ACCESS_KEY, SECRET);
    HttpExecuteIntercepter intercepter = transport.intercepters.get(0);
    HttpRequest request = transport.buildPutRequest();
    request.setUrl(url);
    GoogleHeaders headers = new GoogleHeaders();
    headers.date = "Mon, 15 Feb  2010 21:30:39 GMT";
    MockHttpContent content = new MockHttpContent();
    content.length = 4539;
    content.type = "image/jpg";
    request.content = content;
    headers.googAcl = "public-read";
    headers.set("x-goog-meta-reviewer", Arrays.asList("bob", "jane"));
    request.headers = headers;
    intercepter.intercept(request);
    assertEquals(messageToSign, "GOOG1 " + ACCESS_KEY + ":" + HmacSha.sign(SECRET, messageToSign),
        request.headers.authorization);
  }
}
