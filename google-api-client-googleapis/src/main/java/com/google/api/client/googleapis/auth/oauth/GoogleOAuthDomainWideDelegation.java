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

package com.google.api.client.googleapis.auth.oauth;

import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpExecuteIntercepter;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

/**
 * Google's OAuth domain-wide delegation requires an e-mail address of the user whose data you are
 * trying to access via {@link #requestorId} on every HTTP request.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleOAuthDomainWideDelegation implements HttpExecuteIntercepter {

  /**
   * Generic URL that extends {@link GoogleUrl} and also provides the {@link #requestorId}
   * parameter.
   */
  public static final class Url extends GoogleUrl {

    /** Email address of the user whose data you are trying to access. */
    @Key("xoauth_requestor_id")
    public String requestorId;

    /**
     * @param encodedUrl encoded URL, including any existing query parameters that should be parsed
     */
    public Url(String encodedUrl) {
      super(encodedUrl);
    }
  }

  /** Email address of the user whose data you are trying to access. */
  public String requestorId;

  public void intercept(HttpRequest request) {
    request.url.set("xoauth_requestor_id", requestorId);
  }

  /**
   * Performs OAuth HTTP request signing via query parameter for the {@code xoauth_requestor_id} and
   * the {@code Authorization} header as the final HTTP request execute intercepter for the given
   * HTTP request execute manager.
   *
   * @param transport HTTP transport
   * @param parameters OAuth parameters; the {@link OAuthParameters#signer} and
   *        {@link OAuthParameters#consumerKey} should be set
   */
  public void signRequests(HttpTransport transport, OAuthParameters parameters) {
    transport.intercepters.add(this);
    parameters.signRequestsUsingAuthorizationHeader(transport);
  }
}
