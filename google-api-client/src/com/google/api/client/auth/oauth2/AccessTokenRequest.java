/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.auth.oauth2;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.JsonHttpParser;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 * OAuth 2.0 request for an access token as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-5">Obtaining an Access Token</a>.
 * <p>
 * This class should be used directly only for the Client Credentials flow specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-07#section-2.4">Client Credentials Flow</a>.
 * <p>
 * The {@link #clientId} and {@link #clientSecret} fields are required. Call {@link #execute()} to
 * execute the request.
 * <p>
 * Sample usage for Client Credentials flow:
 *
 * <pre>
 * <code>static void requestAccessToken() throws IOException {
 *   try {
 *     AccessTokenRequest request = new AccessTokenRequest();
 *     request.clientId = CLIENT_ID;
 *     request.clientSecret = CLIENT_SECRET;
 *     AccessTokenResponse response =
 *         request.execute().parseAs(AccessTokenResponse.class);
 *     System.out.println("Access token: " + response.accessToken);
 *   } catch (HttpResponseException e) {
 *     AccessTokenErrorResponse response =
 *         e.response.parseAs(AccessTokenErrorResponse.class);
 *     System.out.println("Error: " + response.error);
 *   }
 * }</code>
 * </pre>
 *
 *  Other flows follow the same general approach, but instantiating a different class customized for
 * that flow with additional custom parameters.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class AccessTokenRequest extends GenericData {

  /** (REQUIRED) The client identifier. */
  @Key("client_id")
  public String clientId;

  /**
   * (REQUIRED if the client identifier has a matching secret) The client secret.
   */
  @Key("client_secret")
  public String clientSecret;

  /** Encoded authorization server URL. */
  public final String encodedAuthorizationServerUrl;

  AccessTokenRequest(String encodedAuthorizationServerUrl) {
    this.encodedAuthorizationServerUrl = encodedAuthorizationServerUrl;
  }

  /**
   * Executes request for an access token, and returns the HTTP response.
   *
   * @return HTTP response, which can then be parsed using {@link HttpResponse#parseAs(Class)} with
   *         {@link AccessTokenResponse}
   * @throws HttpResponseException for an HTTP error response, which can then be parsed using
   *         {@link HttpResponse#parseAs(Class)} on {@link HttpResponseException#response} using
   *         {@link AccessTokenErrorResponse}
   * @throws IOException I/O exception
   */
  public final HttpResponse execute() throws IOException {
    HttpTransport transport = new HttpTransport();
    transport.addParser(new JsonHttpParser());
    HttpRequest request = transport.buildPostRequest();
    request.setUrl(encodedAuthorizationServerUrl);
    UrlEncodedContent content = new UrlEncodedContent();
    content.data = this;
    request.content = content;
    return request.execute();
  }
}
