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
 * Line wrapper for Java files.
 * 
 * @author Yaniv Inbar
 */
public final class JavaLineWrapper extends AbstractLineWrapper {

  /** Maximum Java line length. */
  static final int MAX_LINE_LENGTH = 80;

  private static final JavaLineWrapper INSTANCE = new JavaLineWrapper();

  /** Java line wrapper computation state. */
  static class JavaComputationState implements ComputationState {

    private final JavaLikeCommentProcessor multiLineComment;

    private final SingleLineCommentProcessor singleLineComment;

    JavaComputationState(boolean useJavaMultiLineCommentProcessor,
        String singleLineCommentPrefix) {
      multiLineComment =
          useJavaMultiLineCommentProcessor ? new JavaLikeCommentProcessor()
              : null;
      singleLineComment =
          new SingleLineCommentProcessor(singleLineCommentPrefix);
    }

    /**
     * Returns whether to cut on the line for a space character. Default
     * implementation returns {@code true}, but subclasses may override.
     */
    boolean isSpaceCut(String line, int index) {
      return true;
    }
  }

  /** Returns the instance of the Java line wrapper. */
  public static LineWrapper get() {
    return INSTANCE;
  }

  /** Computes line wrapping result for the given Java text. */
  public static String wrap(String text) {
    return get().compute(text);
  }

  /**
   * {@inheritDoc}
   * 
   * @return by default returns {@code new JavaComputationState(true, "//")},
   *         but subclasses may override.
   */
  @Override
  ComputationState computationState() {
    return new JavaComputationState(true, "//");
  }

  @Override
  int getCuttingPoint(ComputationState computationState, String line,
      StringBuilder prefix, boolean firstCut) {
    // don't cut an import
    if (firstCut && line.startsWith("import ")) {
      return line.length();
    }

    return getCuttingPointImpl(computationState, line, prefix, firstCut,
        MAX_LINE_LENGTH, MAX_LINE_LENGTH);
  }

  /** Cutting algorithm for Java. */
  static int getCuttingPointImpl(ComputationState computationState,
      String line, StringBuilder prefix, boolean firstCut) {
    return getCuttingPointImpl(computationState, line, prefix, firstCut,
        MAX_LINE_LENGTH, MAX_LINE_LENGTH);
  }

  /** Cutting algorithm for Java, with caller specified line lengths. */
  static int getCuttingPointImpl(ComputationState computationState,
      String line, StringBuilder prefix, boolean firstCut, int maxLineLength,
      int maxDocLineLength) {
    // process for a multi-line comment
    JavaComputationState javaCompState =
        (JavaComputationState) computationState;
    JavaLikeCommentProcessor multiLineComment = javaCompState.multiLineComment;
    if (multiLineComment != null) {
      multiLineComment.processLine(line);
    }
    // process for single line comment
    int originalPrefixLength = prefix.length();
    if ((multiLineComment == null || multiLineComment.getCommentStart() == null)
        && !javaCompState.singleLineComment.start(line, prefix, firstCut)) {
      // if there's space, don't cut the line
      int maxWidth = maxLineLength - originalPrefixLength;
      if (line.length() <= maxWidth) {
        return line.length();
      }
      int spaceCutWithinMaxWidth = -1;
      int commaSpaceCutWithinMaxWidth = -1;
      int openParenCutWithinMaxWidth = -1;
      int result = line.length();
      QuoteProcessor quoteProcessor = new QuoteProcessor();
      for (int i = 0; i < result; i++) {
        char ch = line.charAt(i);
        // process for quotes
        if (!quoteProcessor.isSkipped(ch)) {
          // process for a single-line comment
          if (i >= 1 && javaCompState.singleLineComment.isCuttingPoint(line, i)) {
            return i;
          }
          switch (ch) {
            case ',':
              if (i + 1 < line.length() && line.charAt(i + 1) == ' ') {
                if (i + 1 <= maxWidth) {
                  commaSpaceCutWithinMaxWidth = i + 1;
                } else {
                  result = i + 1;
                }
              }
              break;
            case ' ':
              if (javaCompState.isSpaceCut(line, i)) {
                if (i <= maxWidth) {
                  spaceCutWithinMaxWidth = i;
                } else {
                  result = i;
                }
              }
              break;
            case '[':
              if (i + 1 <= maxWidth) {
                openParenCutWithinMaxWidth = i + 1;
              } else {
                result = i + 1;
              }
              break;
            case '(':
              int nextChIndex = Strings.indexOfNonSpace(line, i + 1);
              if (nextChIndex != -1) {
                if (line.charAt(nextChIndex) != ')') {
                  if (i + 1 <= maxWidth) {
                    openParenCutWithinMaxWidth = i + 1;
                  } else {
                    result = i + 1;
                  }
                }
                i = nextChIndex - 1;
              }
              break;
          }
        }
        // check if over max space
        if (i >= maxWidth) {
          if (commaSpaceCutWithinMaxWidth != -1) {
            result = commaSpaceCutWithinMaxWidth;
          } else if (spaceCutWithinMaxWidth != -1) {
            result = spaceCutWithinMaxWidth;
          } else if (openParenCutWithinMaxWidth != -1) {
            result = openParenCutWithinMaxWidth;
          }
        }
      }
      // indent on the first cut
      if (firstCut) {
        prefix.append("    ");
      }
      return result;
    }
    // is it a JavaDoc comment start?
    boolean isJavaDoc =
        multiLineComment != null
            && "/**".equals(multiLineComment.getCommentStart());

    int localLength = isJavaDoc ? maxDocLineLength : maxLineLength;

    // if there's space, don't cut the line
    int maxWidth = localLength - originalPrefixLength;
    if (line.length() <= maxWidth) {
      return line.length();
    }

    // in a comment, so use default algorithm
    int index = getCutOnSpace(line, maxWidth);
    if (index != -1) {
      // update the prefix so next lines are properly indented
      if (firstCut && multiLineComment != null
          && multiLineComment.getCommentStart() != null) {
        if (line.startsWith("/*")) {
          prefix.append(' ');
        }
        prefix.append("* ");
        if (isJavaDoc && javaDocAtParamPattern(line)) {
          prefix.append("    ");
        }
      }
      return index;
    }
    return line.length();
  }

  /** Returns whether it finds the {@code "@param"} in the JavaDoc. */
  private static boolean javaDocAtParamPattern(String line) {
    int index = 0;
    if (line.startsWith("/*")) {
      index += 2;
    }
    if (line.charAt(index) != '*') {
      return false;
    }
    index = Strings.indexOfNonSpace(line, index + 1);
    return index != -1
        && (line.startsWith("@param ", index) || line.startsWith("@return ",
            index));
  }

  private JavaLineWrapper() {
  }
}
