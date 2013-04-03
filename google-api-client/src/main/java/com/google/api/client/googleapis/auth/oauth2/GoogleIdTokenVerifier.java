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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link Beta} <br/>
 * Thread-safe Google ID token verifier.
 *
 * <p>
 * The public keys are loaded from the public certificates endpoint at
 * {@link #getPublicCertsEncodedUrl} and cached in this instance. Therefore, for maximum efficiency,
 * applications should use a single globally-shared instance of the {@link GoogleIdTokenVerifier}.
 * </p>
 *
 * <p>
 * Use {@link #verify(GoogleIdToken)} to verify a Google ID token, and then
 * {@link GoogleIdToken#verifyAudience} to verify the client ID. <br/>
 * Samples usage:
 * </p>
 *
 * <pre>
  public static GoogleIdTokenVerifier verifier;

  public static void initVerifier(HttpTransport transport, JsonFactory jsonFactory) {
    verifier = new GoogleIdTokenVerifier(transport, jsonFactory);
  }

  public static boolean verifyToken(GoogleIdToken idToken, Collection<String> trustedClientIds)
      throws GeneralSecurityException, IOException {
    return verifier.verify(idToken) && idToken.verifyAudience(trustedClientIds);
  }
 * </pre>
 * @since 1.7
 */
@Beta
public class GoogleIdTokenVerifier {

  /** Pattern for the max-age header element of Cache-Control. */
  private static final Pattern MAX_AGE_PATTERN = Pattern.compile("\\s*max-age\\s*=\\s*(\\d+)\\s*");

  /** Seconds of time skew to accept. */
  private static final long TIME_SKEW_SECONDS = 300;

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Public keys or {@code null} for none. */
  private List<PublicKey> publicKeys;

  /**
   * Expiration time in milliseconds to be used with {@link Clock#currentTimeMillis()} or {@code 0}
   * for none.
   */
  private long expirationTimeMilliseconds;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** Lock on the public keys. */
  private final Lock lock = new ReentrantLock();

  /** Clock to use for expiration checks. */
  private final Clock clock;

  /** Public certificates encoded URL. */
  private final String publicCertsEncodedUrl;

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
    transport = builder.transport;
    jsonFactory = builder.jsonFactory;
    clock = builder.clock;
    publicCertsEncodedUrl = builder.publicCertsEncodedUrl;
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
   * Returns the public certificates encoded URL.
   *
   * @since 1.15
   */
  public final String getPublicCertsEncodedUrl() {
    return publicCertsEncodedUrl;
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
   * Verifies that the given ID token is valid using the cached public keys.
   *
   * It verifies:
   *
   * <ul>
   * <li>The RS256 signature, which uses RSA and SHA-256 based on the public keys downloaded from
   * the public certificate endpoint.</li>
   * <li>The current time against the issued at and expiration time (allowing for a 5 minute clock
   * skew).</li>
   * <li>The issuer is {@code "accounts.google.com"}.</li>
   * </ul>
   *
   * @param idToken Google ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   */
  public boolean verify(GoogleIdToken idToken) throws GeneralSecurityException, IOException {
    // check the payload
    if (!idToken.verifyIssuer("accounts.google.com")
        || !idToken.verifyTime(clock.currentTimeMillis(), TIME_SKEW_SECONDS)) {
      return false;
    }
    // verify signature
    lock.lock();
    try {
      // load public keys; expire 5 minutes (300 seconds) before actual expiration time
      if (publicKeys == null
          || clock.currentTimeMillis() + TIME_SKEW_SECONDS * 1000 > expirationTimeMilliseconds) {
        loadPublicCerts();
      }
      for (PublicKey publicKey : publicKeys) {
        if (idToken.verifySignature(publicKey)) {
          return true;
        }
      }
    } finally {
      lock.unlock();
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
    GoogleIdToken idToken = GoogleIdToken.parse(jsonFactory, idTokenString);
    return verify(idToken) ? idToken : null;
  }

  /**
   * Downloads the public keys from the public certificates endpoint at
   * {@link #getPublicCertsEncodedUrl}.
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
          .buildGetRequest(new GenericUrl(publicCertsEncodedUrl)).execute();
      expirationTimeMilliseconds =
          clock.currentTimeMillis() + getCacheTimeInSec(certsResponse.getHeaders()) * 1000;
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
   * Gets the cache time in seconds. "max-age" in "Cache-Control" header and "Age" header are
   * considered.
   *
   * @param httpHeaders the http header of the response
   * @return the cache time in seconds or zero if the response should not be cached
   */
  long getCacheTimeInSec(HttpHeaders httpHeaders) {
    long cacheTimeInSec = 0;
    if (httpHeaders.getCacheControl() != null) {
      for (String arg : httpHeaders.getCacheControl().split(",")) {
        Matcher m = MAX_AGE_PATTERN.matcher(arg);
        if (m.matches()) {
          cacheTimeInSec = Long.valueOf(m.group(1));
          break;
        }
      }
    }
    if (httpHeaders.getAge() != null) {
      cacheTimeInSec -= httpHeaders.getAge();
    }
    return Math.max(0, cacheTimeInSec);
  }

  /**
   * {@link Beta} <br/>
   * Builder for {@link GoogleIdTokenVerifier}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.9
   */
  @Beta
  public static class Builder {

    /** HTTP transport. */
    final HttpTransport transport;

    /** JSON factory. */
    final JsonFactory jsonFactory;

    /** Public certificates encoded URL. */
    String publicCertsEncodedUrl = GoogleOAuthConstants.DEFAULT_PUBLIC_CERTS_ENCODED_URL;

    /** Clock. */
    Clock clock = Clock.SYSTEM;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory) {
      this.transport = Preconditions.checkNotNull(transport);
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
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
     * Returns the public certificates encoded URL.
     *
     * @since 1.15
     */
    public final String getPublicCertsEncodedUrl() {
      return publicCertsEncodedUrl;
    }

    /**
     * Sets the public certificates encoded URL.
     *
     * <p>
     * The default value is {@link GoogleOAuthConstants#DEFAULT_PUBLIC_CERTS_ENCODED_URL}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.15
     */
    public Builder setPublicCertsEncodedUrl(String publicCertsEncodedUrl) {
      this.publicCertsEncodedUrl = Preconditions.checkNotNull(publicCertsEncodedUrl);
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
