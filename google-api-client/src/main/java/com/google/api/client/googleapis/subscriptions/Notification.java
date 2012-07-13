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
 * A notification which was sent to this application.
 *
 * <p>
 * Contains information about subscription ID, topic ID and URI.
 * </p>
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <p>
 * This class is extended by {@link TypedNotification} and {@link UnparsedNotification}.
 * </p>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public abstract class Notification {

  /** SubscriptionID header. */
  private final String subscriptionID;

  /** TopicID header. */
  private final String topicID;

  /** TopicURI header. */
  private final String topicURI;

  /** ConfirmationToken which was passed when subscribing to the service. */
  private final String clientToken;

  /** Name of the event which was performed on the resource. */
  private final String eventType;

  /**
   * Creates a notification containing push information as well as the normal data content.
   *
   * @param subscriptionID The subscription ID to which this notification is being sent
   * @param topicID The topic ID to which this subscription belongs
   * @param topicURI URI of the topic
   * @param clientToken The token which is used for verification and was passed along the response,
   *        or {@code null}
   * @param eventType Type of event which was performed on the resource
   */
  protected Notification(String subscriptionID, String topicID, String topicURI, String clientToken,
      String eventType) {
    this.subscriptionID = Preconditions.checkNotNull(subscriptionID);
    this.topicID = Preconditions.checkNotNull(topicID);
    this.topicURI = Preconditions.checkNotNull(topicURI);
    this.eventType = Preconditions.checkNotNull(eventType);
    this.clientToken = clientToken;
  }

  /**
   * Creates a new notification by copying all information specified in the source notification.
   *
   * @param source notification whose information is copied
   */
  protected Notification(Notification source) {
    this(source.getSubscriptionID(), source.getTopicID(), source.getTopicURI(), source
        .getClientToken(), source.getEventType());
  }

  /** Returns the subscription ID to which this notification belongs. */
  public final String getSubscriptionID() {
    return subscriptionID;
  }

  /** Returns the topic ID on which this notification was broadcasted. */
  public final String getTopicID() {
    return topicID;
  }

  /** Returns the client token received with this notification or {@code null} for none. */
  public final String getClientToken() {
    return clientToken;
  }

  /** Returns the event type of this notification (for example {@code "added"}). */
  public final String getEventType() {
    return eventType;
  }

  /** Returns the topic URI. */
  public final String getTopicURI() {
    return topicURI;
  }
}
