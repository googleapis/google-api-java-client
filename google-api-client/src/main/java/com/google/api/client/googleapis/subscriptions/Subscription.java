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
 * @since 1.14
 */
public final class Subscription implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Notification callback called when a notification is received for this subscription. */
  private final NotificationCallback notificationCallback;

  /**
   * Opaque string provided by the client or {@code null} for none.
   */
  private final String clientToken;

  /**
   * HTTP Date indicating the time at which the subscription will expire or {@code null} for an
   * infinite TTL.
   */
  private String subscriptionExpires;

  /** Subscription UUID. */
  private final String subscriptionId;

  /** Opaque ID for the subscribed resource that is stable across API versions. */
  private String topicId;

  /**
   * Constructor to be called before making the subscribe request.
   *
   * @param handler notification handler called when a notification is received for this
   *        subscription
   * @param clientToken opaque string provided by the client or {@code null} for none
   * @param subscriptionId subscription UUID
   */
  public Subscription(NotificationCallback handler, String clientToken, String subscriptionId) {
    this.notificationCallback = Preconditions.checkNotNull(handler);
    this.clientToken = clientToken;
    this.subscriptionId = Preconditions.checkNotNull(subscriptionId);
  }

  /**
   * Process subscribe response.
   *
   * @param subscriptionExpires HTTP Date indicating the time at which the subscription will expire
   *        or {@code null} for an infinite TTL
   * @param topicId opaque ID for the subscribed resource that is stable across API versions
   */
  public Subscription processResponse(String subscriptionExpires, String topicId) {
    this.subscriptionExpires = subscriptionExpires;
    this.topicId = Preconditions.checkNotNull(topicId);
    return this;
  }

  /**
   * Returns the notification callback called when a notification is received for this subscription.
   */
  public NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /**
   * Returns the Opaque string provided by the client or {@code null} for none.
   */
  public String getClientToken() {
    return clientToken;
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public String getSubscriptionExpires() {
    return subscriptionExpires;
  }

  /** Returns the subscription UUID. */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /** Returns the opaque ID for the subscribed resource that is stable across API versions. */
  public String getTopicId() {
    return topicId;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(Subscription.class)
        .add("notificationCallback", notificationCallback).add("clientToken", clientToken)
        .add("subscriptionExpires", subscriptionExpires).add("subscriptionID", subscriptionId)
        .add("topicID", topicId).toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Subscription)) {
      return false;
    }
    Subscription o = (Subscription) other;
    return subscriptionId.equals(o.subscriptionId);
  }

  @Override
  public int hashCode() {
    return subscriptionId.hashCode();
  }
}
