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
import com.google.api.client.http.HttpRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Manages subscription requests and delivers received notifications.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    SubscriptionStore store = new MemorySubscriptionStore();
    DeliveryMethod method = new WebhookDeliveryMethod("http://example.com");
    service.setSubscriptionManager(new SubscriptionManager(store, method));
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class SubscriptionManager {

  /** Subscription store used for storing subscriptions and delivering notifications. */
  private final SubscriptionStore subscriptionStore;

  /** Delivery method specifying the notification endpoint. */
  private final String deliveryMethod;

  /** Token generator used to generate the X-Client-Token. */
  private ClientTokenGenerator clientTokenGenerator = ClientTokenGenerator.DEFAULT_RANDOM_GENERATOR;

  /**
   * Creates an instance of the {@link SubscriptionManager} without subscription-ability.
   *
   * @param subscriptionStore Subscription store used for storing and retrieving subscriptions
   */
  public SubscriptionManager(SubscriptionStore subscriptionStore) {
    this(subscriptionStore, null);
  }

  /**
   * Creates an instance of the {@link SubscriptionManager}.
   *
   * @param subscriptionStore Subscription store used for storing and retrieving subscriptions
   * @param deliveryMethod Delivery method used when registering new subscriptions or {@code} null
   *        if unused
   */
  public SubscriptionManager(SubscriptionStore subscriptionStore, String deliveryMethod) {
    this.subscriptionStore = Preconditions.checkNotNull(subscriptionStore);
    this.deliveryMethod = deliveryMethod;
  }

  /**
   * Handles a newly received notification, and delegates it to the registered handler.
   *
   * @param notification An unparsed notification
   * @returns {@code true} if the notification was delivered successfully, or {@code false} if this
   *          notification could not be delivered and the subscription should be cancelled.
   * @throws IllegalArgumentException if there is a client-token mismatch
   */
  public boolean deliverNotification(UnparsedNotification notification) throws Exception {
    Preconditions.checkNotNull(notification);
    Preconditions.checkNotNull(notification.getSubscriptionID());

    // Find out the handler to whom this notification should go.
    Subscription subscription = subscriptionStore.getSubscription(notification.getSubscriptionID());
    if (subscription == null) {
      return false;
    }

    // Validate the notification.
    String expectedToken = subscription.getClientToken();
    Preconditions.checkArgument(
        Strings.isNullOrEmpty(expectedToken) || expectedToken.equals(notification.getClientToken()),
        "Token mismatch for subscription with id=%s -- got=%s expected=%s",
        notification.getSubscriptionID(), notification.getClientToken(), expectedToken);

    // Invoke the handler associated with this subscription.
    NotificationCallback h = subscription.getSubscriptionHandler();
    h.handleNotification(subscription, notification);
    return true;
  }

  /**
   * Returns the delivery method by which notifications are received.
   */
  public String getDeliveryMethod() {
    return deliveryMethod;
  }

  /**
   * Retrieves the data store which is used to save created subscriptions.
   */
  public SubscriptionStore getSubscriptionStore() {
    return subscriptionStore;
  }

  /**
   * Returns the {@link ClientTokenGenerator} used to generate random client tokens.
   *
   * <p>
   * The default value is {@link ClientTokenGenerator#DEFAULT_RANDOM_GENERATOR}.
   * </p>
   */
  public ClientTokenGenerator getClientTokenGenerator() {
    return clientTokenGenerator;
  }

  /**
   * Changes the {@link ClientTokenGenerator} used to generate random client tokens.
   *
   * <p>
   * The default value is {@link ClientTokenGenerator#DEFAULT_RANDOM_GENERATOR}.
   * </p>
   */
  public SubscriptionManager setClientTokenGenerator(ClientTokenGenerator clientTokenGenerator) {
    this.clientTokenGenerator = Preconditions.checkNotNull(clientTokenGenerator);
    return this;
  }

  /**
   * Adds the Subscribe header and a Client Token to a request.
   *
   * @param request Request to modify
   * @param clientToken ClientToken which is passed along with the request.
   */
  public void addSubscriptionRequestHeaders(HttpRequest request, String clientToken) {
    // Retrieve the delivery method. Make sure that a push manager has been set.
    String deliveryMethod = Preconditions.checkNotNull(getDeliveryMethod());

    // Inject the subscription headers into the request.
    HttpHeaders headers = request.getHeaders();
    headers.set(SubscriptionHeaders.CLIENT_TOKEN, clientToken);
    headers.set(SubscriptionHeaders.SUBSCRIBE, deliveryMethod);
  }

  /**
   * Adds the Subscribe header and a Client Token generated by the token generator specified in the
   * push manager to the request.
   *
   * @param request Request to modify
   */
  public void addSubscriptionRequestHeaders(HttpRequest request) throws Exception {
    addSubscriptionRequestHeaders(request, getClientTokenGenerator().generateToken());
  }

  /**
   * Completes a subscription request and returns the registered subscription.
   *
   * @param responseHeaders Headers of the response to the subscription request
   * @param handler The handler which should be registered for all push notifications.
   */
  public Subscription processSubscribeResponse(
      HttpHeaders responseHeaders, NotificationCallback handler) throws Exception {
    // Retrieve the necessary headers.
    SubscriptionHeaders pushHeaders = new SubscriptionHeaders(responseHeaders);
    String subscriptionId = pushHeaders.getSubscriptionID();
    String topicId = pushHeaders.getTopicID();
    String clientToken = pushHeaders.getClientToken();

    Preconditions.checkArgument(subscriptionId != null,
        "Could not complete subscription request: Did not receive SubscriptionID");

    Preconditions.checkNotNull(topicId);

    // Create and register the subscription.
    Subscription subscription = new Subscription(subscriptionId, handler, clientToken);
    getSubscriptionStore().storeSubscription(subscription);
    return subscription;
  }
}
