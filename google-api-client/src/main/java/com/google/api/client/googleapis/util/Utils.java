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

package com.google.api.client.googleapis.util;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.SecurityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utility class for the Google API Client Library.
 *
 * @since 1.19
 */
public final class Utils {

  /** Cached value for {@link #getCertificateTrustStore()}. */
  static KeyStore certTrustStore;

  /**
   * Returns the key store for trusted root certificates to use for Google APIs.
   *
   * <p>
   * Value is cached, so subsequent access is fast.
   * </p>
   *
   * @since 1.19, since 1.14 in com.google.api.client.googleapis.GoogleUtils
   */
  public static synchronized KeyStore getCertificateTrustStore()
      throws IOException, GeneralSecurityException {
    if (certTrustStore == null) {
      certTrustStore = SecurityUtils.getJavaKeyStore();
      InputStream keyStoreStream = Utils.class.getResourceAsStream("google.jks");
      SecurityUtils.loadKeyStore(certTrustStore, keyStoreStream, "notasecret");
    }
    return certTrustStore;
  }

  /**
   * {@link Beta} <br/>
   * Returns a cached default implementation of the JsonFactory interface.
   */
  @Beta
  public static JsonFactory getDefaultJsonFactory() {
    return JsonFactoryInstanceHolder.INSTANCE;
  }

  @Beta
  private static class JsonFactoryInstanceHolder {
    // The jackson2.JacksonFactory was introduced as a product dependency in 1.19 to enable
    // other APIs to not require one of these for input. This was the most commonly used
    // implementation in public samples. This is a compile-time dependency to help detect the
    // dependency as early as possible.
    static final JsonFactory INSTANCE = new JacksonFactory();
  }

  /**
   * {@link Beta} <br/>
   * Returns a cached default implementation of the HttpTransport interface.
   */
  @Beta
  public static HttpTransport getDefaultTransport() {
    return TransportInstanceHolder.INSTANCE;
  }

  @Beta
  private static class TransportInstanceHolder {
    static final HttpTransport INSTANCE = new NetHttpTransport();
  }

  private Utils() {
  }
}
