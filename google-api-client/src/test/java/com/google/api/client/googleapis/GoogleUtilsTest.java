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

import static org.junit.Assert.assertNotEquals;

import com.google.api.client.util.SecurityUtils;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleUtils}.
 *
 * @author Yaniv Inbar
 */
public class GoogleUtilsTest extends TestCase {

  public void testGetCertificateTrustStore_LoadsJdkDefaultFirst() throws Exception {
    GoogleUtils.certTrustStore = null;
    KeyStore trustStore = GoogleUtils.getCertificateTrustStore();

    // Load bundled keystore to compare
    KeyStore bundled = GoogleUtils.getBundledKeystore();

    assertNotEquals(
        "Certificate truststore should NOT contain the same amount of certificates as the bundled keystore",
        bundled.size(),
        trustStore.size());
  }

  public void testGetCertificateTrustStore_LoadsBundledKeystoreIfJdkDefaultLoadFails()
      throws Exception {
    GoogleUtils.certTrustStore = null;
    String[] originalPaths = GoogleUtils.possibleJdkPaths;
    GoogleUtils.possibleJdkPaths = new String[0];

    KeyStore trustStore = GoogleUtils.getCertificateTrustStore();

    // Load bundled keystore to compare
    KeyStore bundled = GoogleUtils.getBundledKeystore();
    assertEquals(
        "Certificate truststore should contain the same amount of certificates as the bundled keystore",
        trustStore.size(),
        bundled.size());

    GoogleUtils.possibleJdkPaths = originalPaths;
  }

  public void testGetCertificateTrustStore_IsCached() throws Exception {
    KeyStore trustStore1 = GoogleUtils.getCertificateTrustStore();
    KeyStore trustStore2 = GoogleUtils.getCertificateTrustStore();

    // Should return the exact same instance due to caching
    assertSame("Trust store should be cached", trustStore1, trustStore2);
  }

  public void testGetBundledKeystore_containsOnlyTrustedCaCertificates() throws Exception {
    KeyStore bundled = GoogleUtils.getBundledKeystore();

    assertTrue("Bundled keystore should not be empty", bundled.size() > 0);
    for (String alias : Collections.list(bundled.aliases())) {
      assertTrue(
          alias + " should be a trusted certificate entry", bundled.isCertificateEntry(alias));
      X509Certificate certificate = (X509Certificate) bundled.getCertificate(alias);
      assertTrue(alias + " should be a CA certificate", certificate.getBasicConstraints() >= 0);
    }
  }

  public void testGetBundledKeystore_containsGoogleTrustServicesRoots() throws Exception {
    List<String> subjects = getBundledCertificateSubjects();

    for (String root : new String[] {"GTS Root R1", "GTS Root R2", "GTS Root R3", "GTS Root R4"}) {
      assertTrue(
          "Bundled keystore should contain " + root,
          subjects.contains("CN=" + root + ",O=Google Trust Services LLC,C=US"));
    }
  }

  public void testGetBundledKeystore_doesNotContainAaaCertificateServices() throws Exception {
    for (String subject : getBundledCertificateSubjects()) {
      assertFalse(
          "Bundled keystore should not contain " + subject,
          subject.contains("CN=AAA Certificate Services"));
    }
  }

  public void testBundledJksKeystore_containsSameCertificatesAsBundledKeystore() throws Exception {
    // This library doesn't read google.jks, but some applications load it directly this way.
    KeyStore jks = SecurityUtils.getJavaKeyStore();
    SecurityUtils.loadKeyStore(
        jks,
        GoogleUtils.class.getResourceAsStream("google.jks"),
        GoogleUtils.BUNDLED_KEYSTORE_PASSWORD);

    assertEquals(getCertificates(GoogleUtils.getBundledKeystore()), getCertificates(jks));
  }

  private static Set<Certificate> getCertificates(KeyStore keyStore) throws Exception {
    Set<Certificate> certificates = new HashSet<>();
    for (String alias : Collections.list(keyStore.aliases())) {
      certificates.add(keyStore.getCertificate(alias));
    }
    return certificates;
  }

  private static List<String> getBundledCertificateSubjects() throws Exception {
    KeyStore bundled = GoogleUtils.getBundledKeystore();
    List<String> subjects = new ArrayList<>();
    for (String alias : Collections.list(bundled.aliases())) {
      X509Certificate certificate = (X509Certificate) bundled.getCertificate(alias);
      subjects.add(certificate.getSubjectX500Principal().getName());
    }
    return subjects;
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
