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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
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
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(new TestEnvironmentProvider(""), "/path/to/missing/file");
    assertFalse(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testUseMtlsClientCertificateNull() {
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider(null), "/path/to/missing/file");
    assertFalse(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testUseMtlsClientCertificateTrue() {
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"), "/path/to/missing/file");
    assertTrue(mtlsProvider.useMtlsClientCertificate());
  }

  @Test
  public void testLoadDefaultKeyStoreMissingFile()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"), "/path/to/missing/file");
    KeyStore keyStore = mtlsProvider.loadDefaultKeyStore();
    assertNull(keyStore);
  }

  @Test
  public void testLoadDefaultKeyStore()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"),
            "src/test/resources/com/google/api/client/googleapis/util/mtls_context_aware_metadata.json");
    KeyStore keyStore = mtlsProvider.loadDefaultKeyStore();
    assertNotNull(keyStore);
  }

  @Test
  public void testLoadDefaultKeyStoreBadCertificate()
      throws InterruptedException, GeneralSecurityException, IOException {
    MtlsUtils.MtlsProvider mtlsProvider =
        new MtlsUtils.DefaultMtlsProvider(
            new TestEnvironmentProvider("true"),
            "src/test/resources/com/google/api/client/googleapis/util/mtls_context_aware_metadata_bad_command.json");
    try {
      KeyStore keyStore = mtlsProvider.loadDefaultKeyStore();
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
        this.getClass().getResourceAsStream("mtls_context_aware_metadata.json");
    List<String> command =
        MtlsUtils.DefaultMtlsProvider.extractCertificateProviderCommand(inputStream);
    assertEquals(2, command.size());
    assertEquals("cat", command.get(0));
    assertEquals(
        "src/test/resources/com/google/api/client/googleapis/util/mtlsCertAndKey.pem",
        command.get(1));
  }
}
