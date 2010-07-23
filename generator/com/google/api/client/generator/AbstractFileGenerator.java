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

package com.google.api.client.generator;

import com.google.api.client.generator.linewrap.LineWrapper;

import java.io.PrintWriter;

/**
 * Defines a single file generator, which manages a single generated file.
 * 
 * @author Yaniv Inbar
 */
abstract class AbstractFileGenerator {

  /** Whether to generate this file. Default is to return {@code true}. */
  boolean isGenerated() {
    return true;
  }

  /** Generates the content of the file into the given print writer. */
  abstract void generate(PrintWriter out);

  /**
   * Returns the output file path relative to the root output directory.
   */
  abstract String getOutputFilePath();

  /**
   * Returns the line wrapper to use or {@code null} for none. Default is to
   * return {@code null}.
   */
  LineWrapper getLineWrapper() {
    return null;
  }
}
