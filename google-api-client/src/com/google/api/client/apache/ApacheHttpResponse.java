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

package com.google.api.client.apache;

import com.google.api.client.http.LowLevelHttpResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;

import java.io.IOException;
import java.io.InputStream;

final class ApacheHttpResponse extends LowLevelHttpResponse {

  private final org.apache.http.HttpResponse response;
  private final Header[] allHeaders;

  ApacheHttpResponse(org.apache.http.HttpResponse response) {
    this.response = response;
    allHeaders = response.getAllHeaders();
  }

  @Override
  public int getStatusCode() {
    StatusLine statusLine = response.getStatusLine();
    return statusLine == null ? 0 : statusLine.getStatusCode();
  }

  @Override
  public InputStream getContent() throws IOException {
    HttpEntity entity = response.getEntity();
    return entity == null ? null : entity.getContent();
  }

  @Override
  public String getContentEncoding() {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      Header contentEncodingHeader = entity.getContentEncoding();
      if (contentEncodingHeader != null) {
        return contentEncodingHeader.getValue();
      }
    }
    return null;
  }

  @Override
  public long getContentLength() {
    HttpEntity entity = response.getEntity();
    return entity == null ? -1 : entity.getContentLength();
  }

  @Override
  public String getContentType() {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      Header contentTypeHeader = entity.getContentType();
      if (contentTypeHeader != null) {
        return contentTypeHeader.getValue();
      }
    }
    return null;
  }

  @Override
  public String getReasonPhrase() {
    StatusLine statusLine = response.getStatusLine();
    return statusLine == null ? null : statusLine.getReasonPhrase();
  }

  @Override
  public String getStatusLine() {
    StatusLine statusLine = response.getStatusLine();
    return statusLine == null ? null : statusLine.toString();
  }

  public String getHeaderValue(String name) {
    return response.getLastHeader(name).getValue();
  }

  @Override
  public int getHeaderCount() {
    return allHeaders.length;
  }

  @Override
  public String getHeaderName(int index) {
    return allHeaders[index].getName();
  }

  @Override
  public String getHeaderValue(int index) {
    return allHeaders[index].getValue();
  }
}
