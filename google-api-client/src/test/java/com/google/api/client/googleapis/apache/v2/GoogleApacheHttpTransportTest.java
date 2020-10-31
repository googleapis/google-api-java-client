package com.google.api.client.googleapis.apache.v2;

import java.io.InputStream;
import java.security.KeyStore;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.util.SecurityUtils;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

public class GoogleApacheHttpTransportTest {
  public InputStream getCertAndKey() throws Exception {
    return getClass()
      .getClassLoader()
      .getResourceAsStream("com/google/api/client/googleapis/util/mtlsCertAndKey.pem");
  }

  // mTLS key store is provided, so mTLS transport is created
  public void testWithMtlsKeyStore() throws Exception {
    KeyStore mtlsKeyStore = SecurityUtils.createMtlsKeyStore(getCertAndKey());
    PowerMockito.mockStatic(Utils.class);
    PowerMockito.when(Utils.loadMtlsKeyStore(Mockito.any(InputStream.class))).thenReturn(mtlsKeyStore);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(getCertAndKey());
    assertTrue(transport.isMtls());
  }
  
  // mTLS key store doesn't exist, so transport is not mTLS
  public void testWithoutMtlsKeyStore() throws Exception {
    PowerMockito.mockStatic(Utils.class);
    PowerMockito.when(Utils.loadMtlsKeyStore(Mockito.any(InputStream.class))).thenReturn(null);
    ApacheHttpTransport transport = GoogleApacheHttpTransport.newTrustedTransport(getCertAndKey());
    assertFalse(transport.isMtls());
  }
}
