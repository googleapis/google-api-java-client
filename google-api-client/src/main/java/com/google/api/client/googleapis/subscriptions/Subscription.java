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

import java.io.Serializable;

/**
 * Data type containing essential information about a subscription. Usually stored in a
 * {@link SubscriptionStore}.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    void handleNotification(Subscription subscription, UnparsedNotification notification) {
      System.out.println(subscription.getSubscriptionId());
    }
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class Subscription implements Serializable {

  private static final long serialVersionUID = 1L;

  /** ID of the subscription. */
  private final String subscriptionID;

  /** The token used to verify the authority of the notifications' origin or {@code null}. */
  private final String clientToken;

  /** The handler to call once a notification for this subscription has been received. */
  private final NotificationCallback subscriptionHandler;

  /**
   * Creates a new subscription.
   *
   * @param id ID of the subscription
   * @param handler The handler to call once a notification for this subscription has been
   *                received. Should be thread-safe.
   * @param clientToken The token used to verify the authority of the notifications' origin, or
   *                    {@code null} if unused
   */
  public <T> Subscription(String id, NotificationCallback handler, String clientToken) {
    this.subscriptionID = Preconditions.checkNotNull(id);
    this.subscriptionHandler = Preconditions.checkNotNull(handler);
    this.clientToken = clientToken;
  }

  /**
   * Returns the ID of this subscription which was received as the subscription was made.
   */
  public final String getSubscriptionID() {
    return subscriptionID;
  }

  /**
   * Returns the handler which should be called for every received {@link Notification}.
   */
  public final NotificationCallback getSubscriptionHandler() {
    return subscriptionHandler;
  }

  /**
   * Returns the ClientToken with which this subscription was created, or {@code null} if unused.
   *
   * <p>
   * The ClientToken can be considered as some kind of 'salt' which is used to ensure that the
   * {@link Notification} has been received from the endpoint where the subscription was made. It is
   * especially useful for delivery-methods like WebHook where any possible source might call the
   * servlet.
   * </p>
   */
  public final String getClientToken() {
    return clientToken;
  }
}
