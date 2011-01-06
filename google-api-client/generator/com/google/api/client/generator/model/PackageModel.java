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

package com.google.api.client.generator.model;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaniv Inbar
 */
public final class PackageModel implements Comparable<PackageModel> {

  public static final String VERSION_SNAPSHOT = Strings.VERSION + "-SNAPSHOT";

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("^import (static )?([a-z]\\w*(\\.[a-z]\\w*)*)");

  public final String artifactId;
  public final String directoryPath;
  public final TreeSet<DependencyModel> dependencies = new TreeSet<DependencyModel>();

  private PackageModel(String artifactId) {
    this.artifactId = artifactId;
    directoryPath = "com/" + artifactId.replace('-', '/');
  }

  public int compareTo(PackageModel other) {
    return artifactId.compareTo(other.artifactId);
  }

  @Override
  public String toString() {
    return "PackageModel [artifactId=" + artifactId + ", directoryPath=" + directoryPath
        + ", dependencies=" + dependencies + "]";
  }

  public static SortedSet<PackageModel> compute(File googleApiClientDirectory) throws IOException {
    SortedSet<PackageModel> pkgs = new TreeSet<PackageModel>();
    File srcDirectory = new File(googleApiClientDirectory, "src/com");
    int rootPathLength = srcDirectory.getCanonicalPath().length();
    addPackageModels(rootPathLength, srcDirectory, pkgs);
    return pkgs;
  }

  private static void addPackageModels(int rootPathLength, File dir, SortedSet<PackageModel> pkgs)
      throws IOException {
    PackageModel pkg = null;
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        addPackageModels(rootPathLength, file, pkgs);
      } else {
        if (file.getName().endsWith(".java")) {
          if (pkg == null) {
            pkg =
                new PackageModel(file
                    .getParentFile()
                    .getCanonicalPath()
                    .substring(1 + rootPathLength)
                    .replace('/', '-'));
            pkgs.add(pkg);
          }
          // hack: jackson is used without an import declaration because otherwise it would have
          // created a compilation error due to classes having identical names between Jackson and
          // this library
          if (file.getParentFile().getName().equals("jackson")) {
            foundDependency(pkg, "org.codehaus.jackson");
          }
          BufferedReader reader = new BufferedReader(new FileReader(file));
          String line = null;
          while ((line = reader.readLine()) != null) {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            while (matcher.find()) {
              foundDependency(pkg, matcher.group(2));
            }
          }
        }
      }
    }
  }

  private static void foundDependency(PackageModel pkg, String packageName) {
    if (isOrParentPackageOf(packageName, "com.google")) {
      if (isOrParentPackageOf(packageName, "com.google.api.client")) {
        DependencyModel dep = new DependencyModel();
        dep.groupId = "com.google.api.client";
        dep.artifactId = packageName.substring("com.".length()).replace('.', '-');
        dep.version = VERSION_SNAPSHOT;
        if (!pkg.artifactId.equals(dep.artifactId)) {
          pkg.dependencies.add(dep);
        }
      } else if (isOrParentPackageOf(packageName, "com.google.appengine")) {
        DependencyModel dep = new DependencyModel();
        dep.groupId = "com.google.appengine";
        dep.artifactId = "appengine-api-1.0-sdk";
        dep.scope = "provided";
        pkg.dependencies.add(dep);
      } else if (isOrParentPackageOf(packageName, "com.google.gson")) {
        DependencyModel dep = new DependencyModel();
        dep.groupId = "com.google.code.gson";
        dep.artifactId = "gson";
        pkg.dependencies.add(dep);
      } else {
        Preconditions.checkArgument(false, "unrecognized Google package: %s", packageName);
      }
    } else if (isOrParentPackageOf(packageName, "android")) {
      DependencyModel dep = new DependencyModel();
      dep.groupId = "com.google.android";
      dep.artifactId = "android";
      pkg.dependencies.add(dep);
    } else if (isOrParentPackageOf(packageName, "org.apache")) {
      DependencyModel dep = new DependencyModel();
      dep.groupId = "org.apache.httpcomponents";
      dep.artifactId = "httpclient";
      pkg.dependencies.add(dep);
    } else if (isOrParentPackageOf(packageName, "org.xmlpull.v1")) {
      DependencyModel dep = new DependencyModel();
      dep.groupId = dep.artifactId = "xpp3";
      pkg.dependencies.add(dep);
    } else if (isOrParentPackageOf(packageName, "org.codehaus.jackson")) {
      DependencyModel dep = new DependencyModel();
      dep.groupId = "org.codehaus.jackson";
      dep.artifactId = "jackson-core-asl";
      pkg.dependencies.add(dep);
    } else if (isOrParentPackageOf(packageName, "java")
        || isOrParentPackageOf(packageName, "javax")) {
      // ignore
    } else {
      throw new IllegalArgumentException("unrecognized package: " + packageName);
    }
  }

  private static boolean isOrParentPackageOf(String packageName, String pkg) {
    return packageName.equals(pkg) || packageName.startsWith(pkg + ".");
  }
}
