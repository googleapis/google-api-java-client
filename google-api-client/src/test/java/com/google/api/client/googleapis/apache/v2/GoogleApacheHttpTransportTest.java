package com.google.api.client.googleapis.apache.v2;

import java.io.InputStream;
import java.security.KeyStore;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.util.SecurityUtils;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class, GoogleApacheHttpTransport.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.net.ssl.*"})
public class GoogleApacheHttpTransportTest extends TestCase {
  public KeyStore createTestMtlsKeyStore() throws Exception {
    InputStream certAndKey = getClass()
      .getClassLoader()
      .getResourceAsStream("com/google/api/client/googleapis/util/mtlsCertAndKey.pem");
    return SecurityUtils.createMtlsKeyStore(certAndKey);
  }

  // If client certificate shouldn't be used, then neither the provided mtlsKeyStore
  // nor the default mtls key store should be used.
  public void testNotUseCertificate() throws Exception {
    KeyStore mtlsKeyStore = createTestMtlsKeyStore();
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.useMtlsClientCertificate()).thenReturn(false);
    PowerMockito.when(Utils.loadDefaultMtlsKeyStore()).thenReturn(mtlsKeyStore);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(mtlsKeyStore, "");
    assertFalse(transport.isMtls());
  }

  // If client certificate should be used, and mtlsKeyStore is provided, then the
  // provided key store should be used.
  public void testUseProvidedCertificate() throws Exception {
    KeyStore mtlsKeyStore = createTestMtlsKeyStore();
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.useMtlsClientCertificate()).thenReturn(true);
    PowerMockito.when(Utils.loadDefaultMtlsKeyStore()).thenReturn(null);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(mtlsKeyStore, "");
    assertTrue(transport.isMtls());
  }

  // If client certificate should be used, and mtlsKeyStore is provided, then the
  // provided key store should be used.
  public void testUseDefaultCertificate() throws Exception {
    KeyStore mtlsKeyStore = createTestMtlsKeyStore();
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.useMtlsClientCertificate()).thenReturn(true);
    PowerMockito.when(Utils.loadDefaultMtlsKeyStore()).thenReturn(mtlsKeyStore);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(null, "");
    assertTrue(transport.isMtls());
  }

  // If client certificate should be used, but no mtls key store is available, then
  // the transport created is not mtls.
  public void testNoCertificate() throws Exception {
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.useMtlsClientCertificate()).thenReturn(true);
    PowerMockito.when(Utils.loadDefaultMtlsKeyStore()).thenReturn(null);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(null, "");
    assertFalse(transport.isMtls());
  }
}
