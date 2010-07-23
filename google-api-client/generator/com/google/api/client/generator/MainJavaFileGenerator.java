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

import com.google.api.client.generator.model.Client;
import com.google.api.client.generator.model.Version;

import java.io.PrintWriter;

/**
 * @author Yaniv Inbar
 */
final class MainJavaFileGenerator extends AbstractJavaFileGenerator {

  private final Version version;

  MainJavaFileGenerator(Version version) {
    super(version.getPackageName(), version.client.className);
    this.version = version;
  }

  @Override
  public boolean isGenerated() {
    return isGenerated(version);
  }

  static boolean isGenerated(Version version) {
    Client client = version.client;
    return client.isOldGDataStyle || version.rootUrl != null
        || client.authTokenType != null || client.oauth != null;
  }

  @Override
  public void generate(PrintWriter out) {
    Client client = version.client;
    generateHeader(out);
    DocBuilder classDocBuilder = new DocBuilder();
    classDocBuilder.comment = "Constants for the " + client.name + ".";
    classDocBuilder.generate(out);
    out.println("public final class " + className + " {");
    out.println();
    if ("moderator".equals(client.id)) {
      DocBuilder docBuilder = new DocBuilder();
      docBuilder.container = classDocBuilder;
      docBuilder.indentNumSpaces = 2;
      docBuilder.removedMinor = 4;
      docBuilder.generate(out);
      out.println(indent(2) + "public static final String VERSION = \"1\";");
      out.println();
    } else if (client.isOldGDataStyle) {
      DocBuilder docBuilder = new DocBuilder();
      docBuilder.indentNumSpaces = 2;
      docBuilder.comment = "Version name.";
      docBuilder.generate(out);
      out.println(indent(2) + "public static final String VERSION = \""
          + version.id.substring(1) + "\";");
      out.println();
    }
    if (version.rootUrl != null) {
      DocBuilder docBuilder = new DocBuilder();
      docBuilder.indentNumSpaces = 2;
      docBuilder.comment = "Root URL.";
      docBuilder.generate(out);
      out.println(indent(2) + "public static final String ROOT_URL = \""
          + version.rootUrl + "\";");
      out.println();
    }
    if (client.authTokenType != null) {
      if ("moderator".equals(client.id)) {
        DocBuilder docBuilder = new DocBuilder();
        docBuilder.container = classDocBuilder;
        docBuilder.comment =
            "The authentication token type used for Client Login.";
        docBuilder.indentNumSpaces = 2;
        docBuilder.removedMinor = 4;
        docBuilder.generate(out);
      } else {
        DocBuilder docBuilder = new DocBuilder();
        docBuilder.indentNumSpaces = 2;
        docBuilder.comment =
            "The authentication token type used for Client Login.";
        docBuilder.generate(out);
      }
      out.println(indent(2) + "public static final String AUTH_TOKEN_TYPE = \""
          + client.authTokenType + "\";");
      out.println();
    }
    out.println(indent(2) + "private " + className + "() {");
    out.println(indent(2) + "}");
    out.println("}");
    out.close();
  }
}
