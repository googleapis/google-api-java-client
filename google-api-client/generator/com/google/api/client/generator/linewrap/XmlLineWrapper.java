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
 * Line wrapper for XML files.
 * 
 * @author Yaniv Inbar
 */
public class XmlLineWrapper extends AbstractLineWrapper {

  /** Maximum XML line length. */
  private static final int MAX_LINE_LENGTH = 80;

  private static final XmlLineWrapper INSTANCE = new XmlLineWrapper();

  /** Returns the instance of the XML line wrapper. */
  public static LineWrapper get() {
    return INSTANCE;
  }

  /** Computes line wrapping result for the given XML text. */
  public static String wrap(String text) {
    return INSTANCE.compute(text);
  }

  @Override
  final int getCuttingPoint(ComputationState computationState, String line,
      StringBuilder prefix, boolean firstCut) {
    // if there's space, don't cut the line
    int maxWidth = MAX_LINE_LENGTH - prefix.length();
    if (line.length() <= maxWidth) {
      return line.length();
    }
    // indent on the first cut
    if (firstCut) {
      prefix.append("    ");
    }
    QuoteProcessor quoteProcessor = new QuoteProcessor();
    int spaceCutWithinMaxWidth = -1;
    int elementEndCutWithinMaxWidth = -1;
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (!quoteProcessor.isSkipped(ch)) {
        switch (ch) {
          case ' ':
            // if past max width or found a space preceding an element start
            if (i > maxWidth || i + 1 < line.length()
                && line.charAt(i + 1) == '<') {
              // then cut here
              return i;
            }
            // else find maximum space cut within max width
            spaceCutWithinMaxWidth = i;
            break;
          case '/':
            // falls through
          case '>':
            // element close?
            // if first occurence of element end (not at beginning of line)
            if (elementEndCutWithinMaxWidth == -1
                && i != 0
                && (ch == '>' || i + 1 != line.length()
                    && line.charAt(i + 1) == '>')) {
              // if past max width, cut here
              if (i > maxWidth) {
                return i;
              }
              // find last occurence within max width
              elementEndCutWithinMaxWidth = i;
              // can skip parsing the slash
              if (ch == '/') {
                i++;
              }
            }
            break;
        }
      }
      // check if over max space
      if (i >= maxWidth) {
        if (spaceCutWithinMaxWidth != -1) {
          return spaceCutWithinMaxWidth;
        }
        if (elementEndCutWithinMaxWidth != -1) {
          return elementEndCutWithinMaxWidth;
        }
      }
    }
    return line.length();
  }

  XmlLineWrapper() {
  }
}
