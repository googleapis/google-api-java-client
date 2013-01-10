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

  /**
   * Major part of the current release version.
   *
   * @since 1.14
   */
  public static final int MAJOR_VERSION = 1;

  /**
   * Minor part of the current release version.
   *
   * @since 1.14
   */
  public static final int MINOR_VERSION = 14;

  /**
   * Bug fix part of the current release version.
   *
   * @since 1.14
   */
  public static final int BUGFIX_VERSION = 0;

  /** Current release version. */
  public static final String VERSION = MAJOR_VERSION + "." + MINOR_VERSION + "." + BUGFIX_VERSION
      + "-beta-SNAPSHOT";

  /** Cached value for {@link #getCertificateTrustStore()}. */
  static KeyStore certTrustStore;

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
