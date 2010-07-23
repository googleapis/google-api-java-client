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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Yaniv Inbar
 */
final class PomFileGenerator extends AbstractFileGenerator {

  private final SortedSet<String> packageNames = new TreeSet<String>();

  private final int rootPathLength;

  PomFileGenerator(File srcDirectory) throws IOException {
    srcDirectory = new File(srcDirectory, "com/google/api");
    rootPathLength = srcDirectory.getCanonicalPath().length();
    addPackageNames(srcDirectory);
  }

  void addPackageNames(File dir) throws IOException {
    for (File file : dir.listFiles()) {
      if (".svn".equals(file.getName())) {
        continue;
      }
      if (file.isDirectory()) {
        addPackageNames(file);
      } else {
        if (file.getName().endsWith(".java")) {
          packageNames.add(file.getParentFile().getCanonicalPath().substring(
              1 + rootPathLength));
        }
      }
    }
  }

  @Override
  public void generate(PrintWriter out) {

    out.println("      <plugin>");
    out.println("        <artifactId>maven-jar-plugin</artifactId>");
    out.println("        <version>2.3.1</version>");
    out.println("        <executions>");
    for (String packageName : packageNames) {
      out.println("         <execution>");
      out.println("          <id>" + packageName.replace('/', '-') + "</id>");
      out.println("            <goals>");
      out.println("             <goal>jar</goal>");
      out.println("            </goals>");
      out.println("            <configuration>");
      out.println("              <includes>");
      out.println("                <include>com/google/api/" + packageName
          + "/*</include>");
      out.println("              </includes>");
      out.println(
          "              <outputDirectory>target/packages</outputDirectory>");
      out.println(
          "              <finalName>google-api-" + packageName.replace('/', '-')
              + "-${project.version}</finalName>");
      out.println("            </configuration>");
      out.println("         </execution>");
    }
    out.println("        </executions>");
    out.println("      </plugin>");
    out.close();
  }

  @Override
  public String getOutputFilePath() {
    return "partial-pom.xml";
  }
}
