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

package com.google.api.client.googleapis.extensions.appengine.subscriptions;

import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.IOException;

/**
 * Implementation of a persistent {@link SubscriptionStore} making use of native DataStore and
 * the Memcache API on AppEngine.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * On AppEngine you should prefer this SubscriptionStore over others due to performance and quota
 * reasons.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    service.setSubscriptionStore(new CachedAppEngineSubscriptionStore());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public final class CachedAppEngineSubscriptionStore extends AppEngineSubscriptionStore {

  /** Cache expiration time in seconds. */
  private static final int EXPIRATION_TIME = 3600;

  /** The service instance used to access the Memcache API. */
  private MemcacheService memCache = MemcacheServiceFactory.getMemcacheService(
      CachedAppEngineSubscriptionStore.class.getCanonicalName());

  @Override
  public void removeSubscription(Subscription subscription) throws IOException {
    super.removeSubscription(subscription);
    memCache.delete(subscription.getSubscriptionId());
  }

  @Override
  public void storeSubscription(Subscription subscription) throws IOException {
    super.storeSubscription(subscription);
    memCache.put(subscription.getSubscriptionId(), subscription);
  }

  @Override
  public Subscription getSubscription(String subscriptionId) throws IOException {
    if (memCache.contains(subscriptionId)) {
      return (Subscription) memCache.get(subscriptionId);
    }

    Subscription subscription = super.getSubscription(subscriptionId);
    memCache.put(subscriptionId, subscription, Expiration.byDeltaSeconds(EXPIRATION_TIME));
    return subscription;
  }
}
