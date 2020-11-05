/*
 * Copyright 2020 Google LLC
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

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;
import com.google.api.client.util.SecurityUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;

public class MtlsUtils {
  public interface MtlsProvider {
    boolean useMtlsClientCertificate();

    String getKeyStorePassword();

    KeyStore loadDefaultKeyStore() throws IOException, GeneralSecurityException;
  }

  /**
   * {@link Beta} <br>
   * Data class representing context_aware_metadata.json file.
   *
   * @since 1.31
   */
  @Beta
  public static class ContextAwareMetadataJson extends GenericJson {
    /** Cert provider command */
    @Key("cert_provider_command")
    private List<String> commands;

    /**
     * Returns the cert provider command.
     *
     * @since 1.31
     */
    public final List<String> getCommands() {
      return commands;
    }
  }

  @VisibleForTesting
  static class DefaultMtlsProvider implements MtlsProvider {
    private static final String DEFAULT_CONTEXT_AWARE_METADATA_PATH =
        System.getProperty("user.home") + "/.secureConnect/context_aware_metadata.json";

    /** GOOGLE_API_USE_CLIENT_CERTIFICATE environment variable */
    public static final String GOOGLE_API_USE_CLIENT_CERTIFICATE =
        "GOOGLE_API_USE_CLIENT_CERTIFICATE";

    interface EnvironmentProvider {
      String getenv(String name);
    }

    static class SystemEnvironmentProvider implements EnvironmentProvider {
      @Override
      public String getenv(String name) {
        return System.getenv(name);
      }
    }

    DefaultMtlsProvider() {
      this(new SystemEnvironmentProvider(), DEFAULT_CONTEXT_AWARE_METADATA_PATH);
    }

    private EnvironmentProvider envProvider;
    private String metadataPath;

    @VisibleForTesting
    DefaultMtlsProvider(EnvironmentProvider envProvider, String metadataPath) {
      this.envProvider = envProvider;
      this.metadataPath = metadataPath;
    }

    @Override
    public boolean useMtlsClientCertificate() {
      String useClientCertificate = envProvider.getenv(GOOGLE_API_USE_CLIENT_CERTIFICATE);
      return "true".equals(useClientCertificate);
    }

    @Override
    public String getKeyStorePassword() {
      return "";
    }

    @Override
    public KeyStore loadDefaultKeyStore() throws IOException, GeneralSecurityException {
      // Load the cert provider command from the json file.
      InputStream stream;
      try {
        stream = new FileInputStream(metadataPath);
      } catch (FileNotFoundException ignored) {
        // file doesn't exist
        return null;
      }

      List<String> command = extractCertificateProviderCommand(stream);

      // Call the command.
      Process process = new ProcessBuilder(command).start();
      int exitCode = -1;
      try {
        exitCode = process.waitFor();
      } catch (InterruptedException e) {
        throw new IOException("Interrupted executing certificate provider command", e);
      }
      if (exitCode != 0) {
        throw new IOException(
            String.format("Failed to execute cert provider command with exit code: %d", exitCode));
      }

      // Parse input certificates from shell command
      return SecurityUtils.createMtlsKeyStore(process.getInputStream());
    }

    @VisibleForTesting
    static List<String> extractCertificateProviderCommand(InputStream contextAwareMetadata)
        throws IOException {
      JsonParser parser = Utils.getDefaultJsonFactory().createJsonParser(contextAwareMetadata);
      ContextAwareMetadataJson json = parser.parse(ContextAwareMetadataJson.class);
      return json.getCommands();
    }
  }

  private static final MtlsProvider MTLS_PROVIDER = new DefaultMtlsProvider();

  public static MtlsProvider getDefaultMtlsProvider() {
    return MTLS_PROVIDER;
  }
}
