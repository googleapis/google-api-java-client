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

import com.google.api.client.googleapis.subscriptions.StoredSubscription;
import com.google.api.client.googleapis.testing.subscriptions.MockNotificationCallback;
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

  @Test
  public void testStoreAndGet() throws Exception {
    StoredSubscription subscription = new StoredSubscription(new MockNotificationCallback());
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();
    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    assertEquals(subscription.getId(),
        aess.getSubscription(subscription.getId()).getId());
    assertNull(aess.getSubscription("nonexistent"));
  }

  @Test
  public void testStoreAndGet_overwrite() throws Exception {
    StoredSubscription subscription =
        new StoredSubscription(new MockNotificationCallback()).setClientToken("clientToken");
    StoredSubscription subscriptionB = new StoredSubscription(
        new MockNotificationCallback(), subscription.getId()).setClientToken(
        "clientToken2");
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();
    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    assertEquals(subscription.getClientToken(),
        aess.getSubscription(subscription.getId()).getClientToken());
    aess.storeSubscription(subscriptionB);
    assertEquals(subscriptionB.getClientToken(),
        aess.getSubscription(subscription.getId()).getClientToken());
  }


  @Test
  public void testStoreAndRemove() throws Exception {
    StoredSubscription subscription = new StoredSubscription(new MockNotificationCallback());
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    aess.removeSubscription(subscription);
    assertNull(aess.getSubscription(subscription.getId()));

    aess.removeSubscription(null);
  }

  @Test
  public void testList() throws Exception {
    StoredSubscription subscription = new StoredSubscription(new MockNotificationCallback());
    StoredSubscription subscriptionB = new StoredSubscription(new MockNotificationCallback());
    AppEngineSubscriptionStore aess = new AppEngineSubscriptionStore();

    // Store and retrieve the subscription
    aess.storeSubscription(subscription);
    aess.storeSubscription(subscriptionB);
    assertEquals(2, aess.listSubscriptions().size());
  }
}
