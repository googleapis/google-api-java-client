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

package com.google.api.client.googleapis.apache;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.http.apache.ApacheHttpTransport;

import com.google.api.client.util.SslUtils;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

/**
 * Utilities for Google APIs based on {@link ApacheHttpTransport}.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class GoogleApacheHttpTransport {

  /**
   * Returns a new instance of {@link ApacheHttpTransport} that uses
   * {@link GoogleUtils#getCertificateTrustStore()} for the trusted certificates.
   */
  public static ApacheHttpTransport newTrustedTransport() throws GeneralSecurityException,
      IOException {
    // Set socket buffer sizes to 8192
    SocketConfig socketConfig =
        SocketConfig.custom()
            .setRcvBufSize(8192)
            .setSndBufSize(8192)
            .build();

    PoolingHttpClientConnectionManager connectionManager =
        new PoolingHttpClientConnectionManager(-1, TimeUnit.MILLISECONDS);

    // Disable the stale connection check (previously configured in the HttpConnectionParams
    connectionManager.setValidateAfterInactivity(-1);

    // Use the included trust store
    KeyStore trustStore = GoogleUtils.getCertificateTrustStore();
    SSLContext sslContext = SslUtils.getTlsSslContext();
    SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
    LayeredConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);

    HttpClient client = HttpClientBuilder.create()
        .useSystemProperties()
        .setSSLSocketFactory(socketFactory)
        .setDefaultSocketConfig(socketConfig)
        .setMaxConnTotal(200)
        .setMaxConnPerRoute(20)
        .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()))
        .setConnectionManager(connectionManager)
        .disableRedirectHandling()
        .disableAutomaticRetries()
        .build();
    return new ApacheHttpTransport(client);
  }

  private GoogleApacheHttpTransport() {
  }
}
