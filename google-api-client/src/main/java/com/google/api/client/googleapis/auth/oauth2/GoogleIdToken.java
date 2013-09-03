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
import java.util.List;

/**
 * {@link Beta} <br/>
 * Google ID tokens as specified in <a
 * href="https://developers.google.com/accounts/docs/OAuth2Login">Using OAuth 2.0 for Login</a>.
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
    /** Hosted domain name if asserted user is a domain managed user or {@code null} for none. */
    @Key("hd")
    private String hostedDomain;

    /** E-mail of the user or {@code null} if not requested. */
    @Key("email")
    private String email;

    /**
     * {@code true} if the email is verified.
     * TODO(mwan): change the type of the field to Boolean and the handling in
     * {@link #getEmailVerified()} accordingly after Google OpenID Connect endpoint fixes the
     * type of the field in ID Token.
     */
    @Key("email_verified")
    private Object emailVerified;

    public Payload() {
    }

    /**
     * Returns the obfuscated Google user id or {@code null} for none.
     *
     * @deprecated (scheduled to be removed in 1.18) Use {@link #getSubject()} instead.
     */
    @Deprecated
    public String getUserId() {
      return getSubject();
    }

    /**
     * Sets the obfuscated Google user id or {@code null} for none.
     *
     * @deprecated (scheduled to be removed in 1.18) Use {@link #setSubject(String)} instead.
     */
    @Deprecated
    public Payload setUserId(String userId) {
      return setSubject(userId);
    }

    /**
     * Returns the client ID of issuee or {@code null} for none.
     *
     * @deprecated (scheduled to be removed in 1.18) Use {@link #getAuthorizedParty()} instead.
     */
    @Deprecated
    public String getIssuee() {
      return getAuthorizedParty();
    }

    /**
     * Sets the client ID of issuee or {@code null} for none.
     *
     * @deprecated (scheduled to be removed in 1.18) Use {@link #setAuthorizedParty(String)}
     *             instead.
     */
    @Deprecated
    public Payload setIssuee(String issuee) {
      return setAuthorizedParty(issuee);
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
     *
     * <p>
     * Upgrade warning: in prior version 1.16 this method accessed {@code "verified_email"}
     * and returns a boolean, but starting with verison 1.17, it now accesses
     * {@code "email_verified"} and returns a Boolean. Previously, if this value was not
     * specified, this method would return {@code false}, but now it returns {@code null}.
     * </p>
     */
    public Boolean getEmailVerified() {
      if (emailVerified == null) {
        return null;
      }
      if (emailVerified instanceof Boolean) {
        return (Boolean) emailVerified;
      }

      return Boolean.valueOf((String) emailVerified);
    }

    /**
     * Sets whether the users e-mail address has been verified by Google or not.
     *
     * <p>
     * Used in conjunction with the {@code "https://www.googleapis.com/auth/userinfo.email"} scope.
     * </p>
     *
     * @since 1.10
     *
     * <p>
     * Upgrade warning: in prior version 1.16 this method accessed {@code "verified_email"} and
     * required a boolean parameter, but starting with verison 1.17, it now accesses
     * {@code "email_verified"} and requires a Boolean parameter.
     * </p>
     */
    public Payload setEmailVerified(Boolean emailVerified) {
      this.emailVerified = emailVerified;
      return this;
    }

    @Override
    public Payload setAuthorizationTimeSeconds(Long authorizationTimeSeconds) {
      return (Payload) super.setAuthorizationTimeSeconds(authorizationTimeSeconds);
    }

    @Override
    public Payload setAuthorizedParty(String authorizedParty) {
      return (Payload) super.setAuthorizedParty(authorizedParty);
    }

    @Override
    public Payload setNonce(String nonce) {
      return (Payload) super.setNonce(nonce);
    }

    @Override
    public Payload setAccessTokenHash(String accessTokenHash) {
      return (Payload) super.setAccessTokenHash(accessTokenHash);
    }

    @Override
    public Payload setClassReference(String classReference) {
      return (Payload) super.setClassReference(classReference);
    }

    @Override
    public Payload setMethodsReferences(List<String> methodsReferences) {
      return (Payload) super.setMethodsReferences(methodsReferences);
    }

    @Override
    public Payload setExpirationTimeSeconds(Long expirationTimeSeconds) {
      return (Payload) super.setExpirationTimeSeconds(expirationTimeSeconds);
    }

    @Override
    public Payload setNotBeforeTimeSeconds(Long notBeforeTimeSeconds) {
      return (Payload) super.setNotBeforeTimeSeconds(notBeforeTimeSeconds);
    }

    @Override
    public Payload setIssuedAtTimeSeconds(Long issuedAtTimeSeconds) {
      return (Payload) super.setIssuedAtTimeSeconds(issuedAtTimeSeconds);
    }

    @Override
    public Payload setIssuer(String issuer) {
      return (Payload) super.setIssuer(issuer);
    }

    @Override
    public Payload setAudience(Object audience) {
      return (Payload) super.setAudience(audience);
    }

    @Override
    public Payload setJwtId(String jwtId) {
      return (Payload) super.setJwtId(jwtId);
    }

    @Override
    public Payload setType(String type) {
      return (Payload) super.setType(type);
    }

    @Override
    public Payload setSubject(String subject) {
      return (Payload) super.setSubject(subject);
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
