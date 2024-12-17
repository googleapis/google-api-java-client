/*
 * Copyright 2024 Google LLC
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

package com.google.api.client.googleapis.apache.v5;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.mtls.MtlsProvider;
import com.google.api.client.googleapis.mtls.MtlsUtils;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.apache.v5.Apache5HttpTransport;
import com.google.api.client.util.SslUtils;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;

/**
 * Utilities for Google APIs based on {@link Apache5HttpTransport}.
 *
 * @since 2.6.1
 */
public final class GoogleApache5HttpTransport {

  /**
   * Returns a new instance of {@link Apache5HttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. If
   * `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true", and the default
   * client certificate key store from {@link Utils#loadDefaultMtlsKeyStore()} is not null, then the
   * transport uses the default client certificate and is mutual TLS.
   */
  public static Apache5HttpTransport newTrustedTransport()
      throws GeneralSecurityException, IOException {
    return newTrustedTransport(MtlsUtils.getDefaultMtlsProvider());
  }

  /**
   * {@link Beta} <br>
   * Returns a new instance of {@link Apache5HttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. mtlsProvider can be used
   * to configure mutual TLS for the transport.
   *
   * @param mtlsProvider MtlsProvider to configure mutual TLS for the transport
   */
  @Beta
  public static Apache5HttpTransport newTrustedTransport(MtlsProvider mtlsProvider)
      throws GeneralSecurityException, IOException {

    SocketFactoryRegistryHandler handler = new SocketFactoryRegistryHandler(mtlsProvider);

    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(handler.getSocketFactoryRegistry());
    connectionManager.setMaxTotal(200);
    connectionManager.setDefaultMaxPerRoute(20);
    connectionManager.setDefaultConnectionConfig(
        ConnectionConfig.custom()
            .setTimeToLive(-1, TimeUnit.MILLISECONDS)
            .setValidateAfterInactivity(-1L, TimeUnit.MILLISECONDS)
            .build());

    CloseableHttpClient client =
        HttpClients.custom()
            .useSystemProperties()
            .setConnectionManager(connectionManager)
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .disableRedirectHandling()
            .disableAutomaticRetries()
            .build();

    return new Apache5HttpTransport(client, handler.isMtls());
  }

  @VisibleForTesting
  static class SocketFactoryRegistryHandler {
    private final Registry<ConnectionSocketFactory> socketFactoryRegistry;
    private final boolean isMtls;

    public SocketFactoryRegistryHandler(MtlsProvider mtlsProvider)
        throws GeneralSecurityException, IOException {
      KeyStore mtlsKeyStore = null;
      String mtlsKeyStorePassword = null;
      if (mtlsProvider.useMtlsClientCertificate()) {
        mtlsKeyStore = mtlsProvider.getKeyStore();
        mtlsKeyStorePassword = mtlsProvider.getKeyStorePassword();
      }

      // Use the included trust store
      KeyStore trustStore = GoogleUtils.getCertificateTrustStore();
      SSLContext sslContext = SslUtils.getTlsSslContext();

      if (mtlsKeyStore != null && mtlsKeyStorePassword != null) {
        this.isMtls = true;
        SslUtils.initSslContext(
            sslContext,
            trustStore,
            SslUtils.getPkixTrustManagerFactory(),
            mtlsKeyStore,
            mtlsKeyStorePassword,
            SslUtils.getDefaultKeyManagerFactory());
      } else {
        this.isMtls = false;
        SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
      }
      LayeredConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

      this.socketFactoryRegistry =
          RegistryBuilder.<ConnectionSocketFactory>create()
              .register("http", PlainConnectionSocketFactory.getSocketFactory())
              .register("https", socketFactory)
              .build();
    }

    public Registry<ConnectionSocketFactory> getSocketFactoryRegistry() {
      return this.socketFactoryRegistry;
    }

    public boolean isMtls() {
      return this.isMtls;
    }
  }

  private GoogleApache5HttpTransport() {}
}
