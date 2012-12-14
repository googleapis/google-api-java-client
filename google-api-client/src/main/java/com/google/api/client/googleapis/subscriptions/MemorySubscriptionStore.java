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
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link SubscriptionStore} which stores all subscription information in memory.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    service.setSubscriptionStore(new MemorySubscriptionStore());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public class MemorySubscriptionStore implements SubscriptionStore {

  /** Lock on the token response information. */
  private final Lock lock = new ReentrantLock();

  /** Map of all stored subscriptions. */
  private final SortedMap<String, Subscription> storedSubscriptions =
      new TreeMap<String, Subscription>();

  public void storeSubscription(Subscription subscription) {
    lock.lock();
    try {
      storedSubscriptions.put(subscription.getSubscriptionId(), subscription);
    } finally {
      lock.unlock();
    }
  }

  public void removeSubscription(Subscription subscription) {
    lock.lock();
    try {
      storedSubscriptions.remove(subscription.getSubscriptionId());
    } finally {
      lock.unlock();
    }
  }

  public Collection<Subscription> listSubscriptions() {
    lock.lock();
    try {
      return Collections.unmodifiableCollection(storedSubscriptions.values());
    } finally {
      lock.unlock();
    }
  }

  public Subscription getSubscription(String subscriptionId) {
    lock.lock();
    try {
      return storedSubscriptions.get(subscriptionId);
    } finally {
      lock.unlock();
    }
  }
}
