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

package com.google.api.client.googleapis.services;

import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;

/**
 * Google client request initializer that specifies a Google API key for all requests.
 *
 * <p>
 * This is needed when doing unauthenticated access to Google APIs. Otherwise, you will only be able
 * to make a small number of queries. When you exceed this limit, you will receive a "403 Forbidden"
 * error with the message "Daily Limit Exceeded. Please sign up". See <a
 * href="http://code.google.com/p/google-api-java-client/wiki/OAuth2#Unauthenticated_access"
 * >Unauthenticated access</a> for more details.
 * </p>
 *
 * <p>
 * Note that this is not needed when doing authenticated access with an OAuth 2.0 access token,
 * because the OAuth 2.0 client ID is already associated with the same project as the API key.
 * </p>
 *
 * @since 1.8
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.14) Instead use either
 *             {@link CommonGoogleClientRequestInitializer} or a subclass of it.
 */
@Deprecated
public class GoogleKeyInitializer
    implements
      JsonHttpRequestInitializer,
      GoogleClientRequestInitializer {

  /** API key. */
  private final String key;

  /**
   * @param key API key
   */
  public GoogleKeyInitializer(String key) {
    this.key = key;
  }

  public void initialize(JsonHttpRequest request) {
    request.put("key", key);
  }

  public void initialize(AbstractGoogleClientRequest<?> request) {
    request.put("key", key);
  }
}
