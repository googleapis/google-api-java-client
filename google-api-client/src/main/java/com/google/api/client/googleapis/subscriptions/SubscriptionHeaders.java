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

import com.google.api.client.http.HttpHeaders;


/**
 * Headers for subscribe request and response.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public final class SubscriptionHeaders {

  /**
   * Name of header for the client token (an opaque string) provided by the client in the subscribe
   * request and returned in the subscribe response.
   */
  public static final String CLIENT_TOKEN = "X-Goog-Client-Token";

  /**
   * Name of header for the notifications delivery method in the subscribe request.
   */
  public static final String SUBSCRIBE = "X-Goog-Subscribe";

  /**
   * Name of header for the HTTP Date indicating the time at which the subscription will expire
   * returned in the subscribe response or if not returned for an infinite TTL.
   */
  public static final String SUBSCRIPTION_EXPIRES = "X-Goog-Subscription-Expires";

  /**
   * Name of header for the subscription UUID provided by the client in the subscribe request and
   * returned in the subscribe response.
   */
  public static final String SUBSCRIPTION_ID = "X-Goog-Subscription-ID";

  /**
   * Name of header for the opaque ID for the subscribed resource that is stable across API versions
   * returned in the subscribe response.
   */
  public static final String TOPIC_ID = "X-Goog-Topic-ID";

  /**
   * Name of header for the opaque ID (in the form of a canonicalized URI) for the subscribed
   * resource that is sensitive to the API version returned in the subscribe response.
   */
  public static final String TOPIC_URI = "X-Goog-Topic-URI";

  /**
   * Returns the client token (an opaque string) provided by the client in the subscribe request and
   * returned in the subscribe response or {@code null} for none.
   */
  public static String getClientToken(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(CLIENT_TOKEN);
  }

  /**
   * Sets the client token (an opaque string) provided by the client in the subscribe request and
   * returned in the subscribe response or {@code null} for none.
   */
  public static void setClientToken(HttpHeaders headers, String clienToken) {
    headers.set(CLIENT_TOKEN, clienToken);
  }

  /**
   * Returns the notifications delivery method in the subscribe request or {@code null} for none.
   *
   * @param headers HTTP headers
   */
  public static String getSubscribe(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(SUBSCRIBE);
  }

  /**
   * Sets the notifications delivery method in the subscribe request or {@code null} for none.
   */
  public static void setSubscribe(HttpHeaders headers, String subscribe) {
    headers.set(SUBSCRIBE, subscribe);
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire returned in the
   * subscribe response or {@code null} for an infinite TTL.
   */
  public static String getSubscriptionExpires(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(SUBSCRIPTION_EXPIRES);
  }

  /**
   * Sets the HTTP Date indicating the time at which the subscription will expire returned in the
   * subscribe response or {@code null} for an infinite TTL.
   */
  public static void setSubscriptionExpires(HttpHeaders headers, String subscriptionExpires) {
    headers.set(SUBSCRIPTION_EXPIRES, subscriptionExpires);
  }

  /**
   * Returns the subscription UUID provided by the client in the subscribe request and returned in
   * the subscribe response or {@code null} for none.
   */
  public static String getSubscriptionId(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(SUBSCRIPTION_ID);
  }

  /**
   * Sets the subscription UUID provided by the client in the subscribe request and returned in the
   * subscribe response or {@code null} for none.
   */
  public static void setSubscriptionId(HttpHeaders headers, String subscriptionId) {
    headers.set(SUBSCRIPTION_ID, subscriptionId);
  }

  /**
   * Returns the opaque ID for the subscribed resource that is stable across API versions returned
   * in the subscribe response or {@code null} for none.
   */
  public static String getTopicId(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(TOPIC_ID);
  }

  /**
   * Sets the opaque ID for the subscribed resource that is stable across API versions returned in
   * the subscribe response or {@code null} for none.
   */
  public static void setTopicId(HttpHeaders headers, String topicId) {
    headers.set(TOPIC_ID, topicId);
  }

  /**
   * Returns the opaque ID (in the form of a canonicalized URI) for the subscribed resource that is
   * sensitive to the API version returned in the subscribe response or {@code null} for none.
   */
  public static String getTopicUri(HttpHeaders headers) {
    return headers.getFirstHeaderStringValue(TOPIC_URI);
  }

  /**
   * Sets the opaque ID (in the form of a canonicalized URI) for the subscribed resource that is
   * sensitive to the API version returned in the subscribe response or {@code null} for none.
   */
  public static void setTopicUri(HttpHeaders headers, String topicUri) {
    headers.set(TOPIC_URI, topicUri);
  }

  private SubscriptionHeaders() {
  }
}
