/*
 * 1 * Copyright (c) 2012 Google Inc.
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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link AppEngineSubscriptionStore} class.
 *
 * @author Matthias Linder (mlinder)
 */
public class AppEngineSubscriptionStoreTest extends TestCase {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

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
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    assertEquals(
        subscription.getSubscriptionId(), aess.getSubscription("someID").getSubscriptionId());
    assertEquals(null, aess.getSubscription("nonexistent"));
  }

  @Test
  public void testStoreAndGet_overwrite() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");
    Subscription subscriptionB = new Subscription(
        new SomeNotificationCallback(), "clientToken2", "someID").processResponse(null, "topicID");
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    assertEquals(subscription.getClientToken(), aess.getSubscription("someID").getClientToken());
    aess.storeSubscription(subscriptionB);
    assertEquals(subscriptionB.getClientToken(), aess.getSubscription("someID").getClientToken());
  }


  @Test
  public void testStoreAndRemove() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    aess.removeSubscription(subscription);
    assertEquals(null, aess.getSubscription("someID"));

    aess.removeSubscription(null);
  }

  @Test
  public void testList() throws Exception {
    Subscription subscription = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someID").processResponse(null, "topicID");
    Subscription subscriptionB = new Subscription(
        new SomeNotificationCallback(), "clientToken", "someOtherID").processResponse(
        null, "topicID");
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    aess.storeSubscription(subscriptionB);
    assertEquals(2, aess.listSubscriptions().size());
  }
}
