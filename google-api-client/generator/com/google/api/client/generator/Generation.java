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

import com.google.api.client.generator.linewrap.LineWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Yaniv Inbar
 */
public class Generation {

  static void compute(List<AbstractFileGenerator> fileGenerators, File outputDirectory)
      throws IOException {
    int size = 0;
    List<FileComputer> fileComputers = new ArrayList<FileComputer>();
    System.out.println();
    System.out.println("Computing " + fileGenerators.size() + " file(s):");
    Set<String> outputFilePaths = new HashSet<String>();
    for (AbstractFileGenerator fileGenerator : fileGenerators) {
      FileComputer fileComputer = new FileComputer(fileGenerator, outputDirectory);
      if (!outputFilePaths.add(fileComputer.outputFilePath)) {
        System.err.println("Error: duplicate output file path: " + fileComputer.outputFilePath);
        System.exit(1);
      }
      fileComputers.add(fileComputer);
      fileComputer.compute();
      System.out.print('.');
      if (fileComputer.status != FileStatus.UNCHANGED) {
        size++;
      }
    }
    System.out.println();
    System.out.println();
    System.out.println("Output root directory: " + outputDirectory);
    System.out.println();
    if (size != 0) {
      System.out.println(size + " update(s):");
      int index = 0;
      for (FileComputer fileComputer : fileComputers) {
        if (fileComputer.status != FileStatus.UNCHANGED) {
          index++;
          System.out.println(
              fileComputer.outputFilePath + " (" + fileComputer.status.toString().toLowerCase()
                  + ")");
        }
      }
    } else {
      System.out.println("All files up to date.");
    }
  }

  static File getDirectory(String path) {
    File directory = new File(path);
    if (!directory.isDirectory()) {
      System.err.println("not a directory: " + path);
      System.exit(1);
    }
    return directory;
  }

  private enum FileStatus {
    UNCHANGED, ADDED, UPDATED, DELETED
  }

  private static class FileComputer {
    private final AbstractFileGenerator fileGenerator;
    FileStatus status = FileStatus.UNCHANGED;
    final String outputFilePath;
    private final File outputDirectory;

    FileComputer(AbstractFileGenerator fileGenerator, File outputDirectory) {
      this.fileGenerator = fileGenerator;
      this.outputDirectory = outputDirectory;
      outputFilePath = fileGenerator.getOutputFilePath();
    }

    void compute() throws IOException {
      File file = new File(outputDirectory, outputFilePath);
      boolean exists = file.exists();
      boolean isGenerated = fileGenerator.isGenerated();
      if (isGenerated) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter stringPrintWriter = new PrintWriter(stringWriter);
        fileGenerator.generate(stringPrintWriter);
        String content = stringWriter.toString();
        LineWrapper lineWrapper = fileGenerator.getLineWrapper();
        if (lineWrapper != null) {
          content = lineWrapper.compute(content);
        }
        if (exists) {
          String currentContent = readFile(file);
          if (currentContent.equals(content)) {
            return;
          }
        }
        file.getParentFile().mkdirs();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
        fileWriter.close();
        if (exists) {
          status = FileStatus.UPDATED;
        } else {
          status = FileStatus.ADDED;
        }
      } else if (exists) {
        file.delete();
        status = FileStatus.DELETED;
      }
    }
  }

  static String readFile(File file) throws IOException {
    InputStream content = new FileInputStream(file);
    try {
      int length = (int) file.length();
      byte[] buffer = new byte[length];
      content.read(buffer);
      return new String(buffer, 0, length);
    } finally {
      content.close();
    }
  }

  private Generation() {
  }
}
