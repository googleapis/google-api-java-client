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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Yaniv Inbar
 */
public class GeneratePom {

  public static void main(String[] args) throws IOException {
    File googleApiClientDirectory = Generation.getDirectory(args[0]);
    // package names
    SortedSet<PackageModel> pkgs =
        PackageModel.compute(googleApiClientDirectory);
    // compute file generators
    List<AbstractFileGenerator> fileGenerators =
        new ArrayList<AbstractFileGenerator>();
    fileGenerators.add(new PomFileGenerator(pkgs));
    for (PackageModel packageName : pkgs) {
      fileGenerators.add(new ModulePomFileGenerator(packageName));
    }
    Generation.compute(fileGenerators, googleApiClientDirectory);
  }

  private GeneratePom() {
  }

}
