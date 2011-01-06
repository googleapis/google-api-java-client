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

package com.google.api.client.javanet;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Yaniv Inbar
 */
final class NetHttpRequest extends LowLevelHttpRequest {

  private final HttpURLConnection connection;
  private HttpContent content;
  private final NetHttpTransport transport;

  NetHttpRequest(NetHttpTransport transport, String requestMethod, String url) throws IOException {
    this.transport = transport;
    HttpURLConnection connection =
        this.connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod(requestMethod);
    connection.setUseCaches(false);
    connection.setInstanceFollowRedirects(false);
  }

  @Override
  public void addHeader(String name, String value) {
    connection.addRequestProperty(name, value);
  }

  @Override
  public LowLevelHttpResponse execute() throws IOException {
    HttpURLConnection connection = this.connection;
    // write content
    HttpContent content = this.content;
    if (content != null) {
      connection.setDoOutput(true);
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
      content.writeTo(connection.getOutputStream());
    }
    // connect
    NetHttpTransport transport = this.transport;
    int readTimeout = transport.readTimeout;
    if (readTimeout >= 0) {
      connection.setReadTimeout(readTimeout);
    }
    int connectTimeout = transport.connectTimeout;
    if (connectTimeout >= 0) {
      connection.setConnectTimeout(connectTimeout);
    }
    connection.connect();
    return new NetHttpResponse(connection);
  }

  @Override
  public void setContent(HttpContent content) {
    this.content = content;
  }
}
