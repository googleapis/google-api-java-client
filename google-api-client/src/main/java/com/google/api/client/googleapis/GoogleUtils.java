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

import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.SecurityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utility class for the Google API Client Library.
 *
 * @since 1.12
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class GoogleUtils {

  /** Current version of the Google API Client Library for Java. */
  public static final String VERSION = "1.14.0-beta-SNAPSHOT";

  /** Cached value for {@link #getCertificateTrustStore()}. */
  static KeyStore certTrustStore;

  /**
   * Returns a new instance of {@link NetHttpTransport} that uses
   * {@link #getCertificateTrustStore()} for the trusted certificates using
   * {@link NetHttpTransport.Builder#trustCertificates(KeyStore)}.
   *
   * @since 1.14
   */
  @SuppressWarnings("javadoc")
  public static NetHttpTransport newTrustedNetHttpTransport()
      throws GeneralSecurityException, IOException {
    return new NetHttpTransport.Builder().trustCertificates(getCertificateTrustStore()).build();
  }

  /**
   * Returns a new instance of {@link ApacheHttpTransport} that uses
   * {@link #getCertificateTrustStore()} for the trusted certificates using
   * {@link ApacheHttpTransport.Builder#trustCertificates(KeyStore)}.
   *
   * @since 1.14
   */
  @SuppressWarnings("javadoc")
  public static ApacheHttpTransport newTrustedApacheHttpTransport()
      throws GeneralSecurityException, IOException {
    return new ApacheHttpTransport.Builder().trustCertificates(getCertificateTrustStore()).build();
  }

  /**
   * Returns the key store for trusted root certificates to use for Google APIs.
   *
   * <p>
   * Value is cached, so subsequent access is fast.
   * </p>
   *
   * @since 1.14
   */
  public static synchronized KeyStore getCertificateTrustStore()
      throws IOException, GeneralSecurityException {
    if (certTrustStore == null) {
      certTrustStore = SecurityUtils.getJavaKeyStore();
      InputStream keyStoreStream = GoogleUtils.class.getResourceAsStream("google.jks");
      SecurityUtils.loadKeyStore(certTrustStore, keyStoreStream, "notasecret");
    }
    return certTrustStore;
  }

  private GoogleUtils() {
  }
}
