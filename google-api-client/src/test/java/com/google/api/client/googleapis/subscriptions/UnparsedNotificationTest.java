/*
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

import com.google.api.client.googleapis.testing.subscriptions.MockNotificationCallback;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Yaniv Inbar
 */
public class UnparsedNotificationTest extends TestCase {

  /** Test for .deliverPushNotification() using the normal flow. */
  public void testDeliverPushNotification_simple() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    Subscription s =
        new Subscription(handler, "clientToken", "id").processResponse(null, "topicID");
    store.storeSubscription(s);
    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification(
        "id", "topicID", "topicURI", "clientToken", 1, "eventType", null, "foo/bar", contentStream);
    assertTrue(notification.deliverNotification(store));
    assertEquals(true, handler.wasCalled());
  }

  /** Test for .deliverPushNotification() with a non-existing subscription. */
  public void testDeliverPushNotification_nonExistent() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    Subscription s =
        new Subscription(handler, "clientToken", "differentId").processResponse(null, "topicID");
    store.storeSubscription(s);

    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification(
        "id", "topicID", "topicURI", "clientToken", 1, "eventType", null, "foo/bar", contentStream);

    assertFalse(notification.deliverNotification(store));
    assertEquals(false, handler.wasCalled());
  }

  /** Test for .deliverPushNotification() with an invalid client token. */
  public void testDeliverPushNotification_invalidToken() throws Exception {
    MemorySubscriptionStore store = new MemorySubscriptionStore();
    MockNotificationCallback handler = new MockNotificationCallback();
    Subscription s =
        new Subscription(handler, "randomToken", "id").processResponse(null, "topicID");
    store.storeSubscription(s);

    // Send a notification
    InputStream contentStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
    UnparsedNotification notification = new UnparsedNotification(
        "id", "topicID", "topicURI", "differentClientToken", 1, "eventType", null, "foo/bar",
        contentStream);

    try {
      assertTrue(notification.deliverNotification(store));
      fail("Expected IllegalArgumentException but code passed");
    } catch (IllegalArgumentException expected) {
    }

    assertEquals(false, handler.wasCalled());
  }
}
