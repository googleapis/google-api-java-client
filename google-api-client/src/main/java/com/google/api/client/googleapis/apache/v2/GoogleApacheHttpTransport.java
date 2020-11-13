/*
 * Copyright 2020 Google LLC
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

package com.google.api.client.googleapis.apache.v2;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.googleapis.mtls.MtlsProvider;
import com.google.api.client.googleapis.mtls.MtlsUtils;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.util.Beta;
import com.google.api.client.util.SslUtils;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

/**
 * Utilities for Google APIs based on {@link ApacheHttpTransport}.
 *
 * @since 1.31
 */
public final class GoogleApacheHttpTransport {

  /**
   * Returns a new instance of {@link ApacheHttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. If
   * `GOOGLE_API_USE_CLIENT_CERTIFICATE` environment variable is set to "true", and the default
   * client certificate key store from {@link Utils#loadDefaultMtlsKeyStore()} is not null, then the
   * transport uses the default client certificate and is mutual TLS.
   */
  public static ApacheHttpTransport newTrustedTransport()
      throws GeneralSecurityException, IOException {
    return newTrustedTransport(MtlsUtils.getDefaultMtlsProvider());
  }

  /**
   * {@link Beta} <br>
   * Returns a new instance of {@link ApacheHttpTransport} that uses {@link
   * GoogleUtils#getCertificateTrustStore()} for the trusted certificates. mtlsProvider can be used
   * to configure mutual TLS for the transport.
   *
   * @param mtlsProvider MtlsProvider to configure mutual TLS for the transport
   */
  @Beta
  public static ApacheHttpTransport newTrustedTransport(MtlsProvider mtlsProvider)
      throws GeneralSecurityException, IOException {
    SocketFactoryRegistryHandler handler = new SocketFactoryRegistryHandler(mtlsProvider);
    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(
            handler.getSocketFactoryRegistry(), null, null, null, -1, TimeUnit.MILLISECONDS);

    // Disable the stale connection check (previously configured in the
    // HttpConnectionParams
    connectionManager.setValidateAfterInactivity(-1);

    HttpClient client =
        HttpClientBuilder.create()
            .useSystemProperties()
            .setMaxConnTotal(200)
            .setMaxConnPerRoute(20)
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
            .setConnectionManager(connectionManager)
            .disableRedirectHandling()
            .disableAutomaticRetries()
            .build();
    return new ApacheHttpTransport(client, handler.isMtls());
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

  private GoogleApacheHttpTransport() {}
}
