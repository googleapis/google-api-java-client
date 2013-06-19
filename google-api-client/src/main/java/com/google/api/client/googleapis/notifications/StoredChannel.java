/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.notifications;

import com.google.api.client.util.Beta;
import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Beta} <br/>
 * Notification channel information to be stored in a data store.
 *
 * <p>
 * Implementation is thread safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @author Matthias Linder (mlinder)
 * @since 1.16
 */
@Beta
public final class StoredChannel implements Serializable {

  /** Default data store ID. */
  public static final String DEFAULT_DATA_STORE_ID = StoredChannel.class.getSimpleName();

  private static final long serialVersionUID = 1L;

  /** Lock on access to the store. */
  private final Lock lock = new ReentrantLock();

  /** Notification callback called when a notification is received for this subscription. */
  private final UnparsedNotificationCallback notificationCallback;

  /**
   * Arbitrary string provided by the client associated with this subscription that is delivered to
   * the target address with each notification or {@code null} for none.
   */
  private String clientToken;

  /**
   * Milliseconds in Unix time at which the subscription will expire or {@code null} for an infinite
   * TTL.
   */
  private Long expiration;

  /** Subscription UUID. */
  private final String id;

  /**
   * Opaque ID for the subscribed resource that is stable across API versions or {@code null} for
   * none.
   */
  private String topicId;

  /**
   * Constructor with a random UUID using {@link NotificationUtils#randomUuidString()}.
   *
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   */
  public StoredChannel(UnparsedNotificationCallback notificationCallback) {
    this(notificationCallback, NotificationUtils.randomUuidString());
  }

  /**
   * Constructor with a custom UUID.
   *
   * @param notificationCallback notification handler called when a notification is received for
   *        this subscription
   * @param id subscription UUID
   */
  public StoredChannel(UnparsedNotificationCallback notificationCallback, String id) {
    this.notificationCallback = Preconditions.checkNotNull(notificationCallback);
    this.id = Preconditions.checkNotNull(id);
  }

  /**
   * Stores this notification channel in the notification channel data store, which is derived from
   * {@link #getDefaultDataStore(DataStoreFactory)} on the given data store factory.
   *
   * <p>
   * It is important that this method be called before the watch HTTP request is made in case the
   * notification is received before the watch HTTP response is received.
   * </p>
   *
   * @param dataStoreFactory data store factory
   */
  public StoredChannel store(DataStoreFactory dataStoreFactory) throws IOException {
    return store(getDefaultDataStore(dataStoreFactory));
  }

  /**
   * Stores this notification channel in the given notification channel data store.
   *
   * <p>
   * It is important that this method be called before the watch HTTP request is made in case the
   * notification is received before the watch HTTP response is received.
   * </p>
   *
   * @param dataStore notification channel data store
   */
  public StoredChannel store(DataStore<StoredChannel> dataStore) throws IOException {
    lock.lock();
    try {
      dataStore.set(getId(), this);
      return this;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the notification callback called when a notification is received for this subscription.
   */
  public UnparsedNotificationCallback getNotificationCallback() {
    lock.lock();
    try {
      return notificationCallback;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the arbitrary string provided by the client associated with this subscription that is
   * delivered to the target address with each notification or {@code null} for none.
   */
  public String getClientToken() {
    lock.lock();
    try {
      return clientToken;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the the arbitrary string provided by the client associated with this subscription that is
   * delivered to the target address with each notification or {@code null} for none.
   */
  public StoredChannel setClientToken(String clientToken) {
    lock.lock();
    try {
      this.clientToken = clientToken;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /**
   * Returns the milliseconds in Unix time at which the subscription will expire or {@code null} for
   * an infinite TTL.
   */
  public Long getExpiration() {
    lock.lock();
    try {
      return expiration;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the milliseconds in Unix time at which the subscription will expire or {@code null} for an
   * infinite TTL.
   */
  public StoredChannel setExpiration(Long expiration) {
    lock.lock();
    try {
      this.expiration = expiration;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /** Returns the subscription UUID. */
  public String getId() {
    lock.lock();
    try {
      return id;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public String getTopicId() {
    lock.lock();
    try {
      return topicId;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the opaque ID for the subscribed resource that is stable across API versions or
   * {@code null} for none.
   */
  public StoredChannel setTopicId(String topicId) {
    lock.lock();
    try {
      this.topicId = topicId;
    } finally {
      lock.unlock();
    }
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(StoredChannel.class)
        .add("notificationCallback", getNotificationCallback()).add("clientToken", getClientToken())
        .add("expiration", getExpiration()).add("id", getId()).add("topicId", getTopicId())
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StoredChannel)) {
      return false;
    }
    StoredChannel o = (StoredChannel) other;
    return getId().equals(o.getId());
  }

  @Override
  public int hashCode() {
    return getId().hashCode();
  }

  /**
   * Returns the stored channel data store using the ID {@link #DEFAULT_DATA_STORE_ID}.
   *
   * @param dataStoreFactory data store factory
   * @return stored channel data store
   */
  public static DataStore<StoredChannel> getDefaultDataStore(DataStoreFactory dataStoreFactory)
      throws IOException {
    return dataStoreFactory.getDataStore(DEFAULT_DATA_STORE_ID);
  }
}
