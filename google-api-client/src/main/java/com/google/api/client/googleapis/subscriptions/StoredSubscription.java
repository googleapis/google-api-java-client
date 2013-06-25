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

import com.google.api.client.googleapis.notifications.StoredChannel;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;

import java.io.Serializable;
import java.util.UUID;

/**
 * {@link Beta} <br/>
 * Client subscription information to be stored in a {@link SubscriptionStore}.
 *
 * <p>
 * Implementation is thread safe.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 * @deprecated (scheduled to be removed in 1.17) Use {@link StoredChannel} instead.
 */
@Deprecated
@Beta
public final class StoredSubscription implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Notification callback called when a notification is received for this subscription. */
  private final NotificationCallback notificationCallback;

  /**
   * Arbitrary string provided by the client associated with this subscription that is delivered to
   * the target address with each notification or {@code null} for none.
   */
  private String clientToken;

  /**
   * HTTP date indicating the time at which the subscription will expire or {@code null} for an
   * infinite TTL.
   */
  private String expiration;

  /** Subscription UUID. */
  private final String id;

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
  public StoredSubscription(NotificationCallback notificationCallback) {
    this(notificationCallback, randomId());
  }

  /**
   * Constructor with a custom UUID.
   *
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   * @param id subscription UUID
   */
  public StoredSubscription(NotificationCallback notificationCallback, String id) {
    this.notificationCallback = Preconditions.checkNotNull(notificationCallback);
    this.id = Preconditions.checkNotNull(id);
  }

  /**
   * Constructor based on a JSON-formatted subscription response information.
   *
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   * @param subscriptionJson JSON-formatted subscription response information, where the:
   *        <ul>
   *        <li>{@code "id"} has a JSON string value for the subscription ID</li>
   *        <li>{@code "clientToken"} has a JSON string value for the client token (see
   *        {@link #setClientToken(String)})</li>
   *        <li>{@code "expiration"} has a JSON string value for the client token (see
   *        {@link #setExpiration(String)})</li>
   *        <li>{@code "topicId"} has a JSON string value for the client token (see
   *        {@link #setTopicId(String)})</li>
   *        </ul>
   */
  public StoredSubscription(
      NotificationCallback notificationCallback, GenericJson subscriptionJson) {
    this(notificationCallback, (String) subscriptionJson.get("id"));
    setClientToken((String) subscriptionJson.get("clientToken"));
    setExpiration((String) subscriptionJson.get("expiration"));
    setTopicId((String) subscriptionJson.get("topicId"));
  }

  /**
   * Returns the notification callback called when a notification is received for this subscription.
   */
  public synchronized NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /**
   * Returns the arbitrary string provided by the client associated with this subscription that is
   * delivered to the target address with each notification or {@code null} for none.
   */
  public synchronized String getClientToken() {
    return clientToken;
  }

  /**
   * Sets the the arbitrary string provided by the client associated with this subscription that is
   * delivered to the target address with each notification or {@code null} for none.
   */
  public synchronized StoredSubscription setClientToken(String clientToken) {
    this.clientToken = clientToken;
    return this;
  }

  /**
   * Returns the HTTP date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public synchronized String getExpiration() {
    return expiration;
  }

  /**
   * Sets the HTTP date indicating the time at which the subscription will expire or {@code null}
   * for an infinite TTL.
   */
  public synchronized StoredSubscription setExpiration(String expiration) {
    this.expiration = expiration;
    return this;
  }

  /** Returns the subscription UUID. */
  public synchronized String getId() {
    return id;
  }

  /**
   * Returns the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public synchronized String getTopicId() {
    return topicId;
  }

  /**
   * Sets the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public synchronized StoredSubscription setTopicId(String topicId) {
    this.topicId = topicId;
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(StoredSubscription.class)
        .add("notificationCallback", getNotificationCallback()).add("clientToken", getClientToken())
        .add("expiration", getExpiration()).add("id", getId()).add("topicId", getTopicId())
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StoredSubscription)) {
      return false;
    }
    StoredSubscription o = (StoredSubscription) other;
    return getId().equals(o.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  /** Returns a new random UUID to be used as a subscription ID. */
  public static String randomId() {
    return UUID.randomUUID().toString();
  }
}
