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

package com.google.api.client.extensions.auth.helpers.oauth2.draft10;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource.Method;
import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;

import javax.jdo.InstanceCallbacks;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * {@link Credential} implementation that is used to authorize OAuth2 enabled requests through the
 * use of the access_token header, as well as refresh the token when it is required. It is important
 * that access to this class be made from within a managed JDO context and that the persistence
 * manager be closed in a finally block to save any updates to the access token.
 *
 * This class is safe to use from multiple threads.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
@PersistenceCapable
public final class OAuth2Credential implements Credential, InstanceCallbacks {
  // TODO(moshenko) Remove final modifier once we have a plan for subclassing in place

  /**
   * User ID to be used as the key for storage and retrieval of the credential object.
   */
  @SuppressWarnings("unused")
  @PrimaryKey
  private String userId;

  /**
   * Access token used to authorize requests.
   */
  @Persistent
  volatile String accessToken;

  /**
   * Refresh token used to request a new access token when the previous one has expired or {@code
   * null} if refresh is not supported.
   */
  @Persistent
  private String refreshToken;

  @NotPersistent
  AccessProtectedResource authInterceptor;

  /**
   * Create an instance of an OAuth2Credential that can be used to authorize requests on behalf of
   * an end user. Once you construct the object, you must call
   * {@link #initializeForRefresh(String, String, String, JsonFactory, HttpTransport)} to provide
   * the necessary information to allow token refresh.
   *
   * @param userId Key that can be used to associate this Credential object with an end user.
   * @param accessToken Access token that can be used to authorize this request.
   * @param refreshToken Token that can be given to the token server in exchange for a new access
   *        token.
   */
  public OAuth2Credential(String userId, String accessToken, String refreshToken) {
    this.userId = Preconditions.checkNotNull(userId);
    this.accessToken = Preconditions.checkNotNull(accessToken);
    this.refreshToken = Preconditions.checkNotNull(refreshToken);
    initializeAfterConstruction();
  }

  /**
   * Create an instance of an OAuth2Credential that can be used to authorize requests on behalf of
   * an end user. Instances created through this constructor are not eligible for token refresh.
   *
   * @param userId Key that can be used to associate this Credential object with an end user.
   * @param accessToken Access token that can be used to authorize this request.
   */
  public OAuth2Credential(String userId, String accessToken) {
    this.userId = Preconditions.checkNotNull(userId);
    this.accessToken = Preconditions.checkNotNull(accessToken);
    initializeAfterConstruction();
  }

  /**
   * Extra initialization that must be shared between constructor creation and JDO loading.
   */
  private void initializeAfterConstruction() {
    authInterceptor = new AccessProtectedResource(accessToken, Method.AUTHORIZATION_HEADER);
  }

  /**
   * Force a refresh of this credential if possible using the {@link HttpTransport} and
   * {@link JsonFactory} objects provided for the network communication.
   *
   * @param transport {@link HttpTransport} to use for the refresh
   * @param factory {@link JsonFactory} to use to parse the auth response
   *
   * @return Success or failure of refresh operation
   *
   * @throws IOException When the credential can not communicate with the token server.
   */
  public boolean refresh(HttpTransport transport, JsonFactory factory) throws IOException {
    return authInterceptor.refreshToken();
  }

  public void initialize(HttpRequest request) throws IOException {
    request.interceptor = authInterceptor;
    request.unsuccessfulResponseHandler = authInterceptor;
  }

  public void intercept(HttpRequest request) throws IOException {
    authInterceptor.intercept(request);
  }

  public boolean handleResponse(
      HttpRequest request, HttpResponse response, boolean retrySupported) {
    return authInterceptor.handleResponse(request, response, retrySupported);
  }

  /**
   * Initialize the instance with the required information to allow token refresh. This must be done
   * after construction or after loading the object from the data store.
   *
   * @param clientId Used to identify the client server with the token server.
   * @param clientSecret Secret shared between the client server and the token server.
   * @param refreshUrl Url which can be used to exchange the refresh token for a new access token.
   * @param jsonFactory Json factory used to deserialize communications with the token server.
   * @param transport Transport used to send requests to the token server.
   */
  public void initializeForRefresh(String clientId, String clientSecret, String refreshUrl,
      JsonFactory jsonFactory, HttpTransport transport) {
    Preconditions.checkArgument(
        refreshToken != null, "Must construct the object with a refreshToken");
    Preconditions.checkNotNull(clientId);
    Preconditions.checkNotNull(clientSecret);
    Preconditions.checkNotNull(clientSecret);
    Preconditions.checkNotNull(transport);
    Preconditions.checkNotNull(jsonFactory);

    authInterceptor = new AccessProtectedResource(accessToken,
        Method.AUTHORIZATION_HEADER,
        transport,
        jsonFactory,
        refreshUrl,
        clientId,
        clientSecret,
        refreshToken) {

      @Override
      protected void onAccessToken(String accessToken) {
        OAuth2Credential.this.accessToken = accessToken;
      }
    };
  }

  /**
   * Return the current access token. This may be either the access token with which the object was
   * constructed, or a token fetched through a successful refresh.
   */
  public String getAccessToken() {
    return authInterceptor.getAccessToken();
  }

  /**
   * Return the refresh token with which this object was constructed. This can be used for out of
   * band use cases, but most users should just let the credential be refreshed by the library when
   * a call fails.
   */
  public String getRefreshToken() {
    return refreshToken;
  }

  public void jdoPreClear() {
    // Intentionally blank
  }

  public void jdoPreDelete() {
    // Intentionally blank
  }

  public void jdoPostLoad() {
    initializeAfterConstruction();
  }

  public void jdoPreStore() {
    // Intentionally blank
  }
}
