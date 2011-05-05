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

package com.google.api.client.auth.oauth2.draft10;


import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource.Method;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.GenericData;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link AccessProtectedResource}.
 *
 * @author Yaniv Inbar
 */
public class AccessProtectedResourceTest extends TestCase {

  public void testAccessProtectedResource_header() throws IOException {
    AccessProtectedResource credential =
        new AccessProtectedResource("abc", Method.AUTHORIZATION_HEADER);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals("OAuth abc", request.headers.authorization);
  }

  public void testAccessProtectedResource_queryParam() throws IOException {
    AccessProtectedResource credential = new AccessProtectedResource("abc", Method.QUERY_PARAMETER);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals("abc", request.url.get("oauth_token"));
  }

  public void testAccessProtectedResource_body() throws IOException {
    AccessProtectedResource credential =
        new AccessProtectedResource("abc", Method.FORM_ENCODED_BODY);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals(
        "abc", ((GenericData) ((UrlEncodedContent) request.content).data).get("oauth_token"));
  }

  private HttpRequest subtestAccessProtectedResource(AccessProtectedResource credential)
      throws IOException {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(new GenericUrl());
    request.execute();
    return request;
  }

  public void testAccessProtectedResource_expiredHeader() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.AUTHORIZATION_HEADER, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.headers.get("Authorization").contains("OAuth def");
          }
        });
    assertEquals("OAuth def", request.headers.authorization);
  }

  public void testAccessProtectedResource_expiredQueryParam() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.QUERY_PARAMETER, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.url.contains("oauth_token=def");
          }
        });
    assertEquals("def", request.url.get("oauth_token"));
  }

  public void testAccessProtectedResource_expiredBody() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.FORM_ENCODED_BODY, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return "def".equals(
                ((GenericData) ((UrlEncodedContent) req.content).data).get("oauth_token"));
          }
        });
    assertEquals(
        "def", ((GenericData) ((UrlEncodedContent) request.content).data).get("oauth_token"));
  }

  interface CheckAuth {
    boolean checkAuth(MockLowLevelHttpRequest req);
  }

  private HttpRequest subtestAccessProtectedResource_expired(
      Method method, final CheckAuth checkAuth) throws IOException {
    final AccessProtectedResource credential =
        new AccessProtectedResource("abc", method, new MockHttpTransport() {
          @Override
          public LowLevelHttpRequest buildPostRequest(String url) {
            return new MockLowLevelHttpRequest(url) {
              @Override
              public LowLevelHttpResponse execute() {
                final MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
                response.contentType = Json.CONTENT_TYPE;
                response.setContent("{\"access_token\":\"def\"}");
                return response;
              }
            };
          }
        }, new JacksonFactory(), "http://foo.com", "id", "secret", "refreshToken");
    class MyTransport extends MockHttpTransport {
      boolean resetAccessToken;

      @Override
      public LowLevelHttpRequest buildDeleteRequest(String url) {
        return new MockLowLevelHttpRequest(url) {
          @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            if (!checkAuth.checkAuth(this)) {
              response.statusCode = 401;
              if (resetAccessToken) {
                credential.setAccessToken("def");
              }
            }
            return response;
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(new GenericUrl());
    request.execute();
    credential.setAccessToken("abc");
    transport.resetAccessToken = true;
    request.execute();
    return request;
  }
}
