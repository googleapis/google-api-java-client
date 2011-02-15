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

import com.google.api.client.http.LowLevelHttpResponse;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

final class UrlFetchResponse extends LowLevelHttpResponse {

  private final ArrayList<String> headerNames = new ArrayList<String>();
  private final ArrayList<String> headerValues = new ArrayList<String>();
  private final HTTPResponse fetchResponse;
  private String contentEncoding;
  private String contentType;
  private long contentLength;

  UrlFetchResponse(HTTPResponse fetchResponse) {
    this.fetchResponse = fetchResponse;
    for (HTTPHeader header : fetchResponse.getHeaders()) {
      String name = header.getName();
      String value = header.getValue();
      // Note: URLFetch will merge any duplicate headers with the same key and join their values
      // using ", " as separator. However, ", " is also common inside values, such as in Expires or
      // Set-Cookie headers.
      if (name != null && value != null) {
        headerNames.add(name);
        headerValues.add(value);
        if ("content-type".equalsIgnoreCase(name)) {
          contentType = value;
        } else if ("content-encoding".equalsIgnoreCase(name)) {
          contentEncoding = value;
        } else if ("content-length".equalsIgnoreCase(name)) {
          try {
            contentLength = Long.parseLong(value);
          } catch (NumberFormatException e) {
            // ignore
          }
        }
      }
    }
  }

  @Override
  public int getStatusCode() {
    return fetchResponse.getResponseCode();
  }

  @Override
  public InputStream getContent() {
    return new ByteArrayInputStream(fetchResponse.getContent());
  }

  @Override
  public String getContentEncoding() {
    return contentEncoding;
  }

  @Override
  public long getContentLength() {
    return contentLength;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getReasonPhrase() {
    // unfortunately not available
    return null;
  }

  @Override
  public String getStatusLine() {
    // unfortunately not available
    return null;
  }

  @Override
  public int getHeaderCount() {
    return headerNames.size();
  }

  @Override
  public String getHeaderName(int index) {
    return headerNames.get(index);
  }

  @Override
  public String getHeaderValue(int index) {
    return headerValues.get(index);
  }
}
