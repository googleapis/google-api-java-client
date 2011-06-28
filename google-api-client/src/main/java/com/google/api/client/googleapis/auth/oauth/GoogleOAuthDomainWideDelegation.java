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
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 * Google's OAuth domain-wide delegation requires an e-mail address of the user whose data you are
 * trying to access via {@link #requestorId} on every HTTP request.
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    GoogleOAuthDomainWideDelegation initializer = new GoogleOAuthDomainWideDelegation();
    initializer.requestorId = "...";
    OAuthParameters parameters = new OAuthParameters();
    // parameters...
    initializer.parameters = parameters;
    return transport.createRequestFactory(initializer);
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, take a look at the sample usage for
 * {@link HttpExecuteInterceptor}, which this class also implements.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class GoogleOAuthDomainWideDelegation
    implements HttpExecuteInterceptor, HttpRequestInitializer {

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

  /**
   * OAuth parameters.
   *
   * @since 1.4
   */
  public OAuthParameters parameters;

  public void initialize(HttpRequest request) {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    request.getUrl().set("xoauth_requestor_id", requestorId);
    if (parameters != null) {
      parameters.intercept(request);
    }
  }
}
