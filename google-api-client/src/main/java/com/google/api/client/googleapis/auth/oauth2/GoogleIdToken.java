/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * {@link Beta} <br/>
 * Google ID tokens.
 *
 * <p>
 * Google ID tokens contain useful information about the authorized end user. Google ID tokens are
 * signed and the signature must be verified using {@link #verify(GoogleIdTokenVerifier)}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
@SuppressWarnings("javadoc")
@Beta
public class GoogleIdToken extends IdToken {

  /**
   * Parses the given ID token string and returns the parsed {@link GoogleIdToken}.
   *
   * @param jsonFactory JSON factory
   * @param idTokenString ID token string
   * @return parsed Google ID token
   */
  public static GoogleIdToken parse(JsonFactory jsonFactory, String idTokenString)
      throws IOException {
    JsonWebSignature jws =
        JsonWebSignature.parser(jsonFactory).setPayloadClass(Payload.class).parse(idTokenString);
    return new GoogleIdToken(jws.getHeader(), (Payload) jws.getPayload(), jws.getSignatureBytes(),
        jws.getSignedContentBytes());
  }

  /**
   * @param header header
   * @param payload payload
   * @param signatureBytes bytes of the signature
   * @param signedContentBytes bytes of the signature content
   */
  public GoogleIdToken(
      Header header, Payload payload, byte[] signatureBytes, byte[] signedContentBytes) {
    super(header, payload, signatureBytes, signedContentBytes);
  }

  /**
   * Verifies that this ID token is valid using {@link GoogleIdTokenVerifier#verify(GoogleIdToken)}.
   */
  public boolean verify(GoogleIdTokenVerifier verifier)
      throws GeneralSecurityException, IOException {
    return verifier.verify(this);
  }

  @Override
  public Payload getPayload() {
    return (Payload) super.getPayload();
  }

  /**
   * {@link Beta} <br/>
   * Google ID token payload.
   */
  @Beta
  public static class Payload extends IdToken.Payload {

    /** Obfuscated Google user ID or {@code null} for none. */
    @Key("id")
    private String userId;

    /** Client ID of issuee or {@code null} for none. */
    @Key("cid")
    private String issuee;

    /** Hash of access token or {@code null} for none. */
    @Key("token_hash")
    private String accessTokenHash;

    /** Hosted domain name if asserted user is a domain managed user or {@code null} for none. */
    @Key("hd")
    private String hostedDomain;

    /** E-mail of the user or {@code null} if not requested. */
    @Key("email")
    private String email;

    /** {@code true} if the email is verified. */
    @Key("verified_email")
    private boolean emailVerified;

    public Payload() {
    }

    /** Returns the obfuscated Google user id or {@code null} for none. */
    public String getUserId() {
      return userId;
    }

    /** Sets the obfuscated Google user id or {@code null} for none. */
    public Payload setUserId(String userId) {
      this.userId = userId;
      return this;
    }

    /** Returns the client ID of issuee or {@code null} for none. */
    public String getIssuee() {
      return issuee;
    }

    /** Sets the client ID of issuee or {@code null} for none. */
    public Payload setIssuee(String issuee) {
      this.issuee = issuee;
      return this;
    }

    /** Returns the hash of access token or {@code null} for none. */
    public String getAccessTokenHash() {
      return accessTokenHash;
    }

    /** Sets the hash of access token or {@code null} for none. */
    public Payload setAccessTokenHash(String accessTokenHash) {
      this.accessTokenHash = accessTokenHash;
      return this;
    }

    /**
     * Returns the hosted domain name if asserted user is a domain managed user or {@code null} for
     * none.
     */
    public String getHostedDomain() {
      return hostedDomain;
    }

    /**
     * Sets the hosted domain name if asserted user is a domain managed user or {@code null} for
     * none.
     */
    public Payload setHostedDomain(String hostedDomain) {
      this.hostedDomain = hostedDomain;
      return this;
    }

    /**
     * Returns the e-mail address of the user or {@code null} if it was not requested.
     *
     * <p>
     * Requires the {@code "https://www.googleapis.com/auth/userinfo.email"} scope.
     * </p>
     *
     * @since 1.10
     */
    public String getEmail() {
      return email;
    }

    /**
     * Sets the e-mail address of the user or {@code null} if it was not requested.
     *
     * <p>
     * Used in conjunction with the {@code "https://www.googleapis.com/auth/userinfo.email"} scope.
     * </p>
     *
     * @since 1.10
     */
    public Payload setEmail(String email) {
      this.email = email;
      return this;
    }

    /**
     * Returns {@code true} if the users e-mail address has been verified by Google.
     *
     * <p>
     * Requires the {@code "https://www.googleapis.com/auth/userinfo.email"} scope.
     * </p>
     *
     * @since 1.10
     */
    public boolean getEmailVerified() {
      return emailVerified;
    }

    /**
     * Sets whether the users e-mail address has been verified by Google or not.
     *
     * <p>
     * Used in conjunction with the {@code "https://www.googleapis.com/auth/userinfo.email"} scope.
     * </p>
     *
     * @since 1.10
     */
    public Payload setEmailVerified(boolean emailVerified) {
      this.emailVerified = emailVerified;
      return this;
    }

    @Override
    public Payload set(String fieldName, Object value) {
      return (Payload) super.set(fieldName, value);
    }

    @Override
    public Payload clone() {
      return (Payload) super.clone();
    }
  }
}
