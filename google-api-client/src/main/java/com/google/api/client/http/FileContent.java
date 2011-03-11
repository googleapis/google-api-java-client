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

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of a file.
 * <p>
 * The {@link #type} fields is required.
 * <p>
 * Sample use:
 *
 * <pre>
 * <code>
  private static void setRequestJpegContent(HttpRequest request, File jpegFile) {
    FileContent content = new FileContent(jpegFile);
    content.type = "image/jpeg";
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.4
 * @author moshenko@google.com (Jacob Moshenko)
 */
public final class FileContent extends AbstractInputStreamContent {

  private final File file;

  /**
   * @param file File handle which will be used to create input streams.
   */
  public FileContent(File file) {
    Preconditions.checkNotNull(file);
    this.file = file;
  }

  public long getLength() {
    return file.length();
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  protected InputStream getInputStream() throws FileNotFoundException {
    return new FileInputStream(file);
  }
}
