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

package com.google.api.client.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

/**
 * Wraps another source of HTTP content without modifying the content, but also
 * possibly logging this content.
 * 
 * <p>
 * Content is only logged if {@link Level#CONFIG} is loggable.
 * 
 * @author Yaniv Inbar
 */
final class LogContent implements HttpContent {

  private final HttpContent httpContent;
  private final String contentType;
  private final String contentEncoding;
  private final long contentLength;

  /**
   * @param httpContent another source of HTTP content
   */
  LogContent(HttpContent httpContent, String contentType,
      String contentEncoding, long contentLength) {
    this.httpContent = httpContent;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.contentEncoding = contentEncoding;
  }

  public void writeTo(OutputStream out) throws IOException {
    ByteArrayOutputStream debugStream = new ByteArrayOutputStream();
    this.httpContent.writeTo(debugStream);
    byte[] debugContent = debugStream.toByteArray();
    HttpTransport.LOGGER.config(new String(debugContent));
    out.write(debugContent);
  }

  public String getEncoding() {
    return this.contentEncoding;
  }

  public long getLength() {
    return this.contentLength;
  }

  public String getType() {
    return this.contentType;
  }
}
