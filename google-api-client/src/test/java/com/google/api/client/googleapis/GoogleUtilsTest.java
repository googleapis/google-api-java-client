/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis;

import junit.framework.TestCase;

import java.security.KeyStore;
import java.util.Enumeration;

/**
 * Tests {@link GoogleUtils}.
 *
 * @author Yaniv Inbar
 */
public class GoogleUtilsTest extends TestCase {

  public void testGetCertificateTrustStore() throws Exception {
    KeyStore trustStore = GoogleUtils.getCertificateTrustStore();
    Enumeration<String> aliases = trustStore.aliases();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      assertTrue(trustStore.isCertificateEntry(alias));
    }
    // intentionally check the count of certificates, so it can help us detect if a new certificate
    // has been added or removed
    assertEquals(70, trustStore.size());
  }
}
