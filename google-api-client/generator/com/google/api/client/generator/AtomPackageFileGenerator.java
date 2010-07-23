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

import com.google.api.client.generator.model.Version;

import java.io.PrintWriter;

/**
 * @author Yaniv Inbar
 */
final class AtomPackageFileGenerator extends AbstractHtmlFileGenerator {

  private final Version version;

  AtomPackageFileGenerator(Version version) {
    this.version = version;
  }

  @Override
  public void generate(PrintWriter out) {
    out.println("<body>");
    out.println("Small optional Java library for the "
        + version.client.getXmlFormatName()
        + " format for "
        + version.client.name
        + " "
        + (version.client.isOldGDataStyle ? "version "
            + version.id.substring(1) : version.id) + ".");
    out.println("");
    out.println("<p>This package depends on the "
        + "{@link com.google.api.client.xml} package.</p>");
    out.println("");
    out.println("<p><b>Warning: this package is experimental, and its content "
        + "may be changed in incompatible ways or possibly entirely removed "
        + "in a future version of the library</b></p>");
    out.println("");
    out.println("@since 1.0");
    out.println("</body>");
    out.close();
  }

  @Override
  public String getOutputFilePath() {
    return "src/" + version.getPathRelativeToSrc() + "/"
        + version.client.getXmlFormatId() + "/package.html";
  }

  @Override
  public boolean isGenerated() {
    return version.atom != null;
  }
}
