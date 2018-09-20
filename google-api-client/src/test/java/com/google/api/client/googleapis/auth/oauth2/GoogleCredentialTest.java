/*
 * Copyright 2013 Google Inc.
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

import com.google.api.client.googleapis.testing.auth.oauth2.MockTokenServerTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.util.SecurityTestUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleCredential}.
 *
 * @author Yaniv Inbar
 */
public class GoogleCredentialTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static final Collection<String> SCOPES =
      Collections.unmodifiableCollection(Arrays.asList("scope1", "scope2"));
  private static final Collection<String> EMPTY_SCOPES = Collections.emptyList();

  private static final String SA_KEY_TEXT = "-----BEGIN PRIVATE KEY-----\n"
      + "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALX0PQoe1igW12i"
      + "kv1bN/r9lN749y2ijmbc/mFHPyS3hNTyOCjDvBbXYbDhQJzWVUikh4mvGBA07qTj79Xc3yBDfKP2IeyYQIFe0t0"
      + "zkd7R9Zdn98Y2rIQC47aAbDfubtkU1U72t4zL11kHvoa0/RuFZjncvlr42X7be7lYh4p3NAgMBAAECgYASk5wDw"
      + "4Az2ZkmeuN6Fk/y9H+Lcb2pskJIXjrL533vrDWGOC48LrsThMQPv8cxBky8HFSEklPpkfTF95tpD43iVwJRB/Gr"
      + "CtGTw65IfJ4/tI09h6zGc4yqvIo1cHX/LQ+SxKLGyir/dQM925rGt/VojxY5ryJR7GLbCzxPnJm/oQJBANwOCO6"
      + "D2hy1LQYJhXh7O+RLtA/tSnT1xyMQsGT+uUCMiKS2bSKx2wxo9k7h3OegNJIu1q6nZ6AbxDK8H3+d0dUCQQDTrP"
      + "SXagBxzp8PecbaCHjzNRSQE2in81qYnrAFNB4o3DpHyMMY6s5ALLeHKscEWnqP8Ur6X4PvzZecCWU9BKAZAkAut"
      + "LPknAuxSCsUOvUfS1i87ex77Ot+w6POp34pEX+UWb+u5iFn2cQacDTHLV1LtE80L8jVLSbrbrlH43H0DjU5AkEA"
      + "gidhycxS86dxpEljnOMCw8CKoUBd5I880IUahEiUltk7OLJYS/Ts1wbn3kPOVX3wyJs8WBDtBkFrDHW2ezth2QJ"
      + "ADj3e1YhMVdjJW5jqwlD/VNddGjgzyunmiZg0uOXsHXbytYmsA545S8KRQFaJKFXYYFo2kOjqOiC1T2cAzMDjCQ"
      + "==\n-----END PRIVATE KEY-----\n";
  private static final String SA_KEY_ID = "key_id";

  public void testRefreshToken_ServiceAccounts() throws Exception {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();

    assertTrue(credential.refreshToken());
    assertEquals(accessToken, credential.getAccessToken());
  }

  public void testRefreshToken_User() throws Exception {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxN3PuGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setClientSecrets(clientId, clientSecret)
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    credential.setRefreshToken(refreshToken);

    assertTrue(credential.refreshToken());
    assertEquals(accessToken, credential.getAccessToken());
  }

  public void testCreateScoped() throws Exception {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(EMPTY_SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    assertTrue(credential.createScopedRequired());
    try {
      credential.refreshToken();
      fail("Should not be able to refresh token without scopes.");
    } catch (Exception expected) {
    }

    GoogleCredential scopedCredential = credential.createScoped(SCOPES);
    assertFalse(scopedCredential.createScopedRequired());
    assertNotSame(credential, scopedCredential);

    assertTrue(scopedCredential.refreshToken());
    assertEquals(accessToken, scopedCredential.getAccessToken());

    assertSame(credential.getTransport(), scopedCredential.getTransport());
    assertSame(credential.getJsonFactory(), scopedCredential.getJsonFactory());
    assertSame(credential.getServiceAccountId(), scopedCredential.getServiceAccountId());
    assertSame(credential.getServiceAccountUser(), scopedCredential.getServiceAccountUser());
    assertSame(credential.getServiceAccountPrivateKey(),
        scopedCredential.getServiceAccountPrivateKey());
  }

  public void testCreateScopesNotSet() throws Exception {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    // Note that setServiceAccountScopes() is not called, so it is uninitialized (i.e. null) on the
    // builder.
    assertTrue(credential.getServiceAccountScopes().isEmpty());
  }

  public void testGetApplicationDefaultNullTransportThrows() throws IOException {
    try {
      GoogleCredential.getApplicationDefault(null, JSON_FACTORY);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testGetApplicationDefaultNullJsonFactoryThrows() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    try {
      GoogleCredential.getApplicationDefault(transport, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testFromStreamNullTransportThrows() throws IOException {
    InputStream stream = new ByteArrayInputStream("foo".getBytes());
    try {
      GoogleCredential.fromStream(stream, null, JSON_FACTORY);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testFromStreamNullJsonFactoryThrows() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    InputStream stream = new ByteArrayInputStream("foo".getBytes());
    try {
      GoogleCredential.fromStream(stream, transport, null);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testFromStreamNullStreamThrows() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    try {
      GoogleCredential.fromStream(null, transport, JSON_FACTORY);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testFromStreamServiceAccount() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", serviceAccountId);
    serviceAccountContents.put("client_email", serviceAccountEmail);
    serviceAccountContents.put("private_key", SA_KEY_TEXT);
    serviceAccountContents.put("private_key_id", SA_KEY_ID);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    GoogleCredential defaultCredential = GoogleCredential
        .fromStream(serviceAccountStream, transport, JSON_FACTORY);
    assertNotNull(defaultCredential);
    defaultCredential = defaultCredential.createScoped(SCOPES);

    assertTrue(defaultCredential.refreshToken());
    assertEquals(accessToken, defaultCredential.getAccessToken());
  }

  public void testFromStreamServiceAccountAlternateTokenUri() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    final String tokenServerUrl = "http://another.auth.com/token";
    MockTokenServerTransport transport = new MockTokenServerTransport(tokenServerUrl);
    transport.addServiceAccount(serviceAccountEmail, accessToken);

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", serviceAccountId);
    serviceAccountContents.put("client_email", serviceAccountEmail);
    serviceAccountContents.put("private_key", SA_KEY_TEXT);
    serviceAccountContents.put("private_key_id", SA_KEY_ID);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    serviceAccountContents.put("token_uri", tokenServerUrl);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    GoogleCredential defaultCredential = GoogleCredential
        .fromStream(serviceAccountStream, transport, JSON_FACTORY);
    assertNotNull(defaultCredential);
    assertEquals(tokenServerUrl, defaultCredential.getTokenServerEncodedUrl());
    defaultCredential = defaultCredential.createScoped(SCOPES);
    assertEquals(tokenServerUrl, defaultCredential.getTokenServerEncodedUrl());

    assertTrue(defaultCredential.refreshToken());
    assertEquals(accessToken, defaultCredential.getAccessToken());
  }

  public void testFromStreamServiceAccountMissingClientIdThrows() throws IOException {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_email", serviceAccountEmail);
    serviceAccountContents.put("private_key", SA_KEY_TEXT);
    serviceAccountContents.put("private_key_id", SA_KEY_ID);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(serviceAccountStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_id"));
    }
  }

  public void testFromStreamServiceAccountMissingClientEmailThrows() throws IOException {
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", serviceAccountId);
    serviceAccountContents.put("private_key", SA_KEY_TEXT);
    serviceAccountContents.put("private_key_id", SA_KEY_ID);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(serviceAccountStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_email"));
    }
  }

  public void testFromStreamServiceAccountMissingPrivateKeyThrows() throws IOException {
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", serviceAccountId);
    serviceAccountContents.put("client_email", serviceAccountEmail);
    serviceAccountContents.put("private_key_id", SA_KEY_ID);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(serviceAccountStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("private_key"));
    }
  }

  public void testFromStreamServiceAccountMissingPrivateKeyIdThrows() throws IOException {
    final String serviceAccountId =
        "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String serviceAccountEmail =
        "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", serviceAccountId);
    serviceAccountContents.put("client_email", serviceAccountEmail);
    serviceAccountContents.put("private_key", SA_KEY_TEXT);
    serviceAccountContents.put("type", GoogleCredential.SERVICE_ACCOUNT_FILE_TYPE);
    String json = serviceAccountContents.toPrettyString();
    InputStream serviceAccountStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(serviceAccountStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("private_key_id"));
    }
  }

  public void testFromStreamUser() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId =
        "ya29.1.AADtN_UtlxN3PSc5yhCqfA9nDFp1dfvH8cruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    // Create user stream.
    String json = createUserJson(clientId, clientSecret, refreshToken);
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    GoogleCredential defaultCredential = GoogleCredential
        .fromStream(userStream, transport, JSON_FACTORY);

    assertNotNull(defaultCredential);
    assertEquals(refreshToken, defaultCredential.getRefreshToken());

    assertTrue(defaultCredential.refreshToken());
    assertEquals(accessToken, defaultCredential.getAccessToken());
  }

  public void testFromStreamUsertMissingClientIdThrows() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    // Write out user file
    String json = createUserJson(null, clientSecret, refreshToken);
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_id"));
    }
  }

  public void testFromStreamUsertMissingClientSecretThrows() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    // Write out user file
    String json = createUserJson(clientId, null, refreshToken);
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_secret"));
    }
  }

  public void testFromStreamUsertMissingRefreshTokenThrows() throws IOException {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    // Write out user file
    String json = createUserJson(clientId, clientSecret, null);
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("refresh_token"));
    }
  }

  public void testCreateDelegated() throws Exception {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String delegateUser = "user@domain.com";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();

    assertNotSame(delegateUser, credential.getServiceAccountUser());

    GoogleCredential delegatedCredential = credential.createDelegated(delegateUser);
    assertNotSame(credential, delegatedCredential);

    assertTrue(delegatedCredential.refreshToken());
    assertEquals(accessToken, delegatedCredential.getAccessToken());

    assertNotSame(credential.getServiceAccountUser(), delegatedCredential.getServiceAccountUser());

    assertSame(credential.getTransport(), delegatedCredential.getTransport());
    assertSame(credential.getJsonFactory(), delegatedCredential.getJsonFactory());
    assertSame(credential.getServiceAccountId(), delegatedCredential.getServiceAccountId());
    assertSame(credential.getServiceAccountPrivateKey(),
        delegatedCredential.getServiceAccountPrivateKey());
  }

  public void testBuilderUserAccount() throws Exception {
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String clientSecret = "jakuaL9YyieakhECKL2SwZcu";
    final String clientId = "ya29.1.AADtN_UtlxN3PuGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(clientId, clientSecret);
    transport.addRefreshToken(refreshToken, accessToken);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setClientSecrets(clientId, clientSecret)
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    credential.setRefreshToken(refreshToken);

    assertTrue(credential.refreshToken());
    assertEquals(accessToken, credential.getAccessToken());

    GoogleCredential newCredential = credential.toBuilder().build();

    assertNotSame(credential, newCredential);

    assertSame(credential.getClientAuthentication(), newCredential.getClientAuthentication());

    assertEquals(credential.getTransport(), newCredential.getTransport());
    assertEquals(credential.getJsonFactory(), newCredential.getJsonFactory());
  }

  public void testBuilderServiceAccount() throws Exception {
    final String serviceAccountEmail =
        "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String accessToken = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String refreshToken = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";
    final String delegateUser = "user@domain.com";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(serviceAccountEmail, accessToken);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(serviceAccountEmail)
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setServiceAccountUser(delegateUser)
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    assertTrue(credential.refreshToken());

    GoogleCredential newCredential = credential.toBuilder().build();

    assertNotSame(credential, newCredential);

    assertEquals(credential.getServiceAccountId(),
        newCredential.getServiceAccountId());

    assertEquals(credential.getServiceAccountProjectId(),
        newCredential.getServiceAccountProjectId());

    org.junit.Assert.assertArrayEquals(
        credential.getServiceAccountScopes().toArray(),
        newCredential.getServiceAccountScopes().toArray());

    assertEquals(credential.getServiceAccountPrivateKey(),
        newCredential.getServiceAccountPrivateKey());

    assertEquals(credential.getServiceAccountPrivateKeyId(),
        newCredential.getServiceAccountPrivateKeyId());

    assertEquals(credential.getServiceAccountUser(),
       newCredential.getServiceAccountUser());

    assertTrue(newCredential.refreshToken());
    assertEquals(credential.getAccessToken(), newCredential.getAccessToken());

    assertEquals(credential.getTransport(), newCredential.getTransport());
    assertEquals(credential.getJsonFactory(), newCredential.getJsonFactory());
  }


  static String createUserJson(String clientId, String clientSecret, String refreshToken)
      throws IOException {
    GenericJson userCredentialContents = new GenericJson();
    userCredentialContents.setFactory(JSON_FACTORY);
    if (clientId != null) {
      userCredentialContents.put("client_id", clientId);
    }
    if (clientSecret != null) {
      userCredentialContents.put("client_secret", clientSecret);
    }
    if (refreshToken != null) {
      userCredentialContents.put("refresh_token", refreshToken);
    }
    userCredentialContents.put("type", GoogleCredential.USER_FILE_TYPE);
    String json = userCredentialContents.toPrettyString();
    return json;
  }
}
