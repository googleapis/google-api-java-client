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

package com.google.api.client.googleapis.mtls;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.JsonParser;
import com.google.api.client.util.Beta;
import com.google.api.client.util.SecurityUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;

/**
 * {@link Beta} <br>
 * Utilities for mutual TLS.
 *
 * @since 1.31
 */
@Beta
public class MtlsUtils {
  @VisibleForTesting
  static class DefaultMtlsProvider implements MtlsProvider {
    private static final String DEFAULT_CONTEXT_AWARE_METADATA_PATH =
        System.getProperty("user.home") + "/.secureConnect/context_aware_metadata.json";

    /** GOOGLE_API_USE_CLIENT_CERTIFICATE environment variable. */
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
    public KeyStore getKeyStore() throws IOException, GeneralSecurityException {
      try {
        // Load the cert provider command from the json file.
        InputStream stream = new FileInputStream(metadataPath);
        List<String> command = extractCertificateProviderCommand(stream);

        // Run the command and timeout after 1000 milliseconds.
        Process process = new ProcessBuilder(command).start();
        int exitCode = runCertificateProviderCommand(process, 1000);
        if (exitCode != 0) {
          throw new IOException("Cert provider command failed with exit code: " + exitCode);
        }

        // Create mTLS key store with the input certificates from shell command.
        return SecurityUtils.createMtlsKeyStore(process.getInputStream());
      } catch (FileNotFoundException ignored) {
        // file doesn't exist
        return null;
      } catch (InterruptedException e) {
        throw new IOException("Interrupted executing certificate provider command", e);
      }
    }

    @VisibleForTesting
    static List<String> extractCertificateProviderCommand(InputStream contextAwareMetadata)
        throws IOException {
      JsonParser parser = Utils.getDefaultJsonFactory().createJsonParser(contextAwareMetadata);
      ContextAwareMetadataJson json = parser.parse(ContextAwareMetadataJson.class);
      return json.getCommands();
    }

    @VisibleForTesting
    static int runCertificateProviderCommand(Process commandProcess, long timeoutMilliseconds)
        throws IOException, InterruptedException {
      long startTime = System.currentTimeMillis();
      long remainTime = timeoutMilliseconds;
      boolean terminated = false;

      do {
        try {
          // Check if process is terminated by polling the exitValue, which throws
          // IllegalThreadStateException if not terminated.
          commandProcess.exitValue();
          terminated = true;
          break;
        } catch (IllegalThreadStateException ex) {
          if (remainTime > 0) {
            Thread.sleep(Math.min(remainTime + 1, 100));
          }
        }
        remainTime = remainTime - (System.currentTimeMillis() - startTime);
      } while (remainTime > 0);

      if (!terminated) {
        commandProcess.destroy();
        throw new IOException("cert provider command timed out");
      }

      return commandProcess.exitValue();
    }
  }

  private static final MtlsProvider MTLS_PROVIDER = new DefaultMtlsProvider();

  /**
   * Returns the default MtlsProvider instance.
   *
   * @return The default MtlsProvider instance
   */
  public static MtlsProvider getDefaultMtlsProvider() {
    return MTLS_PROVIDER;
  }
}
