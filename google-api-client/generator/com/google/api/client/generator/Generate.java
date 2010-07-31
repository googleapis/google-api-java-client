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
import com.google.api.client.json.CustomizeJsonParser;
import com.google.api.client.json.Json;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Yaniv Inbar
 */
public class Generate {

  public static void main(String[] args) throws IOException {
    System.out.println(
        "Google API Java Client Library Generator");
    if (args.length < 2) {
      System.err
          .println(
              "Expected arguments: dataDirectory google-api-clientProjectDirectory");
      System.exit(1);
    }
    SortedSet<Client> clients = readClients(args[0]);
    // display clients
    System.out.println();
    System.out.println(clients.size() + " API description(s):");
    for (Client client : clients) {
      System.out.print(client.name + " (" + client.id + ")");
      if (client.versions.size() == 1) {
        System.out.print(" version ");
      } else {
        System.out.print(" versions ");
      }
      boolean first = true;
      for (Version version : client.versions.values()) {
        if (first) {
          first = false;
        } else {
          System.out.print(", ");
        }
        System.out.print(version.id);
      }
      System.out.println();
    }
    // compute file generators
    List<AbstractFileGenerator> fileGenerators =
        new ArrayList<AbstractFileGenerator>();
    for (Client client : clients) {
      for (Version version : client.versions.values()) {
        fileGenerators.add(new MainJavaFileGenerator(version));
        fileGenerators.add(new MainPackageFileGenerator(version));
        fileGenerators.add(new AtomPackageFileGenerator(version));
        fileGenerators.add(new AtomJavaFileGenerator(version));
      }
    }
    Generation.compute(fileGenerators, Generation.getDirectory(args[1]));
  }

  private Generate() {
  }

  static final class Custom extends CustomizeJsonParser {

    @Override
    public void handleUnrecognizedKey(Object context, String key) {
      throw new IllegalArgumentException("unrecognized key: " + key);
    }
  }


  private static SortedSet<Client> readClients(String dataDirectoryPath)
      throws IOException {
    File dataDirectory = Generation.getDirectory(dataDirectoryPath);
    SortedSet<Client> result = new TreeSet<Client>();
    JsonFactory factory = new JsonFactory();
    for (File file : dataDirectory.listFiles()) {
      if (!file.getName().endsWith(".json")) {
        continue;
      }
      Client client;
      try {
        JsonParser parser = factory.createJsonParser(file);
        parser.nextToken();
        client = Json.parseAndClose(parser, Client.class, new Custom());
      } catch (RuntimeException e) {
        throw new RuntimeException(
            "problem parsing " + file.getCanonicalPath(), e);
      }
      client.validate();
      result.add(client);
    }
    return result;
  }
}
