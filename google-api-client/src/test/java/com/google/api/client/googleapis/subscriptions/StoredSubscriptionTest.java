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

import com.google.api.client.json.GenericJson;

import junit.framework.TestCase;

/**
 * Tests {@link StoredSubscription}.
 *
 * @author Yaniv Inbar
 */
public class StoredSubscriptionTest extends TestCase {

  static class SomeNotificationCallback implements NotificationCallback {
    private static final long serialVersionUID = 1L;

    public void handleNotification(StoredSubscription subscription, UnparsedNotification notification) {
    }
  }

  private static final String SUBSCRIPTION_ID_VALUE = "someSubscriptionID";
  private static final String TOPIC_ID_VALUE = "someTopicID";
  private static final String CLIENT_TOKEN_VALUE = "someClienToken";
  private static final String SUBSCRIPTION_EXPIRES_VALUE = "Fri, 07 Sep 2012 18:52:00 GMT";

  public void testConstructor() {
    SomeNotificationCallback callback = new SomeNotificationCallback();
    StoredSubscription subscription = new StoredSubscription(callback, SUBSCRIPTION_ID_VALUE).setClientToken(
        CLIENT_TOKEN_VALUE)
        .setExpiration(SUBSCRIPTION_EXPIRES_VALUE).setTopicId(TOPIC_ID_VALUE);
    assertEquals(SUBSCRIPTION_ID_VALUE, subscription.getId());
    assertEquals(TOPIC_ID_VALUE, subscription.getTopicId());
    assertEquals(CLIENT_TOKEN_VALUE, subscription.getClientToken());
    assertEquals(SUBSCRIPTION_EXPIRES_VALUE, subscription.getExpiration());
    assertEquals(callback, subscription.getNotificationCallback());
    subscription = new StoredSubscription(callback);
    assertNotNull(subscription.getId());
  }

  public void testConstructor_basedOnJson() {
    SomeNotificationCallback callback = new SomeNotificationCallback();
    GenericJson subscriptionJson = new GenericJson();
    subscriptionJson.put("id", SUBSCRIPTION_ID_VALUE);
    StoredSubscription subscription = new StoredSubscription(callback, subscriptionJson);
    assertEquals(callback, subscription.getNotificationCallback());
    assertEquals(SUBSCRIPTION_ID_VALUE, subscription.getId());
    assertNull(subscription.getTopicId());
    assertNull(subscription.getClientToken());
    assertNull(subscription.getExpiration());
    subscriptionJson.put("clientToken", CLIENT_TOKEN_VALUE);
    subscriptionJson.put("expiration", SUBSCRIPTION_EXPIRES_VALUE);
    subscriptionJson.put("topicId", TOPIC_ID_VALUE);
    subscription = new StoredSubscription(callback, subscriptionJson);
    assertEquals(callback, subscription.getNotificationCallback());
    assertEquals(SUBSCRIPTION_ID_VALUE, subscription.getId());
    assertEquals(TOPIC_ID_VALUE, subscription.getTopicId());
    assertEquals(CLIENT_TOKEN_VALUE, subscription.getClientToken());
    assertEquals(SUBSCRIPTION_EXPIRES_VALUE, subscription.getExpiration());
  }
}
