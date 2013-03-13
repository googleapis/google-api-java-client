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

package com.google.api.client.googleapis.subscriptions;

import com.google.api.client.util.Experimental;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link Experimental} <br/>
 * Stores and manages registered subscriptions and their handlers.
 *
 * <p>
 * Implementation should be thread-safe.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
@Experimental
public interface SubscriptionStore {

  /**
   * Returns all known/registered subscriptions.
   */
  Collection<StoredSubscription> listSubscriptions() throws IOException;

  /**
   * Retrieves a known subscription or {@code null} if not found.
   *
   * @param subscriptionId ID of the subscription to retrieve
   */
  StoredSubscription getSubscription(String subscriptionId) throws IOException;

  /**
   * Stores the subscription in the applications data store, replacing any existing subscription
   * with the same id.
   *
   * @param subscription New or existing {@link StoredSubscription} to store/update
   */
  void storeSubscription(StoredSubscription subscription) throws IOException;

  /**
   * Removes a registered subscription from the store.
   *
   * @param subscription {@link StoredSubscription} to remove or {@code null} to ignore
   */
  void removeSubscription(StoredSubscription subscription) throws IOException;
}
