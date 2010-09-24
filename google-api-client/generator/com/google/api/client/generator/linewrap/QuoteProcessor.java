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

package com.google.api.client.generator.linewrap;

/**
 * Processes quotes in a line. Used to detect if there are any quotes. Both {@code '"'} and {@code
 * '\''} are valid quote characters.
 *
 * @author Yaniv Inbar
 */
final class QuoteProcessor {

  private boolean lastSlash = false;

  private char lastQuote = 0;

  /**
   * Returns whether to skip processing the given character because it is part of a quote. Note that
   * this method must be called for all previous characters on the line in order for the result to
   * be correct.
   */
  boolean isSkipped(char ch) {
    if (lastSlash) {
      lastSlash = false;
      return true;
    }
    if (lastQuote != 0) {
      if (ch == lastQuote) {
        // end of quote sequence
        lastQuote = 0;
      } else if (ch == '\\') {
        // skip escaped character
        lastSlash = true;
      }
      return true;
    }
    if (ch == '\'' || ch == '"') {
      // start of quote sequence
      lastQuote = ch;
      return true;
    }
    return false;
  }
}
