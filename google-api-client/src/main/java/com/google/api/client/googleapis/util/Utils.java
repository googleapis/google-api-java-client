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
import java.security.KeyStore;
import java.util.ArrayList;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.SecurityUtils;
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
  public static final String GOOGLE_API_USE_CLIENT_CERTIFICATE = "GOOGLE_API_USE_CLIENT_CERTIFICATE";

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

  @SuppressWarnings("unchecked")
  public static ArrayList<String> extractCertificateProviderCommand(String contextAwareMetadataJson) {
    GsonBuilder builder = new GsonBuilder();
    LinkedTreeMap<String, Object> map = (LinkedTreeMap<String, Object>)builder.create().fromJson(contextAwareMetadataJson, Object.class);
    return (ArrayList<String>)map.get("cert_provider_command");
  }

  public static InputStream loadDefaultCertificate() throws IOException, GeneralSecurityException {
    File file = new File(CONTEXT_AWARE_METADATA_PATH);
    if (!file.exists()) {
      return null;
    }

    // Load the cert provider command from the json file.
    String json = new String(Files.readAllBytes(Paths.get(CONTEXT_AWARE_METADATA_PATH)));
    ArrayList<String> command = extractCertificateProviderCommand(json);
    
    // Call the command.
    Process process = new ProcessBuilder(command).start();
    int exitCode = 0;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException exception) {
      throw new GeneralSecurityException("Failed to execute cert provider command", exception);
    }
    if (exitCode != 0) {
      throw new GeneralSecurityException("Failed to execute cert provider command");
    }

    return process.getInputStream();
  }

  public static KeyStore loadMtlsKeyStore(InputStream clientCertificateSource) throws IOException, GeneralSecurityException {
    String useClientCertificate = System.getenv(GOOGLE_API_USE_CLIENT_CERTIFICATE);
    if ("true".equals(useClientCertificate)) {
      InputStream certificateToUse = null;
      if (clientCertificateSource != null) {
        certificateToUse = clientCertificateSource;
      } else {
        certificateToUse = loadDefaultCertificate();
      }

      if (certificateToUse != null) {
        return SecurityUtils.createMtlsKeyStore(certificateToUse);
      }
    }
    return null;
  }

  private Utils() {
  }
}
