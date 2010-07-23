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
 * Line wrapper for HTML files.
 * 
 * @author Yaniv Inbar
 */
public final class HtmlLineWrapper extends XmlLineWrapper {

  private static final HtmlLineWrapper INSTANCE = new HtmlLineWrapper();

  /** Returns the instance of the HTML line wrapper. */
  public static LineWrapper get() {
    return INSTANCE;
  }

  /** Computes line wrapping result for the given HTML text. */
  public static String wrap(String text) {
    return INSTANCE.compute(text);
  }

  private HtmlLineWrapper() {
  }
}
