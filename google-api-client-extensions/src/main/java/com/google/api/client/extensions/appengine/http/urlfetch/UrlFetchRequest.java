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

package com.google.api.client.extensions.appengine.http.urlfetch;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.ResponseTooLargeException;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author Yaniv Inbar
 */
final class UrlFetchRequest extends LowLevelHttpRequest {

  private HttpContent content;
  private final UrlFetchTransport transport;
  private final HTTPMethod method;
  private final HTTPRequest request;

  UrlFetchRequest(UrlFetchTransport transport, String requestMethod, String url)
      throws IOException {
    this.transport = transport;
    method = HTTPMethod.valueOf(requestMethod);
    FetchOptions options = FetchOptions.Builder.doNotFollowRedirects().disallowTruncate();
    request = new HTTPRequest(new URL(url), method, options);
  }

  @Override
  public void addHeader(String name, String value) {
    request.addHeader(new HTTPHeader(name, value));
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    // write content
    if (content != null) {
      String contentType = content.getType();
      if (contentType != null) {
        addHeader("Content-Type", contentType);
      }
      String contentEncoding = content.getEncoding();
      if (contentEncoding != null) {
        addHeader("Content-Encoding", contentEncoding);
      }
      long contentLength = content.getLength();
      if (contentLength >= 0) {
        addHeader("Content-Length", Long.toString(contentLength));
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      content.writeTo(out);
      request.setPayload(out.toByteArray());
    }
    // connect
    double deadline = transport.deadline;
    if (deadline >= 0) {
      request.getFetchOptions().setDeadline(deadline);
    }
    URLFetchService service = URLFetchServiceFactory.getURLFetchService();
    try {
      HTTPResponse response = service.fetch(request);
      return new UrlFetchResponse(response);
    } catch (ResponseTooLargeException e) {
      IOException ioException = new IOException();
      ioException.initCause(e);
      throw ioException;
    }
  }

  @Override
  public void setContent(HttpContent content) {
    this.content = content;
  }
}
