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

package com.google.api.client.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Serializes another source of HTTP content using GZip compression.
 *
 * @author Yaniv Inbar
 */
final class GZipContent implements HttpContent {

  private final HttpContent httpContent;

  private final String contentType;

  /**
   * @param httpContent another source of HTTP content
   */
  GZipContent(HttpContent httpContent, String contentType) {
    this.httpContent = httpContent;
    this.contentType = contentType;
  }

  public void writeTo(OutputStream out) throws IOException {
    GZIPOutputStream zipper = new GZIPOutputStream(out);
    httpContent.writeTo(zipper);
    zipper.finish();
  }

  public String getEncoding() {
    return "gzip";
  }

  public long getLength() {
    return -1;
  }

  public String getType() {
    return contentType;
  }
}
