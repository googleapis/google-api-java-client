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

package com.google.api.client.generator.model;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yaniv Inbar
 */
public final class PackageModel implements Comparable<PackageModel> {

  public static final String VERSION = "1.1.2-alpha";
  public static final String VERSION_SNAPSHOT = VERSION + "-SNAPSHOT";

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile("\nimport ([a-zA-Z.]+);");

  public final String artifactId;
  public final String directoryPath;
  public final TreeSet<DependencyModel> dependencies =
      new TreeSet<DependencyModel>();

  private PackageModel(String artifactId) {
    this.artifactId = artifactId;
    this.directoryPath = "com/" + artifactId.replace('-', '/');
  }

  public int compareTo(PackageModel other) {
    return artifactId.compareTo(other.artifactId);
  }

  @Override
  public String toString() {
    return "PackageModel [artifactId=" + artifactId + ", directoryPath="
        + directoryPath + ", dependencies=" + dependencies + "]";
  }

  public static SortedSet<PackageModel> compute(File googleApiClientDirectory)
      throws IOException {
    SortedSet<PackageModel> pkgs = new TreeSet<PackageModel>();
    File srcDirectory = new File(googleApiClientDirectory, "src/com");
    int rootPathLength = srcDirectory.getCanonicalPath().length();
    addPackageModels(rootPathLength, srcDirectory, pkgs);
    return pkgs;
  }

  private static void addPackageModels(
      int rootPathLength, File dir, SortedSet<PackageModel> pkgs)
      throws IOException {
    PackageModel pkg = null;
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        addPackageModels(rootPathLength, file, pkgs);
      } else {
        if (file.getName().endsWith(".java")) {
          if (pkg == null) {
            pkg = new PackageModel(file
                .getParentFile()
                .getCanonicalPath()
                .substring(1 + rootPathLength)
                .replace('/', '-'));
            pkgs.add(pkg);
          }
          String content = readFile(file);
          Matcher matcher = IMPORT_PATTERN.matcher(content);
          while (matcher.find()) {
            String className = matcher.group(1);
            String packageName = getPackageName(className);
            if (className.startsWith("com.google.")) {
              DependencyModel dep = new DependencyModel();
              dep.groupId = "com.google.api.client";
              dep.artifactId = packageName.substring(4).replace('.', '-');
              dep.version = VERSION_SNAPSHOT;
              if (!pkg.artifactId.equals(dep.artifactId)) {
                pkg.dependencies.add(dep);
              }
            } else if (className.startsWith("android.")
                || className.startsWith("org.apache.")) {
              DependencyModel dep = new DependencyModel();
              dep.groupId = "com.google.android";
              dep.artifactId = "android";
              dep.scope = "provided";
              pkg.dependencies.add(dep);
            } else if (className.startsWith("org.codehaus.jackson.")) {
              DependencyModel dep = new DependencyModel();
              dep.groupId = "org.codehaus.jackson";
              dep.artifactId = "jackson-core-asl";
              pkg.dependencies.add(dep);
            } else if (className.startsWith("java.")
                || className.startsWith("javax.")) {
              // ignore
            } else {
              throw new IllegalArgumentException(
                  "unrecognized package: " + packageName);
            }
          }
        }
      }
    }
  }

  public static String readFile(File file) throws IOException {
    InputStreamContent content = new InputStreamContent();
    content.setFileInput(file);
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    content.writeTo(byteStream);
    return new String(byteStream.toByteArray());
  }

  /**
   * Returns the package name for the given Java class or package name, assuming
   * that class names always start with a capital letter and package names
   * always start with a lowercase letter.
   */
  public static String getPackageName(String classOrPackageName) {
    int lastDot = classOrPackageName.length();
    if (lastDot == 0) {
      return "";
    }
    while (true) {
      int nextDot = classOrPackageName.lastIndexOf('.', lastDot - 1);
      // check for error case of 2 dots in a row or starts/ends in dot
      Preconditions.checkArgument(nextDot + 1 < lastDot);
      if (Character.isLowerCase(classOrPackageName.charAt(nextDot + 1))) {
        // check for case that input string is already a package
        if (lastDot == classOrPackageName.length()) {
          return classOrPackageName;
        }
        return classOrPackageName.substring(0, lastDot);
      }
      if (nextDot == -1) {
        return "";
      }
      lastDot = nextDot;
    }
  }
}
