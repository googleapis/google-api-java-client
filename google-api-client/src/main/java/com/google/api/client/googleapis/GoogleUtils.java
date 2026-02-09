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

import com.google.api.client.util.SecurityUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for the Google API Client Library.
 *
 * @since 1.12
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class GoogleUtils {

  /** Current release version. */
  public static final String VERSION = getVersion();

  // NOTE: Integer instead of int so compiler thinks it isn't a constant, so it won't inline it
  /**
   * Major part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer MAJOR_VERSION;

  /**
   * Minor part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer MINOR_VERSION;

  /**
   * Bug fix part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer BUGFIX_VERSION;

  @VisibleForTesting
  static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-SNAPSHOT)?");

  static {
    Matcher versionMatcher = VERSION_PATTERN.matcher(VERSION);
    versionMatcher.find();
    MAJOR_VERSION = Integer.parseInt(versionMatcher.group(1));
    MINOR_VERSION = Integer.parseInt(versionMatcher.group(2));
    BUGFIX_VERSION = Integer.parseInt(versionMatcher.group(3));
  }

  /** Cached value for {@link #getCertificateTrustStore()}. */
  @VisibleForTesting
  static KeyStore certTrustStore;

  /** Default JDK cacerts file path relative to java.home. */
  @VisibleForTesting
  static String defaultCacertsPath = "lib/security/cacerts";

  /** Default password for JDK cacerts file. */
  static final String DEFAULT_CACERTS_PASSWORD = "changeit";

  /** Java home system property key. */
  static final String JAVA_HOME_KEY = "java.home";

  /** Name of bundled keystore. */
  static final String BUNDLED_KEYSTORE = "google.p12";

  /** Bundled keystore password. */
  static final String BUNDLED_KEYSTORE_PASSWORD = "notasecret";

  /**
   * Loads the bundled google.p12 key store containing trusted root certificates.
   * 
   * @return the loaded key store
   */
  @VisibleForTesting
  static KeyStore getBundledKeystore() throws IOException, GeneralSecurityException {
    KeyStore ks = SecurityUtils.getPkcs12KeyStore();
    InputStream is = GoogleUtils.class.getResourceAsStream(BUNDLED_KEYSTORE);
    SecurityUtils.loadKeyStore(ks, is, BUNDLED_KEYSTORE_PASSWORD);
    return ks;
  }

    /**
   * Loads the default JDK key store (cacerts) containing trusted root certificates.
   * Determines the path to the cacerts file based on the java.home system property.
   * 
   * @return the loaded key store
   */
  @VisibleForTesting
  static KeyStore getJdkDefaultKeyStore() throws IOException, GeneralSecurityException {
    String javaHome = System.getProperty(JAVA_HOME_KEY);
    File file = new File(javaHome, defaultCacertsPath);

    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (FileInputStream is = new FileInputStream(file)) {
      trustStore.load(is, DEFAULT_CACERTS_PASSWORD.toCharArray());
    }
    return trustStore;
  }

  /**
   * Returns a key store for trusted root certificates to use for Google APIs.
   *
   * <p>Value is cached, so subsequent access is fast.
   *
   * <p>This method first attempts to load the JDK default keystore. If that fails or is not
   * available, it falls back to loading the bundled Google certificate store.
   *
   * @since 1.14
   * @deprecated This method is deprecated because it relies on a bundled certificate store
   *     that is not maintained. Please use {@link #getJdkDefaultTrustStore()} to load the
   *     JDK default keystore directly, or use your own certificate store as needed.
   */
  @Deprecated
  public static synchronized KeyStore getCertificateTrustStore()
      throws IOException, GeneralSecurityException {
    if (certTrustStore == null) {
      // Try to load JDK default trust store first
      try {
        certTrustStore = getJdkDefaultKeyStore();
      } catch (Exception e) {
        // If fails to load default, fall through to bundled certificates
      }

      if (certTrustStore == null) {
        certTrustStore = getBundledKeystore();
      }
    }
    return certTrustStore;
  }

  private static String getVersion() {
    // attempt to read the library's version from a properties file generated during the build
    // this value should be read and cached for later use
    String version = null;
    try (InputStream inputStream =
        GoogleUtils.class.getResourceAsStream(
            "/com/google/api/client/googleapis/google-api-client.properties")) {
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);
        version = properties.getProperty("google-api-client.version");
      }
    } catch (IOException e) {
      // ignore
    }
    return version == null ? "unknown-version" : version;
  }

  private GoogleUtils() {}
}
