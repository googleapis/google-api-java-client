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

import java.io.IOException;
import java.util.Collection;

/**
 * Stores and manages registered subscriptions and their handlers.
 *
 * <p>
 * Implementation should be thread-safe.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public interface SubscriptionStore {

  /**
   * Returns all known/registered subscriptions.
   */
  Collection<Subscription> listSubscriptions() throws IOException;

  /**
   * Retrieves a known subscription or {@code null} if not found.
   *
   * @param subscriptionId ID of the subscription to retrieve
   */
  Subscription getSubscription(String subscriptionId) throws IOException;

  /**
   * Stores the subscription in the applications data store, replacing any existing subscription
   * with the same id.
   *
   * @param subscription New or existing {@link Subscription} to store/update
   */
  void storeSubscription(Subscription subscription) throws IOException;

  /**
   * Removes a registered subscription from the store.
   *
   * @param subscription {@link Subscription} to remove or {@code null} to ignore
   */
  void removeSubscription(Subscription subscription) throws IOException;
}
