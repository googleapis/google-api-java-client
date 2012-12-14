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

import com.google.common.base.Preconditions;


/**
 * Notification sent to this client about a subscribed resource.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public abstract class Notification {

  /** Subscription UUID. */
  private final String subscriptionId;

  /** Opaque ID for the subscribed resource that is stable across API versions. */
  private final String topicId;

  /**
   * Opaque ID (in the form of a canonicalized URI) for the subscribed resource that is sensitive to
   * the API version.
   */
  private final String topicURI;

  /** Client token (an opaque string) or {@code null} for none. */
  private final String clientToken;

  /** Message number (a monotonically increasing value starting with 1). */
  private final long messageNumber;

  /** Event type (see {@link EventTypes}). */
  private final String eventType;

  /** Type of change performed on the resource or {@code null} for none. */
  private final String changeType;

  /**
   * @param subscriptionId subscription UUID
   * @param topicId opaque ID for the subscribed resource that is stable across API versions
   * @param topicURI opaque ID (in the form of a canonicalized URI) for the subscribed resource that
   *        is sensitive to the API version
   * @param clientToken client token (an opaque string) or {@code null} for none
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param eventType event type (see {@link EventTypes})
   * @param changeType type of change performed on the resource or {@code null} for none
   */
  protected Notification(String subscriptionId, String topicId, String topicURI, String clientToken,
      long messageNumber, String eventType, String changeType) {
    this.subscriptionId = Preconditions.checkNotNull(subscriptionId);
    this.topicId = Preconditions.checkNotNull(topicId);
    this.topicURI = Preconditions.checkNotNull(topicURI);
    this.eventType = Preconditions.checkNotNull(eventType);
    this.clientToken = clientToken;
    Preconditions.checkArgument(messageNumber >= 1);
    this.messageNumber = messageNumber;
    this.changeType = changeType;
  }

  /**
   * Creates a new notification by copying all information specified in the source notification.
   *
   * @param source notification whose information is copied
   */
  protected Notification(Notification source) {
    this(source.getSubscriptionId(), source.getTopicId(), source.getTopicURI(), source
        .getClientToken(), source.getMessageNumber(), source.getEventType(), source
        .getChangeType());
  }

  /** Returns the subscription UUID. */
  public final String getSubscriptionId() {
    return subscriptionId;
  }

  /** Returns the opaque ID for the subscribed resource that is stable across API versions. */
  public final String getTopicId() {
    return topicId;
  }

  /** Returns the client token (an opaque string) or {@code null} for none. */
  public final String getClientToken() {
    return clientToken;
  }

  /** Returns the event type (see {@link EventTypes}). */
  public final String getEventType() {
    return eventType;
  }

  /**
   * Returns the opaque ID (in the form of a canonicalized URI) for the subscribed resource that is
   * sensitive to the API version.
   */
  public final String getTopicURI() {
    return topicURI;
  }

  /** Returns the message number (a monotonically increasing value starting with 1). */
  public final long getMessageNumber() {
    return messageNumber;
  }

  /** Returns the type of change performed on the resource or {@code null} for none. */
  public final String getChangeType() {
    return changeType;
  }
}
