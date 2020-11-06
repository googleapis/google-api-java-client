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

package com.google.api.client.googleapis.mtls;

import com.google.api.client.util.Beta;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * {@link Beta} <br>
 * Provider interface for mutual TLS. It is used in {@link
 * GoogleApacheHttpTransport#newTrustedTransport(MtlsProvider)} and {@link
 * GoogleNetHttpTransport#newTrustedTransport(MtlsProvider)} to configure the mutual TLS in the
 * transport.
 *
 * @since 1.31
 */
@Beta
public interface MtlsProvider {
  /**
   * Returns if mutual TLS client certificate should be used. If the value is true, the key store
   * from {@link #getKeyStore()} and key store password from {@link #getKeyStorePassword()} will be
   * used to configure mutual TLS transport.
   */
  boolean useMtlsClientCertificate();

  /** The key store to use for mutual TLS. */
  String getKeyStorePassword();

  /** The password for mutual TLS key store. */
  KeyStore getKeyStore() throws IOException, GeneralSecurityException;
}
