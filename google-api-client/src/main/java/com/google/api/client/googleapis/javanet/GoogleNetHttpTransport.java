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

package com.google.api.client.googleapis.javanet;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Beta;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utilities for Google APIs based on {@link NetHttpTransport}.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public class GoogleNetHttpTransport {

  /**
   * Returns a new instance of {@link NetHttpTransport} that uses
   * {@link GoogleUtils#getCertificateTrustStore()} for the trusted certificates using
   * {@link com.google.api.client.http.javanet.NetHttpTransport.Builder#trustCertificates(KeyStore)}.
   * If `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true",
   * and the default client certificate key store from {@link Utils#loadDefaultMtlsKeyStore()}
   * is not null, then the transport uses the default client certificate and
   * is mutual TLS. 
   *
   * <p>
   * This helper method doesn't provide for customization of the {@link NetHttpTransport}, such as
   * the ability to specify a proxy. To do use, use
   * {@link com.google.api.client.http.javanet.NetHttpTransport.Builder}, for example:
   * </p>
   *
   * <pre>
  static HttpTransport newProxyTransport() throws GeneralSecurityException, IOException {
    NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
    builder.trustCertificates(GoogleUtils.getCertificateTrustStore());
    builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 3128)));
    return builder.build();
  }
   * </pre>
   */
  public static NetHttpTransport newTrustedTransport()
      throws GeneralSecurityException, IOException {
    return newTrustedTransport(null, "");
  }

  /**
   * {@link Beta} <br>
   * Returns a new instance of {@link NetHttpTransport} that uses
   * {@link GoogleUtils#getCertificateTrustStore()} for the trusted certificates using
   * {@link com.google.api.client.http.javanet.NetHttpTransport.Builder#trustCertificates(KeyStore)}.
   * If `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true",
   * the function uses the provided mtlsKeyStore or the default key store from
   * {@link Utils#loadDefaultMtlsKeyStore()} to create the transport. If either key
   * store exists, then the created transport is mutual TLS. The provided key store
   * takes precedence over the default one. 
   * 
   * @param mtlsKeyStore KeyStore for mutual TLS client certificate and private key
   * @param mtlsKeyStorePassword KeyStore password
   * @since 1.31   
   */
  @Beta
  public static NetHttpTransport newTrustedTransport(KeyStore mtlsKeyStore, String mtlsKeyStorePassword)
      throws GeneralSecurityException, IOException {
    // Figure out if mTLS is needed and what key store to use.
    Boolean useMtls = Utils.useMtlsClientCertificate();
    KeyStore mtlsKeyStoreToUse = mtlsKeyStore;
    String mtlsKeyStorePasswordToUse = mtlsKeyStorePassword;
    if (useMtls && mtlsKeyStoreToUse == null) {
      // Use the default mTLS key store if not provided.
      mtlsKeyStoreToUse = Utils.loadDefaultMtlsKeyStore();
      mtlsKeyStorePasswordToUse = "";
    }

    if (useMtls && mtlsKeyStoreToUse != null && mtlsKeyStoreToUse.size() > 0) {
      return new NetHttpTransport.Builder()
          .trustCertificates(GoogleUtils.getCertificateTrustStore(), mtlsKeyStoreToUse, mtlsKeyStorePasswordToUse)
          .build();
    }
    return new NetHttpTransport.Builder().trustCertificates(GoogleUtils.getCertificateTrustStore()).build();
  }

  private GoogleNetHttpTransport() {}
}
