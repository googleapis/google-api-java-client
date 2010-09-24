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

import com.google.api.client.generator.linewrap.JavaLineWrapper;
import com.google.api.client.generator.linewrap.LineWrapper;

import java.io.PrintWriter;

/**
 * @author Yaniv Inbar
 */
abstract class AbstractJavaFileGenerator extends AbstractFileGenerator {

  final String packageName;
  final String className;

  AbstractJavaFileGenerator(String packageName, String className) {
    this.packageName = packageName;
    this.className = className;
  }

  @Override
  public final LineWrapper getLineWrapper() {
    return JavaLineWrapper.get();
  }

  @Override
  public final String getOutputFilePath() {
    return "src/" + packageName.replace('.', '/') + "/" + className + ".java";
  }

  void generateHeader(PrintWriter out) {
    out.println("/*");
    out.println(" * Copyright (c) 2010 Google Inc.");
    out.println(" *");
    out.println(" * Licensed under the Apache License, Version 2.0 (the \"License\"); you may not");
    out.println(" * use this file except in compliance with the License. You may obtain a copy of");
    out.println(" * the License at");
    out.println(" *");
    out.println(" * http://www.apache.org/licenses/LICENSE-2.0");
    out.println(" *");
    out.println(" * Unless required by applicable law or agreed to in writing, software");
    out.println(" * distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT");
    out.println(" * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the");
    out.println(" * License for the specific language governing permissions and limitations under");
    out.println(" * the License.");
    out.println(" */");
    out.println();
    out.println("package " + packageName + ";");
    out.println();
  }

  String useClass(Class<?> clazz) {
    String fullName = clazz.getName();
    int lastDot = fullName.lastIndexOf('.');
    return fullName.substring(lastDot + 1).replace('$', '.');
  }

  static String indent(int numSpaces) {
    return "                    ".substring(0, numSpaces);
  }
}
