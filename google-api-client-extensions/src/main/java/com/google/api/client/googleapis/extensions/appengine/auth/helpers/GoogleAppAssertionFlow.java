/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.googleapis.extensions.appengine.auth.helpers;

import com.google.api.client.extensions.appengine.auth.helpers.AppAssertionFlow;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import javax.jdo.annotations.PersistenceAware;

/**
 * Specialization of {@link AppAssertionFlow} with the endpoint and audience filled in for accessing
 * Google APIs.
 *
 * <p>
 * Upgrade warning: in prior version this class extended
 * {@link com.google.api.client.extensions.appengine.auth.helpers.AppAssertionFlow} but as of
 * version 1.6 it now extends {@link AppAssertionFlow}, which is a drop-in replacement.
 * </p>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 *
 * @since 1.5
 */
@PersistenceAware
public class GoogleAppAssertionFlow extends AppAssertionFlow {
  /**
   * Create an instance of {@link AppAssertionFlow} that is specialized for communicating with
   * Google APIs.
   *
   * @param robotName Identifier that will eventually become the primary key for the credential
   *        object created by this flow. This is usually the application's identifier.
   * @param scope OAuth scope for which we are requesting access.
   * @param transport Instance that we will use for network communication.
   * @param jsonFactory Instance that we will use to deserialize responses from the auth server.
   */
  public GoogleAppAssertionFlow(
      String robotName, String scope, HttpTransport transport, JsonFactory jsonFactory) {
    super(robotName,
        GoogleAccessTokenRequest.AUTHORIZATION_SERVER_URL,
        scope,
        GoogleAccessTokenRequest.AUTHORIZATION_SERVER_URL,
        transport,
        jsonFactory);
  }

}
