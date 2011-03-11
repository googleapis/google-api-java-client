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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serializes HTTP request content from an input stream into an output stream.
 * <p>
 * The {@link #type} field is required. Subclasses should implement the {@link #getLength()},
 * {@link #getInputStream()}, and {@link #retrySupported()} for their specific type of input stream.
 * <p>
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public abstract class AbstractInputStreamContent implements HttpContent {

  private final static int BUFFER_SIZE = 2048;

  /** Required content type. */
  public String type;

  /**
   * Content encoding (for example {@code "gzip"}) or {@code null} for none.
   */
  public String encoding;

  /**
   * Return an input stream for the specific implementation type of
   * {@link AbstractInputStreamContent}. If the specific implementation will return {@code true} for
   * {@link #retrySupported()} this should be a factory function which will create a new
   * {@link InputStream} from the source data whenever invoked.
   */
  abstract protected InputStream getInputStream() throws IOException;

  public void writeTo(OutputStream out) throws IOException {
    InputStream inputStream = getInputStream();
    long contentLength = getLength();
    if (contentLength < 0) {
      copy(inputStream, out);
    } else {
      byte[] buffer = new byte[BUFFER_SIZE];
      try {
        // consume no more than length
        long remaining = contentLength;
        while (remaining > 0) {
          int read = inputStream.read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
          if (read == -1) {
            break;
          }
          out.write(buffer, 0, read);
          remaining -= read;
        }
      } finally {
        inputStream.close();
      }
    }
  }

  public String getEncoding() {
    return encoding;
  }

  public String getType() {
    return type;
  }

  /**
   * Writes the content provided by the given source input stream into the given destination output
   * stream.
   * <p>
   * The input stream is guaranteed to be closed at the end of the method.
   * </p>
   * <p>
   * Sample use:
   *
   * <pre><code>
  static void downloadMedia(HttpResponse response, File file)
      throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    try {
      AbstractInputStreamContent.copy(response.getContent(), out);
    } finally {
      out.close();
    }
  }
   * </code></pre>
   * </p>
   *
   * @param inputStream source input stream
   * @param outputStream destination output stream
   * @throws IOException I/O exception
   */
  public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
    try {
      byte[] tmp = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(tmp)) != -1) {
        outputStream.write(tmp, 0, bytesRead);
      }
    } finally {
      inputStream.close();
    }
  }
}
