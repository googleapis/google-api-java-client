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

package com.google.api.client.googleapis.apache;

import com.google.api.client.googleapis.GoogleUtils;
import com.google.api.client.http.apache.ApacheHttpTransport;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utilities for Google APIs based on {@link ApacheHttpTransport}.
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
public final class GoogleApacheHttpTransport {

  /**
   * Returns a new instance of {@link ApacheHttpTransport} that uses
   * {@link GoogleUtils#getCertificateTrustStore()} for the trusted certificates using
   * {@link com.google.api.client.http.apache.ApacheHttpTransport.Builder#trustCertificates(KeyStore)}.
   */
  public static ApacheHttpTransport newTrustedTransport() throws GeneralSecurityException,
      IOException {
    return new ApacheHttpTransport.Builder().trustCertificates(
        GoogleUtils.getCertificateTrustStore()).build();
  }

  private GoogleApacheHttpTransport() {
  }
}
