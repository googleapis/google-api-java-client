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

package com.google.api.client.generator.linewrap;

/**
 * Processes any single-line comments in the line.
 * 
 * @author Yaniv Inbar
 */
final class SingleLineCommentProcessor {

  private final String commentPrefix;

  private boolean inComment;

  /**
   * @param commentPrefix prefix to recognize the start of a single-line
   *        comment, e.g. {@code "//"} in Java.
   */
  SingleLineCommentProcessor(String commentPrefix) {
    this.commentPrefix = commentPrefix;
  }

  /**
   * Process the given line to check if it starts with a single-line comment.
   * 
   * @param line current line content to be cut
   * @param prefix current line prefix to be updated if necessary
   * @param firstCut whether this is the first cut in the original input line
   * @return whether the line starts with the single-line comment prefix
   */
  boolean start(String line, StringBuilder prefix, boolean firstCut) {
    if (firstCut) {
      inComment = false;
    }
    if (!inComment && isCuttingPoint(line, 0)) {
      inComment = true;
      prefix.append(commentPrefix).append(' ');
    }
    return inComment;
  }

  /**
   * Returns wehther the given index is the location of the single-line comment
   * prefix.
   * 
   * @param line current line content to be cut
   */
  boolean isCuttingPoint(String line, int index) {
    return line.startsWith(commentPrefix, index);
  }
}
