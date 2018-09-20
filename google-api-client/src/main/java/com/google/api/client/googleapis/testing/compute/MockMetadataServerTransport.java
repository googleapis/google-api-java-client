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

package com.google.api.client.googleapis.testing.compute;

import com.google.api.client.googleapis.auth.oauth2.OAuth2Utils;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.Beta;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Transport that simulates the GCE metadata server for access tokens.
 *
 * @since 1.19
 */
@Beta
public class MockMetadataServerTransport extends MockHttpTransport {

  private static final String METADATA_SERVER_URL = OAuth2Utils.getMetadataServerUrl();

  private static final String METADATA_TOKEN_SERVER_URL =
      METADATA_SERVER_URL + "/computeMetadata/v1/instance/service-accounts/default/token";

  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  String accessToken;

  Integer tokenRequestStatusCode;

  public MockMetadataServerTransport(String accessToken) {
    this.accessToken = accessToken;
  }

  public void setTokenRequestStatusCode(Integer tokenRequestStatusCode) {
    this.tokenRequestStatusCode = tokenRequestStatusCode;
  }

  @Override
  public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
    if (url.equals(METADATA_TOKEN_SERVER_URL)) {

      MockLowLevelHttpRequest request = new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() throws IOException {

          if (tokenRequestStatusCode != null) {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse()
              .setStatusCode(tokenRequestStatusCode)
              .setContent("Token Fetch Error");
            return response;
          }

          String metadataRequestHeader = getFirstHeaderValue("Metadata-Flavor");
          if (!"Google".equals(metadataRequestHeader)) {
            throw new IOException("Metadata request header not found.");
          }

          // Create the JSon response
          GenericJson refreshContents = new GenericJson();
          refreshContents.setFactory(JSON_FACTORY);
          refreshContents.put("access_token", accessToken);
          refreshContents.put("expires_in", 3600000);
          refreshContents.put("token_type", "Bearer");
          String refreshText  = refreshContents.toPrettyString();

          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse()
            .setContentType(Json.MEDIA_TYPE)
            .setContent(refreshText);
          return response;

        }
      };
      return request;
    } else if (url.equals(METADATA_SERVER_URL)) {
      MockLowLevelHttpRequest request = new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.addHeader("Metadata-Flavor", "Google");
          return response;
        }
      };
      return request;
    }
    return super.buildRequest(method, url);
  }
}
