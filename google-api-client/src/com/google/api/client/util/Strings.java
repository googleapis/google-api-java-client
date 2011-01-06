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

package com.google.api.client.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Utilities for strings.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class Strings {

  /**
   * Version of the Google API Client Library for Java.
   *
   * @since 1.3
   */
  public static final String VERSION = "1.3.0-alpha";

  /**
   * Line separator to use for this OS, i.e. {@code "\n"} or {@code "\r\n"}.
   */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * UTF-8 character set.
   *
   * @since 1.3
   */
  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  /**
   * Returns a new byte array that is the result of encoding the given string into a sequence of
   * bytes using the {@code "UTF-8"} charset.
   *
   * @param string given string
   * @return resultant byte array
   * @since 1.2
   */
  public static byte[] toBytesUtf8(String string) {
    try {
      return string.getBytes("UTF-8");
    } catch (UnsupportedEncodingException exception) {
      // UTF-8 encoding guaranteed to be supported by JVM
      throw new RuntimeException(exception);
    }
  }

  /**
   * Returns a new {@code String} by decoding the specified array of bytes using the {@code "UTF-8"}
   * charset.
   *
   * <p>
   * The length of the new {@code String} is a function of the charset, and hence may not be equal
   * to the length of the byte array.
   * </p>
   *
   * @param bytes bytes to be decoded into characters
   * @return resultant string
   * @since 1.2
   */
  public static String fromBytesUtf8(byte[] bytes) {
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException exception) {
      // UTF-8 encoding guaranteed to be supported by JVM
      throw new RuntimeException(exception);
    }
  }

  private Strings() {
  }
}
