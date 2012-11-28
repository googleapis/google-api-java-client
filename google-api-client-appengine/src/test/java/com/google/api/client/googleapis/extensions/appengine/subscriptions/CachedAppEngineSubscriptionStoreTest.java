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

import com.google.api.client.googleapis.subscriptions.NotificationCallback;
import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.UnparsedNotification;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link CachedAppEngineSubscriptionStore} class.
 *
 * @author Matthias Linder
 */
public class CachedAppEngineSubscriptionStoreTest extends TestCase {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalDatastoreServiceTestConfig(), new LocalMemcacheServiceTestConfig());

  @Override
  @Before
  public void setUp() {
    helper.setUp();
  }

  @Override
  @After
  public void tearDown() {
    helper.tearDown();
  }

  private static class SomeNotificationCallback implements NotificationCallback {
    private static final long serialVersionUID = 1L;

    public SomeNotificationCallback() {
    }

    @Override
    public void handleNotification(Subscription subscription, UnparsedNotification notification) {
    }
  }

  @Test
  public void testStoreAndGet() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken123", "someID").processResponse(
        null, "topicID");

    // Store a single subscription
    CachedAppEngineSubscriptionStore caess = new CachedAppEngineSubscriptionStore();
    caess.storeSubscription(subscription);
    Subscription sub = caess.getSubscription("someID");
    assertEquals(subscription.getSubscriptionId(), sub.getSubscriptionId());
    assertEquals(1, MemcacheServiceFactory.getMemcacheService().getStatistics().getItemCount());

    // Delete the item from the data store, and confirm that the cached value is returned
    new AppEngineSubscriptionStore().removeSubscription(sub);
    assertEquals(sub.getClientToken(), caess.getSubscription("someID").getClientToken());
  }

  @Test
  public void testRemoveAndGet() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");

    // Store a single subscription
    CachedAppEngineSubscriptionStore caess = new CachedAppEngineSubscriptionStore();
    caess.storeSubscription(subscription);
    Subscription sub = caess.getSubscription("someID");
    assertEquals(subscription.getSubscriptionId(), sub.getSubscriptionId());
    assertEquals(1, MemcacheServiceFactory.getMemcacheService().getStatistics().getItemCount());

    // Delete the item from the data store, and confirm that the cached value is returned
    caess.removeSubscription(sub);
    assertEquals(0, MemcacheServiceFactory.getMemcacheService().getStatistics().getItemCount());
  }

  @Test
  public void testGet() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");

    // Store a single subscription
    new AppEngineSubscriptionStore().storeSubscription(subscription);

    // Delete the item from the data store, and confirm that the cached value is returned
    CachedAppEngineSubscriptionStore caess = new CachedAppEngineSubscriptionStore();
    assertEquals(
        subscription.getSubscriptionId(), caess.getSubscription("someID").getSubscriptionId());
    assertEquals(1, MemcacheServiceFactory.getMemcacheService().getStatistics().getItemCount());

    // Check that the 'null' result is also cached
    caess.getSubscription("non-existant");
    assertEquals(2, MemcacheServiceFactory.getMemcacheService().getStatistics().getItemCount());
  }

  @Test
  public void testStore_overwrite() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");
    Subscription subscriptionB =
        new Subscription(new SomeNotificationCallback(), "bbb", "someID").processResponse(
            null, "topicID");

    // Store a single subscription
    CachedAppEngineSubscriptionStore caess = new CachedAppEngineSubscriptionStore();
    caess.storeSubscription(subscription);
    caess.storeSubscription(subscriptionB);
    assertEquals(subscriptionB.getClientToken(), caess.getSubscription("someID").getClientToken());
  }
}
