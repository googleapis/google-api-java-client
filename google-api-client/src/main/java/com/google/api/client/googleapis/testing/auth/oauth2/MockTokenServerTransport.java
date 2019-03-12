/*
 * Copyright 2014 Google Inc.
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

package com.google.api.client.googleapis.testing.auth.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.testing.TestUtils;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Beta;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * {@link Beta} <br/>
 * A test transport that simulates Google's token server for refresh tokens and service accounts.
 *
 * @since 1.19
 */
@Beta
public class MockTokenServerTransport extends MockHttpTransport {
  /** Old URL of Google's token server (for backwards compatibility) */
  private static final String LEGACY_TOKEN_SERVER_URL =
      "https://accounts.google.com/o/oauth2/token";

  private static final Logger LOGGER = Logger.getLogger(MockTokenServerTransport.class.getName());

  static final String EXPECTED_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  final String tokenServerUrl;
  Map<String, String> serviceAccounts = new HashMap<String, String>();
  Map<String, String> clients = new HashMap<String, String>();
  Map<String, String> refreshTokens = new HashMap<String, String>();

  public MockTokenServerTransport() {
    this(GoogleOAuthConstants.TOKEN_SERVER_URL);
  }

  public MockTokenServerTransport(String tokenServerUrl) {
    this.tokenServerUrl = tokenServerUrl;
  }

  public void addServiceAccount(String email, String accessToken) {
    serviceAccounts.put(email, accessToken);
  }

  public void addClient(String clientId, String clientSecret) {
    clients.put(clientId, clientSecret);
  }

  public void addRefreshToken(String refreshToken, String accessTokenToReturn) {
    refreshTokens.put(refreshToken, accessTokenToReturn);
  }

  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    if (url.equals(tokenServerUrl)) {
      return buildTokenRequest(url);
    } else if (url.equals(LEGACY_TOKEN_SERVER_URL)) {
      LOGGER.warning("Your configured token_uri is using a legacy endpoint. You may want to "
          + "redownload your credentials.");
      return buildTokenRequest(url);
    }
    return super.buildRequest(method, url);
  }

  private MockLowLevelHttpRequest buildTokenRequest(String url) {
    return new MockLowLevelHttpRequest(url) {
      @Override
      public LowLevelHttpResponse execute() throws IOException {
        String content = this.getContentAsString();
        Map<String, String> query = TestUtils.parseQuery(content);
        String accessToken = null;

        String foundId = query.get("client_id");
        if (foundId != null) {
          if (!clients.containsKey(foundId)) {
            throw new IOException("Client ID not found.");
          }
          String foundSecret = query.get("client_secret");
          String expectedSecret = clients.get(foundId);
          if (foundSecret == null || !foundSecret.equals(expectedSecret)) {
            throw new IOException("Client secret not found.");
          }
          String foundRefresh = query.get("refresh_token");
          if (!refreshTokens.containsKey(foundRefresh)) {
            throw new IOException("Refresh Token not found.");
          }
          accessToken = refreshTokens.get(foundRefresh);
        } else if (query.containsKey("grant_type")) {
          String grantType = query.get("grant_type");
          if (!EXPECTED_GRANT_TYPE.equals(grantType)) {
            throw new IOException("Unexpected Grant Type.");
          }
          String assertion = query.get("assertion");
          JsonWebSignature signature = JsonWebSignature.parse(JSON_FACTORY, assertion);
          String foundEmail = signature.getPayload().getIssuer();
          if (!serviceAccounts.containsKey(foundEmail)) {
            throw new IOException("Service Account Email not found as issuer.");
          }
          accessToken = serviceAccounts.get(foundEmail);
          String foundScopes = (String) signature.getPayload().get("scope");
          if (foundScopes == null || foundScopes.length() == 0) {
            throw new IOException("Scopes not found.");
          }
        } else {
          throw new IOException("Unknown token type.");
        }

        // Create the JSon response
        GenericJson refreshContents = new GenericJson();
        refreshContents.setFactory(JSON_FACTORY);
        refreshContents.put("access_token", accessToken);
        refreshContents.put("expires_in", 3600);
        refreshContents.put("token_type", "Bearer");
        String refreshText  = refreshContents.toPrettyString();

        MockLowLevelHttpResponse response = new MockLowLevelHttpResponse()
            .setContentType(Json.MEDIA_TYPE)
            .setContent(refreshText);
        return response;
      }
    };
  }
}
