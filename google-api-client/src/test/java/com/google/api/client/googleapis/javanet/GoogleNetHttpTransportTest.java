package com.google.api.client.googleapis.javanet;

import java.io.InputStream;
import java.security.KeyStore;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.SecurityUtils;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class, GoogleNetHttpTransport.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.net.ssl.*"})
public class GoogleNetHttpTransportTest extends TestCase {
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
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransportBuilder(getCertAndKey()).build();
        assertTrue(transport.isMtls());
    }
    
    // mTLS key store doesn't exist, so transport is not mTLS
    public void testWithoutMtlsKeyStore() throws Exception {
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.loadMtlsKeyStore(Mockito.any(InputStream.class))).thenReturn(null);
        NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransportBuilder(getCertAndKey()).build();
        assertFalse(transport.isMtls());
    }
}
