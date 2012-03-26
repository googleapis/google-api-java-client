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
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.JsonToken;
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
import java.util.Collections;
import java.util.List;
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
 * {@link #verify(GoogleIdToken)} or {@link GoogleIdToken#verify(GoogleIdTokenVerifier)} to verify a
 * Google ID token.
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
    verifier = new GoogleIdTokenVerifier(transport, jsonFactory, clientId);
  }

  public static boolean verifyToken(GoogleIdToken idToken)
      throws GeneralSecurityException, IOException {
    return verifier.verify(idToken);
  }
 * </pre>
 * @since 1.7
 */
public final class GoogleIdTokenVerifier {

  /** Pattern for the max-age header element of Cache-Control. */
  private static final Pattern MAX_AGE_PATTERN = Pattern.compile("\\s*max-age\\s*=\\s*(\\d+)\\s*");

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Public keys or {@code null} for none. */
  private List<PublicKey> publicKeys;

  /**
   * Expiration time in milliseconds to be used with {@link System#currentTimeMillis()} or {@code 0}
   * for none.
   */
  private long expirationTimeMilliseconds;

  /** Client ID or {@code null} for none. */
  private final String clientId;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** Lock on the public keys. */
  private final Lock lock = new ReentrantLock();

  /**
   * Constructor with required parameters.
   *
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param clientId client ID or {@code null} for none
   */
  public GoogleIdTokenVerifier(HttpTransport transport, JsonFactory jsonFactory, String clientId) {
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.clientId = clientId;
  }

  /** Returns the JSON factory. */
  public JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the client ID or {@code null} for none. */
  public String getClientId() {
    return clientId;
  }

  /** Returns the public keys or {@code null} for none. */
  public List<PublicKey> getPublicKeys() {
    return Collections.unmodifiableList(publicKeys);
  }

  /**
   * Returns the expiration time in milliseconds to be used with {@link System#currentTimeMillis()}
   * or {@code 0} for none.
   */
  public long getExpirationTimeMilliseconds() {
    return expirationTimeMilliseconds;
  }

  /**
   * Verifies that the given ID token is valid using {@link #verify(GoogleIdToken, String)} with the
   * {@link #getClientId()}.
   *
   * @param idToken Google ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   */
  public boolean verify(GoogleIdToken idToken) throws GeneralSecurityException, IOException {
    return verify(idToken, clientId);
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
   * <li>The audience and issuee match the client ID (skipped if {@code clientId} is {@code null}.
   * </li>
   * <li>
   * </ul>
   *
   * @param idToken Google ID token
   * @param clientId client ID or {@code null} to skip checking it
   * @return {@code true} if verified successfully or {@code false} if failed
   * @since 1.8
   */
  public boolean verify(GoogleIdToken idToken, String clientId)
      throws GeneralSecurityException, IOException {
    // check the payload
    GoogleIdToken.Payload payload = idToken.getPayload();
    if (!payload.isValidTime(300) || !"accounts.google.com".equals(payload.getIssuer())
        || clientId != null
        && (!clientId.equals(payload.getAudience()) || !clientId.equals(payload.getIssuee()))) {
      return false;
    }
    // check the signature
    JsonWebSignature.Header header = idToken.getHeader();
    String algorithm = header.getAlgorithm();
    if (algorithm.equals("RS256")) {
      lock.lock();
      try {
        // load public keys; expire 5 minutes (300 seconds) before actual expiration time
        if (publicKeys == null
            || System.currentTimeMillis() + 300000 > expirationTimeMilliseconds) {
          loadPublicCerts();
        }
        Signature signer = Signature.getInstance("SHA256withRSA");
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
      CertificateFactory factory = CertificateFactory.getInstance("X509");
      HttpResponse certsResponse = transport.createRequestFactory().buildGetRequest(
          new GenericUrl("https://www.googleapis.com/oauth2/v1/certs")).execute();
      // parse Cache-Control max-age parameter
      for (String arg : certsResponse.getHeaders().getCacheControl().split(",")) {
        Matcher m = MAX_AGE_PATTERN.matcher(arg);
        if (m.matches()) {
          expirationTimeMilliseconds = System.currentTimeMillis() + Long.valueOf(m.group(1)) * 1000;
          break;
        }
      }
      // parse each public key in the JSON response
      JsonParser parser = JsonHttpParser.parserForResponse(jsonFactory, certsResponse);
      try {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          parser.nextToken();
          String certValue = parser.getText();
          X509Certificate x509Cert = (X509Certificate) factory.generateCertificate(
              new ByteArrayInputStream(StringUtils.getBytesUtf8(certValue)));
          publicKeys.add(x509Cert.getPublicKey());
        }
      } finally {
        parser.close();
      }
      return this;
    } finally {
      lock.unlock();
    }
  }
}
