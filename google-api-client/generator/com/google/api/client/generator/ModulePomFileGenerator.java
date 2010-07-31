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

import com.google.api.client.generator.model.DependencyModel;
import com.google.api.client.generator.model.PackageModel;

import java.io.PrintWriter;

/**
 * @author Yaniv Inbar
 */
final class ModulePomFileGenerator extends AbstractFileGenerator {

  private final PackageModel pkg;

  ModulePomFileGenerator(PackageModel pkg) {
    this.pkg = pkg;
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
    out.println(
        "    <artifactId>google-api-client-modules-parent</artifactId>");
    out.println("    <version>" + PackageModel.VERSION + "</version>");
    out.println("  </parent>");
    out.println("  <artifactId>" + pkg.artifactId + "</artifactId>");
    out.println("  <build>");
    out.println("    <plugins>");
    out.println("      <plugin>");
    out.println("        <artifactId>maven-source-plugin</artifactId>");
    out.println("        <version>2.0.4</version>");
    out.println("        <configuration>");
    out.println("          <excludeResources>true</excludeResources>");
    out.println("        </configuration>");
    out.println("      </plugin>");
    out.println("    </plugins>");
    out.println("    <resources>");
    out.println("      <resource>");
    out.println("        <filtering>false</filtering>");
    out.println("        <directory>../../target/classes</directory>");
    out.println("        <includes>");
    out.println("          <include>" + pkg.directoryPath + "/*</include>");
    out.println("        </includes>");
    out.println("      </resource>");
    out.println("    </resources>");
    out.println("  </build>");
    out.println("  <dependencies>");
    for (DependencyModel dep : pkg.dependencies) {
      out.println("    <dependency>");
      out.println("      <groupId>" + dep.groupId + "</groupId>");
      out.println("      <artifactId>" + dep.artifactId + "</artifactId>");
      if (dep.version != null) {
        out.println("      <version>" + dep.version + "</version>");
      }
      if (dep.scope != null) {
        out.println("      <scope>" + dep.scope + "</scope>");
      }
      out.println("    </dependency>");
    }
    out.println("  </dependencies>");
    out.println("</project>");
    out.close();
  }

  @Override
  public String getOutputFilePath() {
    return "modules/" + pkg.artifactId + "/pom.xml";
  }
}
