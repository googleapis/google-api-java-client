/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Beta;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

/**
 * {@link Beta} <br/>
 * Utility class for the Google API Client Library.
 *
 * @since 1.19
 */
@Beta
public final class Utils {
  private static final String CONTEXT_AWARE_METADATA_PATH = System.getProperty("user.home") + "/.secureConnect/context_aware_metadata.json";

  /**
   * Returns a cached default implementation of the JsonFactory interface.
   */
  public static JsonFactory getDefaultJsonFactory() {
    return JsonFactoryInstanceHolder.INSTANCE;
  }

  private static class JsonFactoryInstanceHolder {
    // The jackson2.JacksonFactory was introduced as a product dependency in 1.19 to enable
    // other APIs to not require one of these for input. This was the most commonly used
    // implementation in public samples. This is a compile-time dependency to help detect the
    // dependency as early as possible.
    static final JsonFactory INSTANCE = new JacksonFactory();
  }

  /**
   * Returns a cached default implementation of the HttpTransport interface.
   */
  public static HttpTransport getDefaultTransport() {
    return TransportInstanceHolder.INSTANCE;
  }

  private static class TransportInstanceHolder {
    static final HttpTransport INSTANCE = new NetHttpTransport();
  }

  public static boolean hasDefaultCertSource() {
    File file = new File(CONTEXT_AWARE_METADATA_PATH);
    return file.exists();
  }

  @SuppressWarnings("unchecked")
  public static InputStream loadDefaultCert() throws IOException, InterruptedException, GeneralSecurityException {
    String json = new String(Files.readAllBytes(Paths.get(CONTEXT_AWARE_METADATA_PATH)));
    GsonBuilder builder = new GsonBuilder();
    LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>)builder.create().fromJson(json, Object.class);
    ArrayList<String> commands = (ArrayList<String>)map.get("cert_provider_command");
    Process process = new ProcessBuilder(commands).start();
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new GeneralSecurityException("Failed to execute cert provider command");
    }
    return process.getInputStream();
  }

  private Utils() {
  }
}
