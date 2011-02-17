/*
 * Copyright (c) 2010 Google Inc. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.auth;

import com.google.api.client.util.Base64;
import com.google.api.client.util.Strings;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility methods for {@code "HMAC-SHA1"} signing method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class HmacSha {

  /**
   * Signs the given data using the given secret key.
   *
   * @throws GeneralSecurityException general security exception
   */
  public static String sign(String key, String data) throws GeneralSecurityException {
    SecretKey secretKey = new SecretKeySpec(Strings.toBytesUtf8(key), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(secretKey);
    byte[] encoded = Base64.encode(mac.doFinal(Strings.toBytesUtf8(data)));
    return Strings.fromBytesUtf8(encoded);
  }

  private HmacSha() {
  }
}
