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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * Client subscription information after the subscription has been created.
 *
 * <p>
 * Should be stored in a {@link SubscriptionStore}. Implementation is thread safe.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.12
 */
public final class Subscription implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Notification handler called when a notification is received for this subscription. */
  private final NotificationCallback notificationCallback;

  /** Unique subscription ID. */
  private final String subscriptionID;

  /**
   * Opaque string provided by the client when it creates the subscription and echoed back to the
   * client for every notification it receives for that subscription or {@code null} for none.
   */
  private final String clientToken;

  /** Opaque ID for the subscribed resource that is stable across API versions. */
  private final String topicID;

  /**
   * HTTP Date indicating the time at which the subscription will expire or {@code null} for an
   * infinite TTL.
   */
  private final String subscriptionExpires;

  /**
   * @param handler notification handler called when a notification is received for this
   *        subscription
   * @param subscriptionHeaders subscription headers
   */
  public <T> Subscription(NotificationCallback handler, SubscriptionHeaders subscriptionHeaders) {
    this.notificationCallback = Preconditions.checkNotNull(handler);
    this.subscriptionID = Preconditions.checkNotNull(subscriptionHeaders.getSubscriptionID());
    this.clientToken = subscriptionHeaders.getClientToken();
    this.topicID = Preconditions.checkNotNull(subscriptionHeaders.getTopicID());
    this.subscriptionExpires = subscriptionHeaders.getSubscriptionExpires();
  }

  /** Returns the unique subscription ID. */
  public String getSubscriptionID() {
    return subscriptionID;
  }

  /**
   * Returns the notification handler called when a notification is received for this subscription.
   */
  public NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /**
   * Returns the opaque string provided by the client when it creates the subscription and echoed
   * back to the client for every notification it receives for that subscription or {@code null} for
   * none.
   */
  public String getClientToken() {
    return clientToken;
  }

  /** Returns the opaque ID for the subscribed resource that is stable across API versions. */
  public String getTopicID() {
    return topicID;
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public String getSubscriptionExpires() {
    return subscriptionExpires;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(Subscription.class).add("subscriptionID", subscriptionID)
        .add("clientToken", clientToken).add("topicID", topicID)
        .add("subscriptionExpires", subscriptionExpires)
        .add("notificationCallback", notificationCallback).toString();
  }
}
