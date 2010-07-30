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

import com.google.api.client.generator.model.PackageModel;

import java.io.PrintWriter;
import java.util.SortedSet;

/**
 * @author Yaniv Inbar
 */
final class PomFileGenerator extends AbstractFileGenerator {

  private final SortedSet<PackageModel> pkgs;

  PomFileGenerator(SortedSet<PackageModel> pkgs) {
    this.pkgs = pkgs;
  }

  @Override
  public void generate(PrintWriter out) {
    out.println("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 "
        + "http://maven.apache.org/maven-v4_0_0.xsd\">");
    out.println("  <modelVersion>4.0.0</modelVersion>");
    out.println("  <parent>");
    out.println("    <groupId>com.google.api.client</groupId>");
    out.println("    <artifactId>google-api-client-parent</artifactId>");
    out.println("    <version>1.0.1-alpha</version>");
    out.println("    <relativePath>../parent/pom.xml</relativePath>");
    out.println("  </parent>");
    out.println("  <artifactId>google-api-client-modules-parent</artifactId>");
    out.println("  <packaging>pom</packaging>");
    out
        .println(
            "  <description>A place to hold common settings for each module.</description>");
    out.println("  <modules>");
    for (PackageModel pkg : pkgs) {
      out.println("    <module>" + pkg.artifactId + "</module>");

    }
    out.println("  </modules>");
    out.println("</project>");
    out.close();
  }

  @Override
  public String getOutputFilePath() {
    return "modules/pom.xml";
  }


}
