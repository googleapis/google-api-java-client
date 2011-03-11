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

package com.google.api.client.http;

import com.google.api.client.util.Strings;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of byte array.
 * <p>
 * The {@link #type} field is required.
 * <p>
 * Sample use:
 *
 * <pre>
 * <code>
  static void setRequestJsonContent(HttpRequest request, String json) {
    InputStreamContent content = new ByteArrayContent(json);
    content.type = "application/json";
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public final class ByteArrayContent extends AbstractInputStreamContent {

  private final byte[] byteArray;
  private static final byte[] EMPTY_ARRAY = new byte[] {};

  /**
   * @param array Data source for creating input streams.
   */
  public ByteArrayContent(byte[] array) {
    Preconditions.checkNotNull(array);
    this.byteArray = array;
  }

  /**
   * Create an instance from the byte contents of the string. This assumes that the string is
   * encoded in UTF-8 and uses {@code Strings.toBytesUtf8()} to perform the conversion.
   *
   * @param contentString String to use as the source data for creating input streams
   */
  public ByteArrayContent(String contentString) {
    Preconditions.checkNotNull(contentString);
    this.byteArray = Strings.toBytesUtf8(contentString);
  }

  /**
   * Create an instance with no data.
   */
  public ByteArrayContent() {
    byteArray = EMPTY_ARRAY;
  }

  public long getLength() {
    return byteArray.length;
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  protected InputStream getInputStream() {
    return new ByteArrayInputStream(byteArray);
  }
}
