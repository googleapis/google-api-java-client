/*
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

import com.google.api.client.http.HttpResponse;
import com.google.common.base.Preconditions;


/**
 * Subscribe response.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public class SubscribeResponse {

  /** HTTP response. */
  private final HttpResponse response;

  /** Subscription or {@code null} for none. */
  private final Subscription subscription;

  /**
   * @param response HTTP response
   * @param subscription subscription or {@code null} for none
   */
  public SubscribeResponse(HttpResponse response, Subscription subscription) {
    this.response = Preconditions.checkNotNull(response);
    this.subscription = subscription;
  }

  /** Returns the opaque ID for the subscribed resource that is stable across API versions. */
  public final String getTopicId() {
    return SubscriptionHeaders.getTopicId(response.getHeaders());
  }

  /**
   * Returns the opaque ID (in the form of a canonicalized URI) for the subscribed resource that is
   * sensitive to the API version.
   */
  public final String getTopicUri() {
    return SubscriptionHeaders.getTopicUri(response.getHeaders());
  }

  /** Returns the subscription UUID. */
  public final String getSubscriptionId() {
    return SubscriptionHeaders.getSubscriptionId(response.getHeaders());
  }

  /**
   * Returns the HTTP Date indicating the time at which the subscription will expire returned in the
   * subscribe response or {@code null} for an infinite TTL.
   */
  public final String getSubscriptionExpires() {
    return SubscriptionHeaders.getSubscriptionExpires(response.getHeaders());
  }

  /**
   * Returns the client token (an opaque string) provided by the client or {@code null} for none.
   */
  public final String getClientToken() {
    return SubscriptionHeaders.getClientToken(response.getHeaders());
  }

  /** Returns the HTTP response. */
  public final HttpResponse getHttpResponse() {
    return response;
  }

  /** Returns the subscription or {@code null} for none. */
  public final Subscription getSubscription() {
    return subscription;
  }
}
