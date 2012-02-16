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

package com.google.api.client.googleapis.extensions.auth.helpers.oauth;

import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.extensions.auth.helpers.ThreeLeggedFlow;
import com.google.api.client.extensions.auth.helpers.oauth.OAuthHmacCredential;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetAccessToken;
import com.google.api.client.googleapis.auth.oauth.GoogleOAuthGetTemporaryToken;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * {@link ThreeLeggedFlow} implementation that will execute the proper requests to obtain an OAuth1
 * Credential object that can be used to sign requests.
 *
 *<p>
 * This class is not thread safe, nor should you attempt to execute a flow from multiple threads
 * simultaneously.
 * </p>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.5
 */
@PersistenceCapable
public class GoogleOAuthHmacThreeLeggedFlow implements ThreeLeggedFlow {

  /**
   * Key that can be used to associate this flow with an end user.
   */
  @PrimaryKey
  private String userId;

  /**
   * Temporary token that gets associated with this flow.
   */
  @Persistent
  private String tempToken;

  /**
   * Temporary secret that gets associated with the temporary token.
   */
  @Persistent
  private String tempTokenSecret;

  /**
   * Secret that is shared between the server and the service provider.
   */
  @Persistent
  private String consumerSecret;

  /**
   * Key that identifies the server to the service provider.
   */
  @Persistent
  private String consumerKey;

  /**
   * Url which is generated to authorize this specific user for this service.
   */
  @Persistent
  private final String authorizationUrl;

  /**
   * Http transport to use to communicate with the auth server.
   */
  @NotPersistent
  private HttpTransport transport;

  /**
   * Create an OAuthThreeLeggedFlow instance from the required information.
   *
   * @param userId Key that can be used to associate this flow with an end user.
   * @param consumerKey Key that identifies the server to the service provider.
   * @param consumerSecret Secret that is shared between the server and the service provider.
   * @param scope OAuth scope for which the server is requesting access.
   * @param xoauthDisplayname Display name to show the user when requesting access.
   * @param callbackUrl Url which the server should redirect the user to after obtaining
   *        authorization.
   *
   * @throws IOException Exception thrown when the flow is unable to communicate with the service
   *         provider.
   */
  public GoogleOAuthHmacThreeLeggedFlow(String userId,
      String consumerKey,
      String consumerSecret,
      String scope,
      String xoauthDisplayname,
      String callbackUrl,
      HttpTransport transport) throws IOException {

    this.userId = userId;
    this.consumerSecret = consumerSecret;
    this.consumerKey = consumerKey;
    this.transport = transport;

    GoogleOAuthGetTemporaryToken temporaryToken = new GoogleOAuthGetTemporaryToken();
    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = consumerSecret;
    temporaryToken.signer = signer;
    temporaryToken.consumerKey = consumerKey;
    temporaryToken.scope = scope;
    temporaryToken.displayName = xoauthDisplayname;
    temporaryToken.callback = callbackUrl;
    temporaryToken.transport = this.transport;

    OAuthCredentialsResponse tempCredentials = temporaryToken.execute();

    tempToken = tempCredentials.token;
    tempTokenSecret = tempCredentials.tokenSecret;

    GoogleOAuthAuthorizeTemporaryTokenUrl authorizeUrl =
        new GoogleOAuthAuthorizeTemporaryTokenUrl();
    authorizeUrl.temporaryToken = tempCredentials.token;
    this.authorizationUrl = authorizeUrl.build();
  }

  public String getAuthorizationUrl() {
    return authorizationUrl;
  }

  public Credential complete(String authorizationCode) throws IOException {
    Preconditions.checkNotNull(transport, "Must call setHttpTransport before calling complete.");

    GoogleOAuthGetAccessToken accessToken = new GoogleOAuthGetAccessToken();
    accessToken.temporaryToken = tempToken;
    accessToken.transport = transport;

    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = consumerSecret;
    signer.tokenSharedSecret = tempTokenSecret;

    accessToken.signer = signer;
    accessToken.consumerKey = consumerKey;
    accessToken.verifier = authorizationCode;
    OAuthCredentialsResponse credentials = accessToken.execute();
    signer.tokenSharedSecret = credentials.tokenSecret;

    OAuthHmacCredential accessCredential = new OAuthHmacCredential(
        userId, consumerKey, consumerSecret, credentials.tokenSecret, credentials.token);

    return accessCredential;
  }

  public Credential loadCredential(PersistenceManager pm) {
    try {
      return pm.getObjectById(OAuthHmacCredential.class, userId);
    } catch (JDOObjectNotFoundException e) {
      return null;
    }
  }

  public void setHttpTransport(HttpTransport transport) {
    this.transport = Preconditions.checkNotNull(transport);
  }

  public void setJsonFactory(JsonFactory jsonFactory) {
    // Intentionally blank, not used by OAuth1
  }
}
