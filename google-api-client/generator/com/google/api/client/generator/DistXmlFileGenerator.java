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

package com.google.api.client.generator;

import com.google.api.client.generator.model.PackageModel;
import com.google.api.client.util.Strings;

import java.io.PrintWriter;
import java.util.SortedSet;

/**
 * @author Yaniv Inbar
 */
final class DistXmlFileGenerator extends AbstractFileGenerator {

  private final SortedSet<PackageModel> pkgs;

  DistXmlFileGenerator(SortedSet<PackageModel> pkgs) {
    this.pkgs = pkgs;
  }

  @Override
  public void generate(PrintWriter out) {
    out.println("<assembly xmlns=\"http://maven.apache.org/plugins/"
        + "maven-assembly-plugin/assembly/1.1.0\" "
        + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
        + "xsi:schemaLocation=\"http://maven.apache.org/plugins/"
        + "maven-assembly-plugin/assembly/1.1.0 "
        + "http://maven.apache.org/xsd/assembly-1.1.0.xsd\">");
    out.println("  <id>java</id>");
    out.println("  <formats>");
    out.println("    <format>zip</format>");
    out.println("  </formats>");
    out.println("  <files>");
    out.println("    <file>");
    out.println("      <source>target/${project.artifactId}-${project.version}.jar</source>");
    out.println("    </file>");
    out.println("    <file>");
    out.println(
        "      <source>target/${project.artifactId}-${project.version}-sources.jar</source>");
    out.println("    </file>");
    out.println("    <file>");
    out.println(
        "      <source>target/${project.artifactId}-${project.version}-javadoc.jar</source>");
    out.println("    </file>");
    out.println("    <file>");
    out.println("      <source>assemble/LICENSE</source>");
    out.println("    </file>");
    out.println("    <file>");
    out.println("      <source>assemble/packages/readme.html</source>");
    out.println("      <outputDirectory>packages</outputDirectory>");
    out.println("      <filtered>true</filtered>");
    out.println("    </file>");
    out.println("    <file>");
    out.println("      <source>assemble/readme.html</source>");
    out.println("      <filtered>true</filtered>");
    out.println("    </file>");
    out.println("    <file>");
    out.println("      <source>assemble/dependencies/readme.html</source>");
    out.println("      <outputDirectory>dependencies</outputDirectory>");
    out.println("      <filtered>true</filtered>");
    out.println("    </file>");
    for (PackageModel pkg : pkgs) {
      out.println("    <file>");
      out.println("      <source>modules/" + pkg.artifactId + "/target/" + pkg.artifactId + "-"
          + Strings.VERSION + ".jar</source>");
      out.println("      <outputDirectory>packages</outputDirectory>");
      out.println("    </file>");
    }
    out.println("  </files>");
    out.println("  <fileSets>");
    out.println("    <fileSet>");
    out.println("      <directory>assemble/dependencies/java</directory>");
    out.println("      <outputDirectory>dependencies/java</outputDirectory>");
    out.println("    </fileSet>");
    out.println("  </fileSets>");
    out.println("  <dependencySets>");
    out.println("    <dependencySet>");
    out.println("      <outputDirectory>dependencies</outputDirectory>");
    out.println("      <useProjectArtifact>false</useProjectArtifact>");
    out.println("    </dependencySet>");
    out.println("  </dependencySets>");
    out.println("</assembly>");
    out.close();
  }

  @Override
  public String getOutputFilePath() {
    return "assemble/dist.xml";
  }
}
