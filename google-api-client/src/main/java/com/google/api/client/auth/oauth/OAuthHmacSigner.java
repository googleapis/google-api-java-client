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

package com.google.api.client.auth.oauth;

import com.google.api.client.auth.HmacSha;

import java.security.GeneralSecurityException;

/**
 * OAuth {@code "HMAC-SHA1"} signature method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class OAuthHmacSigner implements OAuthSigner {

  /** Client-shared secret or {@code null} for none. */
  public String clientSharedSecret;

  /** Token-shared secret or {@code null} for none. */
  public String tokenSharedSecret;

  public String getSignatureMethod() {
    return "HMAC-SHA1";
  }

  public String computeSignature(String signatureBaseString) throws GeneralSecurityException {
    StringBuilder keyBuf = new StringBuilder();
    String clientSharedSecret = this.clientSharedSecret;
    if (clientSharedSecret != null) {
      keyBuf.append(OAuthParameters.escape(clientSharedSecret));
    }
    keyBuf.append('&');
    String tokenSharedSecret = this.tokenSharedSecret;
    if (tokenSharedSecret != null) {
      keyBuf.append(OAuthParameters.escape(tokenSharedSecret));
    }
    return HmacSha.sign(keyBuf.toString(), signatureBaseString);
  }
}
