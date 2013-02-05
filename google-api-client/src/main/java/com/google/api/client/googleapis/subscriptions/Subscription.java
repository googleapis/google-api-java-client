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

import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;

import java.io.Serializable;
import java.util.UUID;

/**
 * Client subscription information.
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

  /** Opaque string provided by the client or {@code null} for none. */
  private String clientToken;

  /**
   * HTTP Date indicating the time at which the subscription will expire or {@code null} for an
   * infinite TTL.
   */
  private String subscriptionExpires;

  /** Subscription UUID. */
  private final String subscriptionId;

  /**
   * Opaque ID for the subscribed resource that is stable across API versions or {@code null} for
   * none.
   */
  private String topicId;

  /**
   * Constructor with a random UUID using {@link #randomId()}.
   * 
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   */
  public Subscription(NotificationCallback notificationCallback) {
    this(notificationCallback, randomId());
  }

  /**
   * Constructor with a custom UUID.
   * 
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   * @param subscriptionId subscription UUID
   */
  public Subscription(NotificationCallback notificationCallback, String subscriptionId) {
    this.notificationCallback = Preconditions.checkNotNull(notificationCallback);
    this.subscriptionId = Preconditions.checkNotNull(subscriptionId);
  }

  /**
   * Returns the notification callback called when a notification is received for this subscription.
   */
  public NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /** Returns the opaque string provided by the client or {@code null} for none. */
  public String getClientToken() {
    return clientToken;
  }

  /** Sets the opaque string provided by the client or {@code null} for none. */
  public Subscription setClientToken(String clientToken) {
    this.clientToken = clientToken;
    return this;
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public String getSubscriptionExpires() {
    return subscriptionExpires;
  }

  /**
   * Sets the HTTP Date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public Subscription setSubscriptionExpires(String subscriptionExpires) {
    this.subscriptionExpires = subscriptionExpires;
    return this;
  }

  /** Returns the subscription UUID. */
  public String getSubscriptionId() {
    return subscriptionId;
  }

  /**
   * Returns the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public String getTopicId() {
    return topicId;
  }

  /**
   * Sets the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public Subscription setTopicId(String topicId) {
    this.topicId = topicId;
    return this;
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

  /** Returns a new random UUID to be used as a subscription ID. */
  public static String randomId() {
    return UUID.randomUUID().toString();
  }
}
