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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Yaniv Inbar
 */
public class GeneratePom {

  public static void main(String[] args) throws IOException {
    File googleApiClientDirectory = Generation.getDirectory(args[0]);
    // package names
    SortedSet<String> packageNames =
        computePackageNames(googleApiClientDirectory);
    // compute file generators
    List<AbstractFileGenerator> fileGenerators =
        new ArrayList<AbstractFileGenerator>();
    fileGenerators.add(new PomFileGenerator(packageNames));
    for (String packageName : packageNames) {
      fileGenerators.add(new ModulePomFileGenerator(packageName));
    }
    Generation.compute(fileGenerators, googleApiClientDirectory);
  }

  private GeneratePom() {
  }

  private static SortedSet<String> computePackageNames(
      File googleApiClientDirectory) throws IOException {
    SortedSet<String> packageNames = new TreeSet<String>();
    File srcDirectory = new File(googleApiClientDirectory, "src/com");
    int rootPathLength = srcDirectory.getCanonicalPath().length();
    addPackageNames(rootPathLength, srcDirectory, packageNames);
    return packageNames;
  }

  private static void addPackageNames(
      int rootPathLength, File dir, SortedSet<String> packageNames)
      throws IOException {
    for (File file : dir.listFiles()) {
      if (".svn".equals(file.getName())) {
        continue;
      }
      if (file.isDirectory()) {
        addPackageNames(rootPathLength, file, packageNames);
      } else {
        if (file.getName().endsWith(".java")) {
          packageNames.add(file.getParentFile().getCanonicalPath().substring(
              1 + rootPathLength).replace('/', '-'));
        }
      }
    }
  }
}
