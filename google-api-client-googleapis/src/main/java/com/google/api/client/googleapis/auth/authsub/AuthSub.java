/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.googleapis.auth.authsub;

import com.google.api.client.auth.RsaSha;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SecureRandom;

/**
 * @since 1.0
 * @author Yaniv Inbar
 */
public class AuthSub {

  /** Secure random number generator to sign requests. */
  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Returns {@code AuthSub} authorization header value based on the given authentication token.
   */
  public static String getAuthorizationHeaderValue(String token) {
    return getAuthSubTokenPrefix(token).toString();
  }

  /**
   * Returns {@code AuthSub} authorization header value based on the given authentication token,
   * private key, request method, and request URL.
   *
   * @throws GeneralSecurityException
   */
  public static String getAuthorizationHeaderValue(
      String token, PrivateKey privateKey, String requestMethod, String requestUrl)
      throws GeneralSecurityException {
    StringBuilder buf = getAuthSubTokenPrefix(token);
    if (privateKey != null) {
      String algorithm = privateKey.getAlgorithm();
      if (!"RSA".equals(algorithm)) {
        throw new IllegalArgumentException(
            "Only supported algorithm for private key is RSA: " + algorithm);
      }
      long timestamp = System.currentTimeMillis() / 1000;
      long nonce = Math.abs(RANDOM.nextLong());
      String data = new StringBuilder()
          .append(requestMethod)
          .append(' ')
          .append(requestUrl)
          .append(' ')
          .append(timestamp)
          .append(' ')
          .append(nonce)
          .toString();
      String sig = RsaSha.sign(privateKey, data);
      buf
          .append(" sigalg=\"rsa-sha1\" data=\"")
          .append(data)
          .append("\" sig=\"")
          .append(sig)
          .append('"');
    }
    return buf.toString();
  }

  private static StringBuilder getAuthSubTokenPrefix(String token) {
    return new StringBuilder("AuthSub token=\"").append(token).append('"');
  }

  private AuthSub() {
  }
}
