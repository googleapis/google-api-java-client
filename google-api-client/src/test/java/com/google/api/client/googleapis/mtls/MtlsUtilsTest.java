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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import org.junit.Test;

public class MtlsUtilsTest {
  static class TestEnvironmentProvider
      implements MtlsUtils.DefaultMtlsProvider.EnvironmentProvider {
    private final String value;

    TestEnvironmentProvider(String value) {
      this.value = value;
    }

    @Override
    public String getenv(String name) {
      return value;
    }
  }

  @Test
  public void testUseMtlsClientCertificateEmpty() {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(new TestEnvironmentProvider(""), "/path/to/missing/file");
    assertFalse(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testUseMtlsClientCertificateNull() {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider(null), "/path/to/missing/file");
    assertFalse(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testUseMtlsClientCertificateTrue() {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"), "/path/to/missing/file");
    assertTrue(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testLoadDefaultKeyStoreMissingFile()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"), "/path/to/missing/file");
    KeyStore keyStore = mtlsProvider.getKeyStore();
    assertNull(keyStore);
  }

  @Test
  public void testLoadDefaultKeyStore()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"),
            "src/test/resources/com/google/api/client/googleapis/util/mtls_context_aware_metadata.json");
    KeyStore keyStore = mtlsProvider.getKeyStore();
    assertNotNull(keyStore);
  }

  @Test
  public void testLoadDefaultKeyStoreBadCertificate()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"),
            "src/test/resources/com/google/api/client/googleapis/util/mtls_context_aware_metadata_bad_command.json");
    try {
      mtlsProvider.getKeyStore();
      fail("should throw and exception");
    } catch (IllegalArgumentException e) {
      assertTrue(
          "expected to fail with certificate is missing",
          e.getMessage().contains("certificate is missing"));
    }
  }

  @Test
  public void testExtractCertificateProviderCommand() throws IOException {
    InputStream inputStream =
        this.getClass()
            .getClassLoader()
            .getResourceAsStream(
                "com/google/api/client/googleapis/util/mtls_context_aware_metadata.json");
    List<String> command =
        MtlsUtils.DefaultMtlsProvider.extractCertificateProviderCommand(inputStream);
    assertEquals(2, command.size());
    assertEquals("cat", command.get(0));
    assertEquals(
        "src/test/resources/com/google/api/client/googleapis/util/mtlsCertAndKey.pem",
        command.get(1));
  }

  static class TestCertProviderCommandProcess extends Process {
    private boolean runForever;
    private int exitValue;

    public TestCertProviderCommandProcess(int exitValue, boolean runForever) {
      this.runForever = runForever;
      this.exitValue = exitValue;
    }

    @Override
    public OutputStream getOutputStream() {
      return null;
    }

    @Override
    public InputStream getInputStream() {
      return null;
    }

    @Override
    public InputStream getErrorStream() {
      return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
      return 0;
    }

    @Override
    public int exitValue() {
      if (runForever) {
        throw new IllegalThreadStateException();
      }
      return exitValue;
    }

    @Override
    public void destroy() {}
  }

  @Test
  public void testRunCertificateProviderCommandSuccess() throws IOException, InterruptedException {
    Process certCommandProcess = new TestCertProviderCommandProcess(0, false);
    int exitValue =
        MtlsUtils.DefaultMtlsProvider.runCertificateProviderCommand(certCommandProcess, 100);
    assertEquals(0, exitValue);
  }

  @Test
  public void testRunCertificateProviderCommandTimeout() throws InterruptedException {
    Process certCommandProcess = new TestCertProviderCommandProcess(0, true);
    try {
      MtlsUtils.DefaultMtlsProvider.runCertificateProviderCommand(certCommandProcess, 100);
      fail("should throw and exception");
    } catch (IOException e) {
      assertTrue(
          "expected to fail with timeout",
          e.getMessage().contains("cert provider command timed out"));
    }
  }
}
