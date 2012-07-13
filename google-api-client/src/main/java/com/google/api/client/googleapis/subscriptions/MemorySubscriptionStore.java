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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link SubscriptionStore} which stores all subscription information in memory.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    SubscriptionStore store = new MemorySubscriptionStore();
    service.setSubscriptionManager(new SubscriptionManager(store));
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public class MemorySubscriptionStore implements SubscriptionStore {

  /** Map of all stored subscriptions. */
  private final Map<String, Subscription> storedSubscriptions =
      new ConcurrentHashMap<String, Subscription>();

  public void storeSubscription(Subscription subscription) {
    storedSubscriptions.put(subscription.getSubscriptionID(), subscription);
  }

  public void removeSubscription(Subscription subscription) {
    storedSubscriptions.remove(subscription.getSubscriptionID());
  }

  public Collection<Subscription> listSubscriptions() {
    return Collections.unmodifiableCollection(storedSubscriptions.values());
  }

  public Subscription getSubscription(String subscriptionID) {
    return storedSubscriptions.get(subscriptionID);
  }
}
