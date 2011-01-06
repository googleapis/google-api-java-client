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

package com.google.api.client.auth.oauth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedParser;

import java.io.IOException;

/**
 * Generic OAuth 1.0a URL to request a temporary or long-lived token from an authorization server.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractOAuthGetToken extends GenericUrl {

  /**
   * HTTP transport required for executing request in {@link #execute()}.
   *
   * @since 1.3
   */
  public HttpTransport transport;

  /**
   * Required identifier portion of the client credentials (equivalent to a username).
   */
  public String consumerKey;

  /** Required OAuth signature algorithm. */
  public OAuthSigner signer;

  /** {@code true} for POST request or the default {@code false} for GET request. */
  protected boolean usePost;

  /**
   * @param authorizationServerUrl encoded authorization server URL
   */
  protected AbstractOAuthGetToken(String authorizationServerUrl) {
    super(authorizationServerUrl);
  }

  /**
   * Executes the HTTP request for a temporary or long-lived token.
   *
   * @return OAuth credentials response object
   * @throws HttpResponseException for an HTTP error code
   * @throws IOException I/O exception
   */
  public final OAuthCredentialsResponse execute() throws IOException {
    createParameters().signRequestsUsingAuthorizationHeader(transport);
    HttpRequest request = usePost ? transport.buildPostRequest() : transport.buildGetRequest();
    request.url = this;
    HttpResponse response = request.execute();
    response.disableContentLogging = true;
    OAuthCredentialsResponse oauthResponse = new OAuthCredentialsResponse();
    UrlEncodedParser.parse(response.parseAsString(), oauthResponse);
    return oauthResponse;
  }

  /**
   * Returns a new instance of the OAuth authentication provider. Subclasses may override by calling
   * this super implementation and then adding OAuth parameters.
   */
  public OAuthParameters createParameters() {
    OAuthParameters result = new OAuthParameters();
    result.consumerKey = consumerKey;
    result.signer = signer;
    return result;
  }
}
