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

package com.google.api.client.googleapis.extensions.auth.helpers.oauth2.draft10;

import com.google.api.client.extensions.auth.helpers.oauth2.draft10.OAuth2ThreeLeggedFlow;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAuthorizationRequestUrl;

import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;

/**
 * This class performs the same function as {@link OAuth2ThreeLeggedFlow} but provides a convenience
 * constructor that fills in the OAuth2 endpoints for talking to Google APIs.
 *
 * It is not safe to use one instance of this implementation from multiple threads.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class GoogleOAuth2ThreeLeggedFlow extends OAuth2ThreeLeggedFlow {

  /**
   * Create the flow object with the information provided and generate the authorization url.
   *
   * @param userId Key that will be used to associate this flow object with an end user.
   * @param clientId Used to identify the client server with the token server.
   * @param clientSecret Secret shared between the client server and the token server.
   * @param scope OAuth2 scope or space delimited list of scopes for which we require access.
   * @param callbackUrl Where the authorization should redirect the user to complete the flow.
   */
  public GoogleOAuth2ThreeLeggedFlow(
      String userId, String clientId, String clientSecret, String scope, String callbackUrl) {
    super(userId,
        clientId,
        clientSecret,
        scope,
        callbackUrl,
        GoogleAuthorizationRequestUrl.AUTHORIZATION_SERVER_URL,
        GoogleAccessTokenRequest.AUTHORIZATION_SERVER_URL);
  }

}
