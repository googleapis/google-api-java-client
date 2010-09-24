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
 * Processes multi-line comments like the ones in Java, i.e. those that start with {@code "/*"}. The
 * current implementation of this processor is that {@code "/*"} must be at the beginning of the
 * line, and {@code "*\/"} must be at the end of the line.
 *
 * @author Yaniv Inbar
 */
final class JavaLikeCommentProcessor {

  /**
   * Multi-line comment start prefix or {@code null} if not inside a multi-line comment.
   */
  private String commentStart = null;

  /**
   * Current line's multi-line comment start prefix or {@code null} for none.
   */
  private String curCommentStart = null;

  /**
   * Processes the current line for a multi-line comment. Must be called regardless of whether the
   * line is going to be cut.
   */
  void processLine(String line) {
    /*
     * TODO: support case where multi-line comments don't start at beginning of line or end at end
     * of line
     */
    // check if we're in a multi-line comment
    if (commentStart == null && line.startsWith("/*")) {
      if (line.length() >= 3 && line.charAt(2) == '*') {
        // Javadoc comment
        commentStart = "/**";
      } else {
        // non-Javadoc comment
        commentStart = "/*";
      }
    }
    // cur line's comment
    curCommentStart = commentStart;
    // check for end of comment
    if (commentStart != null && line.endsWith("*/")) {
      commentStart = null;
    }
  }

  /**
   * Returns the current line's multi-line comment start prefix or {@code null} for none.
   */
  String getCommentStart() {
    return curCommentStart;
  }
}
