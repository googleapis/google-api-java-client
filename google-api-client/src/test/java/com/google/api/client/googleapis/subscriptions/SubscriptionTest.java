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

import junit.framework.TestCase;

/**
 * Tests {@link Subscription}.
 *
 * @author Yaniv Inbar
 */
public class SubscriptionTest extends TestCase {

  static class SomeNotificationCallback implements NotificationCallback {
    private static final long serialVersionUID = 1L;

    public void handleNotification(Subscription subscription, UnparsedNotification notification) {
    }
  }

  private static final String SUBSCRIPTION_ID_VALUE = "someSubscriptionID";
  private static final String TOPIC_ID_VALUE = "someTopicID";
  private static final String CLIENT_TOKEN_VALUE = "someClienToken";
  private static final String SUBSCRIPTION_EXPIRES_VALUE = "Fri, 07 Sep 2012 18:52:00 GMT";

  public void testConstructor() {
    SomeNotificationCallback callback = new SomeNotificationCallback();
    Subscription subscription = new Subscription(
        callback, CLIENT_TOKEN_VALUE, SUBSCRIPTION_ID_VALUE).processResponse(
        SUBSCRIPTION_EXPIRES_VALUE, TOPIC_ID_VALUE);
    assertEquals(SUBSCRIPTION_ID_VALUE, subscription.getSubscriptionId());
    assertEquals(TOPIC_ID_VALUE, subscription.getTopicId());
    assertEquals(CLIENT_TOKEN_VALUE, subscription.getClientToken());
    assertEquals(SUBSCRIPTION_EXPIRES_VALUE, subscription.getSubscriptionExpires());
    assertEquals(callback, subscription.getNotificationCallback());
  }
}
