/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis.auth.storage;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.MockHttpContent;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;

/**
 * Tests {@link GoogleStorageAuthentication}.
 * 
 * @author Yaniv Inbar
 */
public class GoogleStorageAuthenticationTest extends TestCase {

  public GoogleStorageAuthenticationTest() {
  }

  public GoogleStorageAuthenticationTest(String name) {
    super(name);
  }

  public void test() throws IOException {
    HttpTransport transport = GoogleTransport.create();
    GoogleStorageAuthentication.authorize(transport, "GOOGTS7C7FUP3AIRVJTE",
        "abc");
    HttpExecuteIntercepter intercepter = transport.intercepters.get(1);
    HttpRequest request = transport.buildPutRequest();
    request
        .setUrl("http://travel-maps.commondatastorage.googleapis.com/europe/france/paris.jpg");
    GoogleHeaders headers = (GoogleHeaders) request.headers;
    headers.date = "Mon, 15 Feb  2010 21:30:39 GMT";
    MockHttpContent content = new MockHttpContent();
    content.length = 4539;
    content.type = "image/jpg";
    request.content = content;
    headers.googAcl = "public-read";
    headers.set("x-goog-meta-reviewer", Arrays.asList("bob", "jane"));
    intercepter.intercept(request);
    assertEquals("GOOG1 GOOGTS7C7FUP3AIRVJTE:ovyTUuOaD+E6l/Xu+eOAhZ/8LKk=",
        request.headers.authorization);
  }
}
