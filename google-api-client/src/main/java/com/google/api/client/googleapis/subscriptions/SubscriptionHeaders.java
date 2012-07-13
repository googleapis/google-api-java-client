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

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Key;

/**
 * Contains all header constants related to subscriptions.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    HttpResponse response = request.execute();
    SubscriptionHeaders headers = new SubscriptionHeaders(response.getHeaders());
    System.out.println(headers.getSubscriptionId());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class SubscriptionHeaders extends GoogleHeaders {

  /**
   * The Subscribe header used to create new subscriptions.
   */
  public static final String SUBSCRIBE = "X-Subscribe";

  /**
   * The Subscription ID which uniquely identifies this subscription.
   */
  public static final String SUBSCRIPTION_ID = "X-Subscription-ID";

  /**
   * The Topic ID which identifies what you subscribed to.
   */
  public static final String TOPIC_ID = "X-Topic-ID";

  /**
   * The Topic URL which represents a more readable version of the TopicID.
   */
  public static final String TOPIC_URI = "X-Topic-URI";

  /**
   * The Client-Token which is used to verify the origin of a notification.
   */
  public static final String CLIENT_TOKEN = "X-Client-Token";

  /**
   * The Event-Type which describes the action which was performed on a resource.
   */
  public static final String EVENT_TYPE = "X-Event-Type";

  /**
   * The Header sent when the user wants to remove a subscription. Value is be the subscription ID.
   */
  public static final String UNSUBSCRIBE = "X-Unsubscribe";

  /** {@code "X-Subscribe"} header. */
  @Key(SubscriptionHeaders.SUBSCRIBE)
  private String subscribe;

  /** {@code "X-Subscription-ID"} header. */
  @Key(SubscriptionHeaders.SUBSCRIPTION_ID)
  private String subscriptionID;

  /** {@code "X-Topic-ID"} header. */
  @Key(SubscriptionHeaders.TOPIC_ID)
  private String topicID;

  /** {@code "X-Topic-URI"} header. */
  @Key(SubscriptionHeaders.TOPIC_URI)
  private String topicUri;

  /** {@code "X-Event-Type"} header. */
  @Key(SubscriptionHeaders.EVENT_TYPE)
  private String eventType;

  /** {@code "X-Client-Token"} header. */
  @Key(SubscriptionHeaders.CLIENT_TOKEN)
  private String clientToken;

  /** {@code "X-Unsubscribe"} header. */
  @Key(SubscriptionHeaders.UNSUBSCRIBE)
  private String unsubscribe;

  /**
   * Creates a PushHeaders object using the headers present in the specified {@link HttpHeaders}.
   *
   * @param headers HttpHeaders object including set headers
   */
  public SubscriptionHeaders(HttpHeaders headers) {
    this.fromHttpHeaders(headers);
  }

  /**
   * Returns the {@code "X-Subscription-ID"} header.
   */
  public String getSubscriptionID() {
    return subscriptionID;
  }

  /**
   * Sets the {@code "X-Subscription-ID"} header.
   */
  public void setSubscriptionID(String subscriptionID) {
    this.subscriptionID = subscriptionID;
  }

  /**
   * Returns the {@code "X-Topic-ID"} header.
   */
  public String getTopicID() {
    return topicID;
  }

  /**
   * Sets the {@code "X-Topic-ID"} header.
   */
  public void setTopicID(String topicID) {
    this.topicID = topicID;
  }

  /**
   * Returns the {@code "X-Topic-URI"} header.
   */
  public String getTopicUri() {
    return topicUri;
  }

  /**
   * Sets the {@code "X-Topic-URI"} header.
   */
  public void setTopicUri(String topicUri) {
    this.topicUri = topicUri;
  }

  /**
   * Returns the {@code "X-Event-Type"} header.
   */
  public String getEventType() {
    return eventType;
  }

  /**
   * Sets the {@code "X-Event-Type"} header.
   */
  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  /**
   * Returns the {@code "X-Client-Token"} header.
   */
  public String getClientToken() {
    return clientToken;
  }

  /**
   * Sets the {@code "X-Client-Token"} header.
   */
  public void setClientToken(String clienToken) {
    this.clientToken = clienToken;
  }

  /**
   * Returns the {@code "X-Subscribe"} header.
   */
  public String getSubscribe() {
    return subscribe;
  }

  /**
   * Sets the {@code "X-Subscribe"} header.
   */
  public void setSubscribe(String subscribe) {
    this.subscribe = subscribe;
  }

  /**
   * Returns the {@code "X-Unsubscribe"} header.
   */
  public String getUnsubscribe() {
    return unsubscribe;
  }

  /**
   * Sets the {@code "X-Unsubscribe"} header.
   */
  public void setUnsubscribe(String unsubscribe) {
    this.unsubscribe = unsubscribe;
  }
}
