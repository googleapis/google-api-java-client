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

package com.google.api.client.extensions.auth.helpers;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;

/**
 * Interface for auth flows that require a user authorization step through a web browser to obtain
 * an authorization code.
 *
 * <p>
 * Implementations are required to be {@link PersistenceCapable} for storage in JDO compliant
 * datastores during user authorization step.
 * </p>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
public interface ThreeLeggedFlow {

  /**
   * After the object is created, the developer should use this method to interrogate it for the
   * authorization URL to which the user should be redirected to obtain permission.
   *
   * @return URL to which the user should be directed
   */
  String getAuthorizationUrl();

  /**
   * Set {@link HttpTransport} instance for this three legged flow.
   */
  void setHttpTransport(HttpTransport transport);

  /**
   * Set {@link JsonFactory} instance for this three legged flow.
   */
  void setJsonFactory(JsonFactory jsonFactory);

  /**
   * Convenience function that will load a credential based on the userId for which this flow was
   * instantiated.
   *
   * @param pm {@link PersistenceManager} instance which this flow should use to interact with the
   *        data store. The caller must remember to call {@link PersistenceManager#close()} after
   *        this method returns.
   * @return Fully initialized {@link Credential} object or {@code null} if none exists.
   */
  Credential loadCredential(PersistenceManager pm);

  /**
   * After the user has authorized the request, the token or code obtained should be passed to this
   * complete function to allow us to exchange the code with the authentication server for a
   * {@link Credential}.
   *
   * @param authorizationCode Code or token obtained after the user grants permission
   * @return {@link Credential} object that is obtained from token server
   *
   * @throws IOException When an error occurs when communicating with the token server
   */
  Credential complete(String authorizationCode) throws IOException;
}
