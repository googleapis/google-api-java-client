/*
 * Copyright (c) 2013 Google Inc.
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

import com.google.api.client.http.AbstractHttpContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * HTTP request wrapped as a content part of a multipart/mixed request.
 *
 * @author Yaniv Inbar
 */
class HttpRequestContent extends AbstractHttpContent {

  static final String NEWLINE = "\r\n";

  /** HTTP request. */
  private final HttpRequest request;

  HttpRequestContent(HttpRequest request) {
    super("application/http");
    this.request = request;
  }

  public void writeTo(OutputStream out) throws IOException {
    Writer writer = new OutputStreamWriter(out, getCharset());
    // write method and URL
    writer.write(request.getRequestMethod());
    writer.write(" ");
    writer.write(request.getUrl().build());
    writer.write(NEWLINE);
    // write headers
    HttpHeaders headers = new HttpHeaders();
    headers.fromHttpHeaders(request.getHeaders());
    headers.setAcceptEncoding(null).setUserAgent(null)
        .setContentEncoding(null).setContentType(null).setContentLength(null);
    // analyze the content
    HttpContent content = request.getContent();
    if (content != null) {
      headers.setContentType(content.getType());
      // NOTE: batch does not support gzip encoding
      long contentLength = content.getLength();
      if (contentLength != -1) {
        headers.setContentLength(contentLength);
      }
    }
    HttpHeaders.serializeHeadersForMultipartRequests(headers, null, null, writer);
    // write content
    if (content != null) {
      writer.write(NEWLINE);
      writer.flush();
      content.writeTo(out);
    }
  }
}
