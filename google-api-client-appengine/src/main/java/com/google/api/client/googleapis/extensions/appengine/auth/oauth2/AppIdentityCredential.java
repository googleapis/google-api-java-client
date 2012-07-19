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

package com.google.api.client.googleapis.extensions.appengine.auth.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

/**
 * OAuth 2.0 credential in which a client Google App Engine application needs to access data that it
 * owns, based on <a href="http://code.google.com/appengine/docs/java/appidentity/overview.html
 * #Asserting_Identity_to_Google_APIs">Asserting Identity to Google APIs</a>.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(
      HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return transport.createRequestFactory(
        new AppIdentityCredential("https://www.googleapis.com/auth/urlshortener"));
  }
 * </pre>
 *
 * <p>
 * Implementation is immutable and thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AppIdentityCredential implements HttpRequestInitializer, HttpExecuteInterceptor {

  /** OAuth scopes. */
  private final List<String> scopes;

  /**
   * @param scopes OAuth scopes
   */
  public AppIdentityCredential(Iterable<String> scopes) {
    this.scopes = Lists.newArrayList(scopes.iterator());
  }

  /**
   * @param scopes OAuth scopes
   */
  public AppIdentityCredential(String... scopes) {
    this.scopes = Lists.newArrayList(scopes);
  }

  @Override
  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
  }

  /**
   * Intercept the request by using the access token obtained from the {@link AppIdentityService}.
   * Any thrown {@link AppIdentityServiceFailureException} will be wrapped with an
   * {@link IOException}.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link Exception}. In prior version 1.10 it threw
   * an {@link java.io.IOException}.
   * </p>
   */
  @Override
  public void intercept(HttpRequest request) throws Exception {
    try {
      String accessToken =
          AppIdentityServiceFactory.getAppIdentityService().getAccessToken(scopes).getAccessToken();
      BearerToken.authorizationHeaderAccessMethod().intercept(request, accessToken);
    } catch (AppIdentityServiceFailureException e) {
      throw new IOException(e);
    }
  }
}
