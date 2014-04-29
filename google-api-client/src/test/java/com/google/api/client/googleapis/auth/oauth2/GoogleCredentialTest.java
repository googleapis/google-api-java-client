/*
 * Copyright (c) 2013 Google Inc.
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

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.util.SecurityTestUtils;
import com.google.api.client.util.Joiner;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Tests {@link GoogleCredential}.
 *
 * @author Yaniv Inbar
 */
public class GoogleCredentialTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private static final Collection<String> SCOPES = Arrays.asList("scope1", "scope2");
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
    final String SA_EMAIL= "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(SA_EMAIL, ACCESS_TOKEN);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(SA_EMAIL)
        .setServiceAccountScopes(SCOPES)
        .setServiceAccountPrivateKey(SecurityTestUtils.newRsaPrivateKey())
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();

    assertTrue(credential.refreshToken());
    assertEquals(ACCESS_TOKEN, credential.getAccessToken());
  }

  public void testRefreshToken_User() throws Exception {
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PuGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    GoogleCredential credential = new GoogleCredential.Builder()
        .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .build();
    credential.setRefreshToken(REFRESH_TOKEN);

    assertTrue(credential.refreshToken());
    assertEquals(ACCESS_TOKEN, credential.getAccessToken());
  }

  public void testCreateScoped() throws Exception {
    final String SA_EMAIL= "36680232662-vrd7ji19q3ne0ah2csanun6bnr@developer.gserviceaccount.com";
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(SA_EMAIL, ACCESS_TOKEN);
    GoogleCredential credential = new GoogleCredential.Builder()
        .setServiceAccountId(SA_EMAIL)
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
    assertEquals(ACCESS_TOKEN, scopedCredential.getAccessToken());

    assertSame(credential.getTransport(), scopedCredential.getTransport());
    assertSame(credential.getJsonFactory(), scopedCredential.getJsonFactory());
    assertSame(credential.getServiceAccountId(), scopedCredential.getServiceAccountId());
    assertSame(credential.getServiceAccountUser(), scopedCredential.getServiceAccountUser());
    assertSame(credential.getServiceAccountPrivateKey(),
        scopedCredential.getServiceAccountPrivateKey());
  }

  public void testGetDefaultNullTransportThrows() throws IOException {
    try {
      GoogleCredential.getDefault(null, JSON_FACTORY);
      fail();
    } catch (NullPointerException expected) {
    }
  }

  public void testGetDefaultNullJsonFactoryThrows() throws IOException {
    HttpTransport transport = new MockHttpTransport();
    try {
      GoogleCredential.getDefault(transport, null);
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
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String SA_ID = "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String SA_EMAIL= "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addServiceAccount(SA_EMAIL, ACCESS_TOKEN);

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", SA_ID);
    serviceAccountContents.put("client_email", SA_EMAIL);
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
    assertEquals(ACCESS_TOKEN, defaultCredential.getAccessToken());
  }

  public void testFromStreamServiceAccountMissingClientIdThrows() throws IOException {
    final String SA_EMAIL= "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_email", SA_EMAIL);
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
    final String SA_ID = "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", SA_ID);
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
    final String SA_ID = "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String SA_EMAIL= "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", SA_ID);
    serviceAccountContents.put("client_email", SA_EMAIL);
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
    final String SA_ID = "36680232662-vrd7ji19qe3nelgchd0ah2csanun6bnr.apps.googleusercontent.com";
    final String SA_EMAIL= "36680232662-vrd7ji19qgchd0ah2csanun6bnr@developer.gserviceaccount.com";

    MockHttpTransport transport = new MockTokenServerTransport();

    // Write out user file
    GenericJson serviceAccountContents = new GenericJson();
    serviceAccountContents.setFactory(JSON_FACTORY);
    serviceAccountContents.put("client_id", SA_ID);
    serviceAccountContents.put("client_email", SA_EMAIL);
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
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID =
        "ya29.1.AADtN_UtlxN3PSc5yhCqfA9nDFp1dfvH8cruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    // Create user stream.
    GenericJson userCredentialContents = new GenericJson();
    userCredentialContents.setFactory(JSON_FACTORY);
    userCredentialContents.put("client_id", CLIENT_ID);
    userCredentialContents.put("client_secret", CLIENT_SECRET);
    userCredentialContents.put("refresh_token", REFRESH_TOKEN);
    String scopesAsString = Joiner.on(' ').join(SCOPES);
    userCredentialContents.put("scopes", scopesAsString);
    userCredentialContents.put("type", GoogleCredential.USER_FILE_TYPE);
    String json = userCredentialContents.toPrettyString();
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    GoogleCredential defaultCredential = GoogleCredential
        .fromStream(userStream, transport, JSON_FACTORY);

    assertNotNull(defaultCredential);
    assertEquals(defaultCredential.getRefreshToken(), REFRESH_TOKEN);

    assertTrue(defaultCredential.refreshToken());
    assertEquals(ACCESS_TOKEN, defaultCredential.getAccessToken());
  }

  public void testFromStreamUsertMissingClientIdThrows() throws IOException {
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    // Write out user file
    GenericJson userCredentialContents = new GenericJson();
    userCredentialContents.setFactory(JSON_FACTORY);
    userCredentialContents.put("client_secret", CLIENT_SECRET);
    userCredentialContents.put("refresh_token", REFRESH_TOKEN);
    String scopesAsString = Joiner.on(' ').join(SCOPES);
    userCredentialContents.put("scopes", scopesAsString);
    userCredentialContents.put("type", GoogleCredential.USER_FILE_TYPE);
    String json = userCredentialContents.toPrettyString();
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_id"));
    }
  }

  public void testFromStreamUsertMissingClientSecretThrows() throws IOException {
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    // Write out user file
    GenericJson userCredentialContents = new GenericJson();
    userCredentialContents.setFactory(JSON_FACTORY);
    userCredentialContents.put("client_id", CLIENT_ID);
    userCredentialContents.put("refresh_token", REFRESH_TOKEN);
    String scopesAsString = Joiner.on(' ').join(SCOPES);
    userCredentialContents.put("scopes", scopesAsString);
    userCredentialContents.put("type", GoogleCredential.USER_FILE_TYPE);
    String json = userCredentialContents.toPrettyString();
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("client_secret"));
    }
  }

  public void testFromStreamUsertMissingRefreshTokenThrows() throws IOException {
    final String ACCESS_TOKEN = "1/MkSJoj1xsli0AccessToken_NKPY2";
    final String CLIENT_SECRET = "jakuaL9YyieakhECKL2SwZcu";
    final String CLIENT_ID = "ya29.1.AADtN_UtlxN3PSruGAxrN2XQnZTVRvDyVWnYq4I6dws";
    final String REFRESH_TOKEN = "1/Tl6awhpFjkMkSJoj1xsli0H2eL5YsMgU_NKPY2TyGWY";

    MockTokenServerTransport transport = new MockTokenServerTransport();
    transport.addClient(CLIENT_ID, CLIENT_SECRET);
    transport.addRefreshToken(REFRESH_TOKEN, ACCESS_TOKEN);

    // Write out user file
    GenericJson userCredentialContents = new GenericJson();
    userCredentialContents.setFactory(JSON_FACTORY);
    userCredentialContents.put("client_id", CLIENT_ID);
    String scopesAsString = Joiner.on(' ').join(SCOPES);
    userCredentialContents.put("scopes", scopesAsString);
    userCredentialContents.put("type", GoogleCredential.USER_FILE_TYPE);
    String json = userCredentialContents.toPrettyString();
    InputStream userStream = new ByteArrayInputStream(json.getBytes());

    try {
      GoogleCredential.fromStream(userStream, transport, JSON_FACTORY);
      fail();
    } catch (IOException expected) {
      assertTrue(expected.getMessage().contains("refresh_token"));
    }
  }
}
