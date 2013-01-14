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

import com.google.api.client.auth.jsontoken.JsonWebSignature;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Clock;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;
import com.google.common.base.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread-safe Google ID token verifier.
 *
 * <p>
 * The public keys are loaded Google's public certificate endpoint at
 * {@code "https://www.googleapis.com/oauth2/v1/certs"}. The public keys are cached in this instance
 * of {@link GoogleIdTokenVerifier}. Therefore, for maximum efficiency, applications should use a
 * single globally-shared instance of the {@link GoogleIdTokenVerifier}. Use
 * {@link #verify(GoogleIdToken, Collection, Collection)} to verify a Google ID token.
 * </p>
 *
 * <p>
 * Samples usage:
 * </p>
 *
 * <pre>
  public static GoogleIdTokenVerifier verifier;

  public static void initVerifier(
      HttpTransport transport, JsonFactory jsonFactory, String clientId) {
    verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
        .setClientId(clientId)
        .build();
  }

  public static boolean verifyToken(GoogleIdToken idToken)
      throws GeneralSecurityException, IOException {
    return verifier.verify(idToken);
  }
 * </pre>
 * @since 1.7
 */
public class GoogleIdTokenVerifier {

  /** Pattern for the max-age header element of Cache-Control. */
  private static final Pattern MAX_AGE_PATTERN = Pattern.compile("\\s*max-age\\s*=\\s*(\\d+)\\s*");

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Public keys or {@code null} for none. */
  private List<PublicKey> publicKeys;

  /**
   * Expiration time in milliseconds to be used with {@link Clock#currentTimeMillis()} or {@code 0}
   * for none.
   */
  private long expirationTimeMilliseconds;

  /** Set of Client IDs. */
  @Deprecated
  private Set<String> clientIds;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** Lock on the public keys. */
  private final Lock lock = new ReentrantLock();

  /** Clock to use for expiration checks. */
  private final Clock clock;

  /**
   * Constructor with required parameters.
   *
   * <p>
   * Use {@link GoogleIdTokenVerifier.Builder} to specify client IDs.
   * </p>
   *
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   */
  public GoogleIdTokenVerifier(HttpTransport transport, JsonFactory jsonFactory) {
    this(new Builder(transport, jsonFactory));
  }

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected GoogleIdTokenVerifier(Builder builder) {
    clientIds = builder.clientIds == null
        ? Collections.<String>emptySet() : Collections.unmodifiableSet(builder.clientIds);
    transport = Preconditions.checkNotNull(builder.transport);
    jsonFactory = Preconditions.checkNotNull(builder.jsonFactory);
    clock = Preconditions.checkNotNull(builder.clock);
  }

  /**
   * Construct the {@link GoogleIdTokenVerifier}.
   *
   * @param clientIds set of client IDs or {@code null} for none
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use {@link #GoogleIdTokenVerifier(Builder)} and
   *             {@link #verify(GoogleIdToken, Collection, Collection)} or
   *             {@link #verify(String, Collection, Collection)}
   */
  @Deprecated
  protected GoogleIdTokenVerifier(
      Set<String> clientIds, HttpTransport transport, JsonFactory jsonFactory) {
    this(clientIds, transport, jsonFactory, Clock.SYSTEM);
  }

  /**
   * Construct the {@link GoogleIdTokenVerifier}.
   *
   * @param clientIds set of client IDs or {@code null} for none
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param clock Clock for expiration checks
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use {@link #GoogleIdTokenVerifier(Builder)} and
   *             {@link #verify(GoogleIdToken, Collection, Collection)} or
   *             {@link #verify(String, Collection, Collection)}
   */
  @Deprecated
  protected GoogleIdTokenVerifier(
      Set<String> clientIds, HttpTransport transport, JsonFactory jsonFactory, Clock clock) {
    this.clientIds =
        clientIds == null ? Collections.<String>emptySet() : Collections.unmodifiableSet(clientIds);
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.clock = Preconditions.checkNotNull(clock);
  }

  /**
   * Returns the HTTP transport.
   *
   * @since 1.14
   */
  public final HttpTransport getTransport() {
    return transport;
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Returns the set of client IDs.
   *
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link #verify(GoogleIdToken, Collection, Collection)} or
   *             {@link #verify(String, Collection, Collection)}
   */
  @Deprecated
  public final Set<String> getClientIds() {
    return clientIds;
  }

  /** Returns the public keys or {@code null} for none. */
  public final List<PublicKey> getPublicKeys() {
    return publicKeys;
  }

  /**
   * Returns the expiration time in milliseconds to be used with {@link Clock#currentTimeMillis()}
   * or {@code 0} for none.
   */
  public final long getExpirationTimeMilliseconds() {
    return expirationTimeMilliseconds;
  }

  /**
   * Verifies that the given ID token is valid using {@link #verify(GoogleIdToken, String)} with the
   * {@link #getClientIds()}.
   *
   * @param idToken Google ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link #verify(GoogleIdToken, Collection, Collection)}
   */
  @Deprecated
  public boolean verify(GoogleIdToken idToken) throws GeneralSecurityException, IOException {
    return verify(clientIds, idToken);
  }

  /**
   * Returns a Google ID token if the given ID token string is valid using
   * {@link #verify(GoogleIdToken, String)} with the {@link #getClientIds()}.
   *
   * @param idTokenString Google ID token string
   * @return Google ID token if verified successfully or {@code null} if failed
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link #verify(String, Collection, Collection)}
   */
  @Deprecated
  public GoogleIdToken verify(String idTokenString) throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, idTokenString);
    return verify(idToken) ? idToken : null;
  }

  /**
   * Returns a Google ID token if the given ID token string is verified using
   * {@link #verify(GoogleIdToken, Collection, Collection)}.
   *
   * @param idTokenString Google ID token string
   * @param issuees issues to allow or {@code null} to skip
   * @param audiences audiences to allow or {@code null} to skip
   * @return Google ID token if verified successfully or {@code null} if failed
   *
   * @since 1.14
   */
  public GoogleIdToken verify(
      String idTokenString, Collection<String> issuees, Collection<String> audiences)
      throws GeneralSecurityException, IOException {
    GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, idTokenString);
    return verify(idToken) ? idToken : null;
  }

  /**
   * Verifies that the given ID token is valid, using the given client ID.
   *
   * It verifies:
   *
   * <ul>
   * <li>The RS256 signature, which uses RSA and SHA-256 based on the public keys downloaded from
   * the public certificate endpoint.</li>
   * <li>The current time against the issued at and expiration time (allowing for a 5 minute clock
   * skew).</li>
   * <li>The issuer is {@code "accounts.google.com"}.</li>
   * <li>The audience and issuee match the client ID (skipped if {@code clientId} is {@code null}).
   * </li>
   * </ul>
   *
   * @param idToken Google ID token
   * @param clientId client ID or {@code null} to skip checking it
   * @return {@code true} if verified successfully or {@code false} if failed
   * @since 1.8
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link #verify(GoogleIdToken, Collection, Collection)}
   */
  @Deprecated
  public boolean verify(GoogleIdToken idToken, String clientId)
      throws GeneralSecurityException, IOException {
    return verify(
        clientId == null ? Collections.<String>emptySet() : Collections.singleton(clientId),
        idToken);
  }

  /**
   * Verifies that the given ID token is valid, using the given set of client IDs.
   *
   * It verifies:
   *
   * <ul>
   * <li>The RS256 signature, which uses RSA and SHA-256 based on the public keys downloaded from
   * the public certificate endpoint.</li>
   * <li>The current time against the issued at and expiration time (allowing for a 5 minute clock
   * skew).</li>
   * <li>The issuer is {@code "accounts.google.com"}.</li>
   * <li>The audience and issuee match one of the client IDs (skipped if {@code clientIds} is
   * {@code null}.</li>
   * <li>
   * </ul>
   *
   * @param idToken Google ID token
   * @param clientIds set of client IDs
   * @return {@code true} if verified successfully or {@code false} if failed
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use
   *             {@link #verify(GoogleIdToken, Collection, Collection)}
   */
  @Deprecated
  public boolean verify(Set<String> clientIds, GoogleIdToken idToken)
      throws GeneralSecurityException, IOException {
    Set<String> allowed = clientIds.isEmpty() ? null : clientIds;
    return verify(idToken, allowed, allowed);
  }

  /**
   * Verifies that the given ID token is valid using the cached public keys and a set of parameters.
   *
   * It verifies:
   *
   * <ul>
   * <li>The RS256 signature, which uses RSA and SHA-256 based on the public keys downloaded from
   * the public certificate endpoint.</li>
   * <li>The current time against the issued at and expiration time (allowing for a 5 minute clock
   * skew).</li>
   * <li>The issuer is {@code "accounts.google.com"}.</li>
   * <li>The audience is one of the specified audiences (skipped if {@code null}).</li>
   * <li>The issuee is one of the specified issuees (skipped if {@code null}). For some use cases,
   * this may be identical to the audiences.</li>
   * <li>
   * </ul>
   *
   * @param idToken Google ID token
   * @param issuees issues to allow or {@code null} to skip
   * @param audiences audiences to allow or {@code null} to skip
   * @return {@code true} if verified successfully or {@code false} if failed
   *
   * @since 1.14
   */
  public boolean verify(
      GoogleIdToken idToken, Collection<String> issuees, Collection<String> audiences)
      throws GeneralSecurityException, IOException {
    // check the payload
    GoogleIdToken.Payload payload = idToken.getPayload();
    if (!payload.isValidTime(300) || !"accounts.google.com".equals(payload.getIssuer())
        || issuees != null && !issuees.contains(payload.getIssuee()) || audiences != null
        && !audiences.contains(payload.getAudience())) {
      return false;
    }
    // check the signature
    JsonWebSignature.Header header = idToken.getHeader();
    String algorithm = header.getAlgorithm();
    if (algorithm.equals("RS256")) {
      lock.lock();
      try {
        // load public keys; expire 5 minutes (300 seconds) before actual expiration time
        if (publicKeys == null || clock.currentTimeMillis() + 300000 > expirationTimeMilliseconds) {
          loadPublicCerts();
        }
        Signature signer = SecurityUtils.getSha256WithRsaSignatureAlgorithm();
        for (PublicKey publicKey : publicKeys) {
          signer.initVerify(publicKey);
          signer.update(idToken.getSignedContentBytes());
          if (signer.verify(idToken.getSignatureBytes())) {
            return true;
          }
        }
      } finally {
        lock.unlock();
      }
    }
    return false;
  }

  /**
   * Downloads the public keys from the public certificates endpoint at
   * {@code "https://www.googleapis.com/oauth2/v1/certs"}.
   *
   * <p>
   * This method is automatically called if the public keys have not yet been initialized or if the
   * expiration time is very close, so normally this doesn't need to be called. Only call this
   * method explicitly to force the public keys to be updated.
   * </p>
   */
  public GoogleIdTokenVerifier loadPublicCerts() throws GeneralSecurityException, IOException {
    lock.lock();
    try {
      publicKeys = new ArrayList<PublicKey>();
      // HTTP request to public endpoint
      CertificateFactory factory = SecurityUtils.getX509CertificateFactory();
      HttpResponse certsResponse = transport.createRequestFactory()
          .buildGetRequest(new GenericUrl("https://www.googleapis.com/oauth2/v1/certs")).execute();
      // parse Cache-Control max-age parameter
      for (String arg : certsResponse.getHeaders().getCacheControl().split(",")) {
        Matcher m = MAX_AGE_PATTERN.matcher(arg);
        if (m.matches()) {
          expirationTimeMilliseconds = clock.currentTimeMillis() + Long.valueOf(m.group(1)) * 1000;
          break;
        }
      }
      // parse each public key in the JSON response
      JsonParser parser = jsonFactory.createJsonParser(certsResponse.getContent());
      JsonToken currentToken = parser.getCurrentToken();
      // token is null at start, so get next token
      if (currentToken == null) {
        currentToken = parser.nextToken();
      }
      Preconditions.checkArgument(currentToken == JsonToken.START_OBJECT);
      try {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          parser.nextToken();
          String certValue = parser.getText();
          X509Certificate x509Cert = (X509Certificate) factory.generateCertificate(
              new ByteArrayInputStream(StringUtils.getBytesUtf8(certValue)));
          publicKeys.add(x509Cert.getPublicKey());
        }
        publicKeys = Collections.unmodifiableList(publicKeys);
      } finally {
        parser.close();
      }
      return this;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Builder for {@link GoogleIdTokenVerifier}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.9
   */
  public static class Builder {

    /** HTTP transport. */
    final HttpTransport transport;

    /** JSON factory. */
    final JsonFactory jsonFactory;

    /** Set of Client IDs. */
    @Deprecated
    Set<String> clientIds = new HashSet<String>();

    /** Clock. */
    Clock clock = Clock.SYSTEM;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory) {
      this.transport = transport;
      this.jsonFactory = jsonFactory;
    }

    /** Builds a new instance of {@link GoogleIdTokenVerifier}. */
    public GoogleIdTokenVerifier build() {
      return new GoogleIdTokenVerifier(this);
    }

    /** Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return transport;
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Returns the set of client IDs.
     *
     * @deprecated (scheduled to be removed in 1.15) Use
     *             {@link #verify(GoogleIdToken, Collection, Collection)} or
     *             {@link #verify(String, Collection, Collection)}
     */
    @Deprecated
    public final Set<String> getClientIds() {
      return clientIds;
    }

    /**
     * Sets a list of client IDs.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @deprecated (scheduled to be removed in 1.15) Use
     *             {@link #verify(GoogleIdToken, Collection, Collection)} or
     *             {@link #verify(String, Collection, Collection)}
     */
    @Deprecated
    public Builder setClientIds(Iterable<String> clientIds) {
      this.clientIds.clear();
      for (String clientId : clientIds) {
        this.clientIds.add(clientId);
      }
      return this;
    }

    /**
     * Sets a list of client IDs.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @deprecated (scheduled to be removed in 1.15) Use
     *             {@link #verify(GoogleIdToken, Collection, Collection)} or
     *             {@link #verify(String, Collection, Collection)}
     */
    @Deprecated
    public Builder setClientIds(String... clientIds) {
      this.clientIds.clear();
      Collections.addAll(this.clientIds, clientIds);
      return this;
    }

    /**
     * Returns the clock.
     *
     * @since 1.14
     */
    public final Clock getClock() {
      return clock;
    }

    /**
     * Sets the clock.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.14
     */
    public Builder setClock(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
      return this;
    }
  }
}
