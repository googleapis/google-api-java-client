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

package com.google.api.client.googleapis;

import java.security.KeyStore;
import java.util.Enumeration;
import java.util.regex.Matcher;

import junit.framework.TestCase;

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

  public void testVersionMatcher() {
    String version = "1.30.3";
    Matcher matcher = GoogleUtils.VERSION_PATTERN.matcher(version);
    assertTrue(matcher.find());
    assertEquals(1, Integer.parseInt(matcher.group(1)));
    assertEquals(30, Integer.parseInt(matcher.group(2)));
    assertEquals(3, Integer.parseInt(matcher.group(3)));
  }

  public void testVersionMatcherSnapshot() {
    String version = "1.30.3-SNAPSHOT";
    Matcher matcher = GoogleUtils.VERSION_PATTERN.matcher(version);
    assertTrue(matcher.find());
    assertEquals(1, Integer.parseInt(matcher.group(1)));
    assertEquals(30, Integer.parseInt(matcher.group(2)));
    assertEquals(3, Integer.parseInt(matcher.group(3)));
  }

  public void testVersion() {
    Matcher matcher = GoogleUtils.VERSION_PATTERN.matcher(GoogleUtils.VERSION);
    assertTrue(matcher.find());
    assertNotNull(GoogleUtils.MAJOR_VERSION);
    assertNotNull(GoogleUtils.MINOR_VERSION);
    assertNotNull(GoogleUtils.BUGFIX_VERSION);
  }
}
