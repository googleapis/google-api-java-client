/*
 * Copyright 2012 Google Inc.
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
import com.google.api.client.auth.openidconnect.IdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link Beta} <br>
 * Thread-safe Google ID token verifier.
 *
 * <p>Call {@link #verify(IdToken)} to verify a ID token. Use the constructor {@link
 * #GoogleIdTokenVerifier(HttpTransport, JsonFactory)} for the typical simpler case if your
 * application has only a single instance of {@link GoogleIdTokenVerifier}. Otherwise, ideally you
 * should use {@link #GoogleIdTokenVerifier(GooglePublicKeysManager)} with a shared global instance
 * of the {@link GooglePublicKeysManager} since that way the Google public keys are cached. Sample
 * usage:
 *
 * <pre>{@code
 * GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
 *        .setAudience(Arrays.asList("myClientId"))
 *        .build();
 *
 * ...
 *
 * if (!verifier.verify(googleIdToken)) {...}
 * }</pre>
 *
 * @since 1.7
 */
@Beta
public class GoogleIdTokenVerifier extends IdTokenVerifier {

  /** Google public keys manager. */
  private final GooglePublicKeysManager publicKeys;

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   */
  public GoogleIdTokenVerifier(HttpTransport transport, JsonFactory jsonFactory) {
    this(new Builder(transport, jsonFactory));
  }

  /**
   * @param publicKeys Google public keys manager
   * @since 1.17
   */
  public GoogleIdTokenVerifier(GooglePublicKeysManager publicKeys) {
    this(new Builder(publicKeys));
  }

  /**
   * @param builder builder
   * @since 1.14
   */
  protected GoogleIdTokenVerifier(Builder builder) {
    super(builder);
    publicKeys = builder.publicKeys;
  }

  /**
   * Returns the Google public keys manager.
   *
   * @since 1.17
   */
  public final GooglePublicKeysManager getPublicKeysManager() {
    return publicKeys;
  }

  /**
   * Returns the HTTP transport.
   *
   * @since 1.14
   */
  public final HttpTransport getTransport() {
    return publicKeys.getTransport();
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return publicKeys.getJsonFactory();
  }

  /**
   * Returns the public certificates encoded URL.
   *
   * @since 1.15
   * @deprecated (scheduled to be removed in 1.18) Use {@link #getPublicKeysManager()} and {@link
   *     GooglePublicKeysManager#getPublicCertsEncodedUrl()} instead.
   */
  @Deprecated
  public final String getPublicCertsEncodedUrl() {
    return publicKeys.getPublicCertsEncodedUrl();
  }

  /**
   * Returns the public keys.
   *
   * <p>Upgrade warning: in prior version 1.16 it may return {@code null} and not throw any
   * exceptions, but starting with version 1.17 it cannot return {@code null} and may throw {@link
   * GeneralSecurityException} or {@link IOException}.
   *
   * @deprecated (scheduled to be removed in 1.18) Use {@link #getPublicKeysManager()} and {@link
   *     GooglePublicKeysManager#getPublicKeys()} instead.
   */
  @Deprecated
  public final List<PublicKey> getPublicKeys() throws GeneralSecurityException, IOException {
    return publicKeys.getPublicKeys();
  }

  /**
   * Returns the expiration time in milliseconds to be used with {@link Clock#currentTimeMillis()}
   * or {@code 0} for none.
   *
   * @deprecated (scheduled to be removed in 1.18) Use {@link #getPublicKeysManager()} and {@link
   *     GooglePublicKeysManager#getExpirationTimeMilliseconds()} instead.
   */
  @Deprecated
  public final long getExpirationTimeMilliseconds() {
    return publicKeys.getExpirationTimeMilliseconds();
  }

  /**
   * Verifies that the given ID token is valid using the cached public keys.
   *
   * <p>It verifies:
   *
   * <ul>
   *   <li>The RS256 signature, which uses RSA and SHA-256 based on the public keys downloaded from
   *       the public certificate endpoint.
   *   <li>The current time against the issued at and expiration time (allowing for a 5 minute clock
   *       skew).
   *   <li>The issuer is {@code "accounts.google.com"} or {@code "https://accounts.google.com"}.
   * </ul>
   *
   * @param googleIdToken Google ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   */
  public boolean verify(GoogleIdToken googleIdToken) throws GeneralSecurityException, IOException {
    // check the payload only
    if (!super.verifyPayload(googleIdToken)) {
      return false;
    }

    // verify signature, try all public keys in turn.
    for (PublicKey publicKey : publicKeys.getPublicKeys()) {
      if (googleIdToken.verifySignature(publicKey)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Verifies that the given ID token is valid using {@link #verify(GoogleIdToken)} and returns the
   * ID token if succeeded.
   *
   * @param idTokenString Google ID token string
   * @return Google ID token if verified successfully or {@code null} if failed
   * @since 1.9
   */
  public GoogleIdToken verify(String idTokenString) throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = GoogleIdToken.parse(getJsonFactory(), idTokenString);
    return verify(idToken) ? idToken : null;
  }

  /**
   * Downloads the public keys from the public certificates endpoint at {@link
   * #getPublicCertsEncodedUrl}.
   *
   * <p>This method is automatically called if the public keys have not yet been initialized or if
   * the expiration time is very close, so normally this doesn't need to be called. Only call this
   * method explicitly to force the public keys to be updated.
   *
   * @deprecated (scheduled to be removed in 1.18) Use {@link #getPublicKeysManager()} and {@link
   *     GooglePublicKeysManager#refresh()} instead.
   */
  @Deprecated
  public GoogleIdTokenVerifier loadPublicCerts() throws GeneralSecurityException, IOException {
    publicKeys.refresh();
    return this;
  }

  /**
   * {@link Beta} <br>
   * Builder for {@link GoogleIdTokenVerifier}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.9
   */
  @Beta
  public static class Builder extends IdTokenVerifier.Builder {

    /** Google public keys manager. */
    GooglePublicKeysManager publicKeys;

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory) {
      this(new GooglePublicKeysManager(transport, jsonFactory));
    }

    /**
     * @param publicKeys Google public keys manager
     * @since 1.17
     */
    public Builder(GooglePublicKeysManager publicKeys) {
      this.publicKeys = Preconditions.checkNotNull(publicKeys);
      setIssuers(Arrays.asList("accounts.google.com", "https://accounts.google.com"));
    }

    /** Builds a new instance of {@link GoogleIdTokenVerifier}. */
    @Override
    public GoogleIdTokenVerifier build() {
      return new GoogleIdTokenVerifier(this);
    }

    /**
     * Returns the Google public keys manager.
     *
     * @since 1.17
     */
    public final GooglePublicKeysManager getPublicCerts() {
      return publicKeys;
    }

    /** Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return publicKeys.getTransport();
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return publicKeys.getJsonFactory();
    }

    /**
     * Returns the public certificates encoded URL.
     *
     * @since 1.15
     * @deprecated (scheduled to be removed in 1.18) Use {@link #getPublicCerts()} and {@link
     *     GooglePublicKeysManager#getPublicCertsEncodedUrl()} instead.
     */
    @Deprecated
    public final String getPublicCertsEncodedUrl() {
      return publicKeys.getPublicCertsEncodedUrl();
    }

    /**
     * Sets the public certificates encoded URL.
     *
     * <p>The default value is {@link GoogleOAuthConstants#DEFAULT_PUBLIC_CERTS_ENCODED_URL}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.15
     * @deprecated (scheduled to be removed in 1.18) Use {@link
     *     GooglePublicKeysManager.Builder#setPublicCertsEncodedUrl(String)} instead.
     */
    @Deprecated
    public Builder setPublicCertsEncodedUrl(String publicKeysEncodedUrl) {
      // TODO(yanivi): make publicKeys field final when this method is removed
      publicKeys =
          new GooglePublicKeysManager.Builder(getTransport(), getJsonFactory())
              .setPublicCertsEncodedUrl(publicKeysEncodedUrl)
              .setClock(publicKeys.getClock())
              .build();
      return this;
    }

    @Override
    public Builder setIssuer(String issuer) {
      return (Builder) super.setIssuer(issuer);
    }

    /** @since 1.21.0 */
    @Override
    public Builder setIssuers(Collection<String> issuers) {
      return (Builder) super.setIssuers(issuers);
    }

    @Override
    public Builder setAudience(Collection<String> audience) {
      return (Builder) super.setAudience(audience);
    }

    @Override
    public Builder setAcceptableTimeSkewSeconds(long acceptableTimeSkewSeconds) {
      return (Builder) super.setAcceptableTimeSkewSeconds(acceptableTimeSkewSeconds);
    }

    @Override
    public Builder setClock(Clock clock) {
      return (Builder) super.setClock(clock);
    }
  }
}
