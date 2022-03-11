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
import com.google.api.client.googleapis.mtls.MtlsProvider;
import com.google.api.client.googleapis.mtls.MtlsUtils;
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
   * Returns a new instance of {@link NetHttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates using {@link
   * com.google.api.client.http.javanet.NetHttpTransport.Builder#trustCertificates(KeyStore)}. If
   * `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true", and the default
   * client certificate key store from {@link Utils#loadDefaultMtlsKeyStore()} is not null, then the
   * transport uses the default client certificate and is mutual TLS.
   *
   * <p>This helper method doesn't provide for customization of the {@link NetHttpTransport}, such
   * as the ability to specify a proxy. To do use, use {@link
   * com.google.api.client.http.javanet.NetHttpTransport.Builder}, for example:
   *
   * <pre>{@code
   * static HttpTransport newProxyTransport() throws GeneralSecurityException, IOException {
   *   NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
   *   builder.trustCertificates(GoogleUtils.getCertificateTrustStore());
   *   builder.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 3128)));
   *   return builder.build();
   * }
   * }</pre>
   */
  public static NetHttpTransport newTrustedTransport()
      throws GeneralSecurityException, IOException {
    return newTrustedTransport(MtlsUtils.getDefaultMtlsProvider());
  }

  /**
   * {@link Beta} <br>
   * Returns a new instance of {@link NetHttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates using {@link
   * com.google.api.client.http.javanet.NetHttpTransport.Builder#trustCertificates(KeyStore)}.
   * mtlsProvider can be used to configure mutual TLS for the transport.
   *
   * @param mtlsProvider MtlsProvider to configure mutual TLS for the transport
   */
  @Beta
  public static NetHttpTransport newTrustedTransport(MtlsProvider mtlsProvider)
      throws GeneralSecurityException, IOException {
    KeyStore mtlsKeyStore = null;
    String mtlsKeyStorePassword = null;
    if (mtlsProvider.useMtlsClientCertificate()) {
      mtlsKeyStore = mtlsProvider.getKeyStore();
      mtlsKeyStorePassword = mtlsProvider.getKeyStorePassword();
    }

    if (mtlsKeyStore != null && mtlsKeyStorePassword != null) {
      return new NetHttpTransport.Builder()
          .trustCertificates(
              GoogleUtils.getCertificateTrustStore(), mtlsKeyStore, mtlsKeyStorePassword)
          .build();
    }
    return new NetHttpTransport.Builder()
        .trustCertificates(GoogleUtils.getCertificateTrustStore())
        .build();
  }

  private GoogleNetHttpTransport() {}
}
