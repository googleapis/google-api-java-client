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
 * Abstract super class for all language-specific line wrappers.
 *
 * @author Yaniv Inbar
 */
abstract class AbstractLineWrapper implements LineWrapper {

  private static final ThreadLocal<StringBuilder> LOCAL_OUT_BUFFER = new ThreadLocalStringBuilder();

  private static final ThreadLocal<StringBuilder> LOCAL_PREFIX_BUFFER =
      new ThreadLocalStringBuilder();


  /**
   * Computation state interface used for storing arbitrary data during the line wrapping
   * computation that can be shared across all lines.
   */
  interface ComputationState {
  }

  /** Thread-local string builder instance. */
  static class ThreadLocalStringBuilder extends ThreadLocal<StringBuilder> {

    @Override
    protected StringBuilder initialValue() {
      return new StringBuilder();
    }
  }

  public final String compute(String text) {
    // parse into separate lines
    ComputationState computationState = computationState();
    int nextNewLine = 0;
    int lineSeparatorLength = Strings.LINE_SEPARATOR.length();
    int textLength = text.length();
    int copyFromTextIndex = 0;
    StringBuilder prefix = LOCAL_PREFIX_BUFFER.get();
    StringBuilder outBuffer = LOCAL_OUT_BUFFER.get();
    outBuffer.setLength(0);
    for (int curIndex = 0; nextNewLine != -1 && curIndex < textLength; curIndex = nextNewLine + 1) {
      // find next line separator which we know ends in '\n'
      nextNewLine = text.indexOf('\n', curIndex);
      int highIndex = nextNewLine == -1 ? textLength - 1 : nextNewLine - lineSeparatorLength;
      // remove whitespace from end of line
      int lastNonSpace = Strings.lastIndexOfNonSpace(text, highIndex, curIndex);
      if (lastNonSpace == -1) {
        // empty line
        if (nextNewLine == -1) {
          // end of text but missing new line
          outBuffer.append(text, copyFromTextIndex, curIndex);
          outBuffer.append(Strings.LINE_SEPARATOR);
          copyFromTextIndex = textLength;
        } else if (nextNewLine != curIndex) {
          // line may up of >= 1 space
          outBuffer.append(text, copyFromTextIndex, curIndex);
          copyFromTextIndex = highIndex + 1;
        }
        continue;
      }
      // remove whitespace from beginning of line, remembering it as a "prefix"
      int firstNonSpace = Strings.indexOfNonSpace(text, curIndex, lastNonSpace);
      prefix.setLength(0);
      prefix.append(text, curIndex, firstNonSpace);
      // iterate over each line "cut"
      boolean firstCut = true;
      while (true) {
        // run the language-specific line-wrapping algorithm
        int originalPrefixLength = prefix.length();
        String line = text.substring(firstNonSpace, lastNonSpace + 1);
        int cut = getCuttingPoint(computationState, line, prefix, firstCut);
        // remove spaces from end of cut
        cut = Strings.lastIndexOfNonSpace(line, cut - 1) + 1;
        // don't want infinite recursion!
        if (cut == 0) {
          throw new IllegalStateException(
              "illegal cutting point:" + "\nline (" + line.length() + "):" + line + "\nprefix ("
                  + originalPrefixLength + "): " + prefix.substring(0, originalPrefixLength)
                  + "\nfirstCut: " + firstCut);
        }
        if (cut == line.length() && nextNewLine != -1 && lastNonSpace == highIndex) {
          // preserve to the end of line
          if (!firstCut) {
            copyFromTextIndex = firstNonSpace;
          }
        } else {
          // make a cut
          if (firstCut) {
            outBuffer.append(text, copyFromTextIndex, firstNonSpace + cut);
            copyFromTextIndex = nextNewLine == -1 ? textLength : nextNewLine + 1;
          } else {
            outBuffer.append(text, firstNonSpace, firstNonSpace + cut);
          }
          // insert a line separator
          outBuffer.append(Strings.LINE_SEPARATOR);
        }
        // find next non-space character to start next line
        int next = Strings.indexOfNonSpace(line, cut);
        if (next == -1) {
          break;
        }
        // insert prefix for next line
        outBuffer.append(prefix);
        firstNonSpace += next;
        firstCut = false;
      }
    }
    // optimize for case where original text is unchanged
    if (copyFromTextIndex == 0) {
      return text;
    }
    return outBuffer.append(text, copyFromTextIndex, textLength).toString();
  }

  /**
   * Instantiates a new computation state. By default just returns an empty object, but may be
   * overriden by subclasses.
   */
  ComputationState computationState() {
    return new ComputationState() {};
  }

  /**
   * Returns an index in the given string where the line should be broken. If no cut is desired or
   * no cut can be found, it should just return the line length. Also, this method is responsible
   * for updating the current line's prefix, i.e. any additional indent to use after any subsequent
   * cuts.
   * <p/>
   * The default implementation cuts on a space if the line length is greater than maximum width but
   * subclasses may override.
   *
   * @param computationState computation state
   * @param line current line content to be cut
   * @param prefix current line prefix to be updated if necessary
   * @param firstCut whether this is the first cut in the original input line
   * @return index of the character at which at cut {@code >= 1} and {@code <= line.length()}
   */
  abstract int getCuttingPoint(
      ComputationState computationState, String line, StringBuilder prefix, boolean firstCut);

  /**
   * Returns a cut on a space or {@code -1} if no space found. It will try to return an index that
   * is {@code <= maxWidth}.
   */
  static int getCutOnSpace(String line, int maxWidth) {
    int max = Math.min(line.length() - 1, maxWidth);
    int index = line.lastIndexOf(' ', max);
    if (index == -1) {
      index = line.indexOf(' ', max + 1);
    }
    return index;
  }
}
