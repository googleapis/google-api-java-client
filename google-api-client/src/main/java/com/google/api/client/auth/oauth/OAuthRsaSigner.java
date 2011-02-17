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

import com.google.api.client.auth.RsaSha;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;

/**
 * OAuth {@code "RSA-SHA1"} signature method.
 * <p>
 * The private key may be retrieved using the utilities in {@link RsaSha}.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class OAuthRsaSigner implements OAuthSigner {

  /** Private key. */
  public PrivateKey privateKey;

  public String getSignatureMethod() {
    return "RSA-SHA1";
  }

  public String computeSignature(String signatureBaseString) throws GeneralSecurityException {
    return RsaSha.sign(privateKey, signatureBaseString);
  }
}
