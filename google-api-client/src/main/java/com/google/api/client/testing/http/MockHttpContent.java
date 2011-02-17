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

package com.google.api.client.testing.http;

import com.google.api.client.http.HttpContent;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Mock for {@link HttpContent}.
 *
 * @author Yaniv Inbar
 * @since 1.3
 */
public class MockHttpContent implements HttpContent {

  /** HTTP content encoding or {@code null} by default. */
  public String encoding;

  /** HTTP content length or {@code -1} by default. */
  public long length = -1;

  /** HTTP content type or {@code null} by default. */
  public String type;

  /** HTTP content or an empty byte array by default. */
  public byte[] content = new byte[0];

  public String getEncoding() {
    return encoding;
  }

  public long getLength() {
    return length;
  }

  public String getType() {
    return type;
  }

  public void writeTo(OutputStream out) throws IOException {
    out.write(content);
  }
}
