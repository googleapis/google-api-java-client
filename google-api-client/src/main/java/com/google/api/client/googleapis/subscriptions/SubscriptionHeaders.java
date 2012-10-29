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
 *
 * <pre>
    HttpResponse response = request.execute();
    SubscriptionHeaders headers = new SubscriptionHeaders(response.getHeaders());
    System.out.println(headers.getSubscriptionId());
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.13
 */
public final class SubscriptionHeaders extends GoogleHeaders {

  /**
   * Header name for the notifications delivery method provided by the client when it creates the
   * subscription.
   */
  public static final String SUBSCRIBE = "X-Subscribe";

  /**
   * Header name for the unique subscription ID (found in subscription response).
   */
  public static final String SUBSCRIPTION_ID = "X-Subscription-ID";

  /**
   * Header name for the opaque ID for the subscribed resource that is stable across API versions
   * (found in subscription response).
   */
  public static final String TOPIC_ID = "X-Topic-ID";

  /**
   * Header name for the opaque ID (in the form of a canonicalized URI) for the subscribed resource
   * that sensitive to the API version (found in subscription response).
   */
  public static final String TOPIC_URI = "X-Topic-URI";

  /**
   * Header name for the opaque string provided by the client when it creates the subscription and
   * echoed back to the client for every notification it receives for that subscription.
   */
  public static final String CLIENT_TOKEN = "X-Client-Token";

  /**
   * Header sent that provides an HTTP Date indicating the time at which the subscription will
   * expire (found in subscription response) or if not provided the subscription is assumed to have
   * an infinite TTL.
   */
  public static final String SUBSCRIPTION_EXPIRES = "X-Subscription-Expires";

  /**
   * Notifications delivery method provided by the client when it creates the subscription or
   * {@code null} for none.
   */
  @Key(SubscriptionHeaders.SUBSCRIBE)
  private String subscribe;

  /**
   * Unique subscription ID (found in subscription response) or {@code null} for none.
   */
  @Key(SubscriptionHeaders.SUBSCRIPTION_ID)
  private String subscriptionID;

  /**
   * Opaque ID for the subscribed resource that is stable across API versions (in subscription
   * response) or {@code null} for none.
   */
  @Key(SubscriptionHeaders.TOPIC_ID)
  private String topicID;

  /**
   * Opaque ID (in the form of a canonicalized URI) for the subscribed resource that sensitive to
   * the API version (found in subscription response) or {@code null} for none.
   */
  @Key(SubscriptionHeaders.TOPIC_URI)
  private String topicUri;

  /**
   * Opaque string provided by the client when it creates the subscription and echoed back to the
   * client for every notification it receives for that subscription or {@code null} for none.
   */
  @Key(SubscriptionHeaders.CLIENT_TOKEN)
  private String clientToken;

  /**
   * HTTP Date indicating the time at which the subscription will expire (found in subscription
   * response) or {@code null} for an infinite TTL.
   */
  @Key(SubscriptionHeaders.SUBSCRIPTION_EXPIRES)
  private String subscriptionExpires;

  public SubscriptionHeaders() {
  }

  /**
   * @param headers headers to copy from
   */
  public SubscriptionHeaders(HttpHeaders headers) {
    fromHttpHeaders(headers);
  }

  /**
   * Returns the unique subscription ID (found in subscription response) or {@code null} for none.
   */
  public String getSubscriptionID() {
    return subscriptionID;
  }

  /** Sets the unique subscription ID (found in subscription response) or {@code null} for none. */
  public SubscriptionHeaders setSubscriptionID(String subscriptionID) {
    this.subscriptionID = subscriptionID;
    return this;
  }

  /**
   * Returns the opaque ID for the subscribed resource that is stable across API versions (in
   * subscription response) or {@code null} for none.
   */
  public String getTopicID() {
    return topicID;
  }

  /**
   * Sets the opaque ID for the subscribed resource that is stable across API versions (in
   * subscription response) or {@code null} for none.
   */
  public SubscriptionHeaders setTopicID(String topicID) {
    this.topicID = topicID;
    return this;
  }

  /**
   * Returns the opaque ID (in the form of a canonicalized URI) for the subscribed resource that
   * sensitive to the API version (found in subscription response) or {@code null} for none.
   */
  public String getTopicUri() {
    return topicUri;
  }

  /**
   * Sets the opaque ID (in the form of a canonicalized URI) for the subscribed resource that
   * sensitive to the API version (found in subscription response) or {@code null} for none.
   */
  public SubscriptionHeaders setTopicUri(String topicUri) {
    this.topicUri = topicUri;
    return this;
  }

  /**
   * Returns the opaque string provided by the client when it creates the subscription and echoed
   * back to the client for every notification it receives for that subscription or {@code null} for
   * none.
   */
  public String getClientToken() {
    return clientToken;
  }

  /**
   * Sets the opaque string provided by the client when it creates the subscription and echoed back
   * to the client for every notification it receives for that subscription or {@code null} for
   * none.
   */
  public SubscriptionHeaders setClientToken(String clienToken) {
    this.clientToken = clienToken;
    return this;
  }

  /**
   * Returns the notifications delivery method provided by the client when it creates the
   * subscription or {@code null} for none.
   */
  public String getSubscribe() {
    return subscribe;
  }

  /**
   * Sets the notifications delivery method provided by the client when it creates the subscription
   * or {@code null} for none.
   */
  public SubscriptionHeaders setSubscribe(String subscribe) {
    this.subscribe = subscribe;
    return this;
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire (found in
   * subscription response) or {@code null} for an infinite TTL.
   */
  public String getSubscriptionExpires() {
    return subscriptionExpires;
  }

  /**
   * Sets the HTTP Date indicating the time at which the subscription will expire (found in
   * subscription response) or {@code null} for an infinite TTL.
   */
  public SubscriptionHeaders setSubscriptionExpires(String subscriptionExpires) {
    this.subscriptionExpires = subscriptionExpires;
    return this;
  }
}
