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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Tests {@link Utils}.
 *
 * @author Yaniv Inbar
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, Utils.class})
@PowerMockIgnore({"jdk.internal.reflect.*", "javax.net.ssl.*"})
public class UtilsTest extends TestCase {

  public void testGetDefaultJsonFactory() {
    JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
    assertNotNull(jsonFactory);
    assertTrue(jsonFactory instanceof JacksonFactory);
    JsonFactory secondCall = Utils.getDefaultJsonFactory();
    assertSame(jsonFactory, secondCall);
  }

  public void testGetDefaultTransport() {
    HttpTransport transport = Utils.getDefaultTransport();
    assertNotNull(transport);
    assertTrue(transport instanceof NetHttpTransport);
    HttpTransport secondCall = Utils.getDefaultTransport();
    assertSame(transport, secondCall);
  }

  public static Map<String, String> parseQuery(String query) throws IOException {
    Map<String, String> map = new HashMap<String, String>();
    String[] entries = query.split("&");
    for (String entry : entries) {
      String[] sides = entry.split("=");
      if (sides.length != 2) {
        throw new IOException("Invalid Query String");
      }
      String key = URLDecoder.decode(sides[0], "UTF-8");
      String value = URLDecoder.decode(sides[1], "UTF-8");
      map.put(key, value);
    }
    return map;
  }

  public void testExtractCertificateProviderCommand() {
    String json = "{\"cert_provider_command\":[\"/opt/google/endpoint-verification/bin/apihelper\",\"--print_certificate\"],\"device_resource_ids\":[\"123\"]}";
    ArrayList<String> command = Utils.extractCertificateProviderCommand(json);
    assertEquals(2, command.size());
    assertEquals("/opt/google/endpoint-verification/bin/apihelper", command.get(0));
    assertEquals("--print_certificate", command.get(1));
  }

  public InputStream getMtlsCertificateAndKey() {
    return getClass()
    .getClassLoader()
    .getResourceAsStream("com/google/api/client/googleapis/util/mtlsCertAndKey.pem");
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "true"
  // - User provided client cert to Utils.loadMtlsKeyStore
  // In this case the provided client cert will be used to create a key store.
  public void testUseCertWithProvidedCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("true");

    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(getMtlsCertificateAndKey());
    assertNotNull(mtlsKeyStore);    
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "true"
  // - default client cert exists
  // In this case the default client cert will be used to create a key store.
  public void testUseCertWithDefaultCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("true");
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.loadDefaultCertificate()).thenReturn(getMtlsCertificateAndKey());
    
    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(null);
    assertNotNull(mtlsKeyStore);    
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "true"
  // - no client cert is provided or exists
  // In this case key store is null because there is no client cert.
  public void testUseCertWithNoCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("true");
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.loadDefaultCertificate()).thenReturn(null);
    
    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(null);
    assertNull(mtlsKeyStore);    
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "false"
  // - User provided client cert to Utils.loadMtlsKeyStore
  // In this case client cert is not used, so no key store is created.
  public void testNotUseCertWithProvidedCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("false");

    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(getMtlsCertificateAndKey());
    assertNull(mtlsKeyStore);    
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "false"
  // - default client cert exists
  // In this case client cert is not used, so no key store is created.
  public void testNotUseCertWithDefaultCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("false");
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.loadDefaultCertificate()).thenReturn(getMtlsCertificateAndKey());
    
    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(null);
    assertNull(mtlsKeyStore);    
  }

  // Test the case for Utils.loadMtlsKeyStore where:
  // - GOOGLE_API_USE_CLIENT_CERTIFICATE = "false"
  // - no client cert is provided or exists
  // In this case client cert is not used, so no key store is created.
  public void testNotUseCertNoCert() throws Exception {
    PowerMockito.spy(System.class);
    PowerMockito.when(System.getenv(Utils.GOOGLE_API_USE_CLIENT_CERTIFICATE)).thenReturn("false");
    PowerMockito.spy(Utils.class);
    PowerMockito.when(Utils.loadDefaultCertificate()).thenReturn(null);
    
    KeyStore mtlsKeyStore = Utils.loadMtlsKeyStore(null);
    assertNull(mtlsKeyStore);    
  }
}