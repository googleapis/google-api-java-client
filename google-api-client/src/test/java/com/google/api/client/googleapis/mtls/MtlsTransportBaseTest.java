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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.SecurityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class MtlsTransportBaseTest  {
  protected KeyStore createTestMtlsKeyStore() throws IOException, GeneralSecurityException {
    InputStream certAndKey = getClass()
        .getClassLoader()
        .getResourceAsStream("com/google/api/client/googleapis/util/mtlsCertAndKey.pem");
    return SecurityUtils.createMtlsKeyStore(certAndKey);
  }

  protected static class TestMtlsProvider implements MtlsProvider {
    private boolean useClientCertificate;
    private KeyStore keyStore;
    private String keyStorePassword;
    TestMtlsProvider(boolean useClientCertificate, KeyStore keystore, String keyStorePassword) {
      this.useClientCertificate = useClientCertificate;
      this.keyStore = keystore;
      this.keyStorePassword = keyStorePassword;
    }
    @Override
    public boolean useMtlsClientCertificate() {
      return useClientCertificate;
    }

    @Override
    public String getKeyStorePassword() {
      return keyStorePassword;
    }

    @Override
    public KeyStore getKeyStore() throws IOException, GeneralSecurityException {
      return keyStore;
    }
  }

  abstract protected HttpTransport buildTrustedTransport(MtlsProvider mtlsProvider) throws IOException, GeneralSecurityException;

  // If client certificate shouldn't be used, then neither the provided mtlsKeyStore
  // nor the default mtls key store should be used.
  @Test
  public void testNotUseCertificate() throws IOException, GeneralSecurityException {
    MtlsProvider mtlsProvider = new TestMtlsProvider(false, createTestMtlsKeyStore(), "");
    HttpTransport transport = buildTrustedTransport(mtlsProvider);
    assertFalse(transport.isMtls());
  }

  // If client certificate should be used, and mtlsKeyStore is provided, then the
  // provided key store should be used.
  @Test
  public void testUseProvidedCertificate() throws IOException, GeneralSecurityException {
    MtlsProvider mtlsProvider = new TestMtlsProvider(true, createTestMtlsKeyStore(), "");
    HttpTransport transport = buildTrustedTransport(mtlsProvider);
    assertTrue(transport.isMtls());
  }

  // If client certificate should be used, but no mtls key store is available, then
  // the transport created is not mtls.
  @Test
  public void testNoCertificate() throws IOException, GeneralSecurityException {
    MtlsProvider mtlsProvider = new TestMtlsProvider(true, null, "");
    HttpTransport transport = buildTrustedTransport(mtlsProvider);
    assertFalse(transport.isMtls());
  }
}
