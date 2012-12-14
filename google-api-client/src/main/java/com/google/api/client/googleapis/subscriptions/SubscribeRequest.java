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

import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.UUID;

/**
 * Subscribe request.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public class SubscribeRequest {

  /** HTTP request. */
  private final HttpRequest request;

  /** Notification callback or {@code null} for none. */
  private NotificationCallback notificationCallback;

  /** Subscription store or {@code null} for none. */
  private SubscriptionStore subscriptionStore;

  /** Stored subscription. */
  Subscription subscription;

  /**
   * @param request HTTP GET request
   * @param notificationDeliveryMethod notification delivery method
   */
  public SubscribeRequest(HttpRequest request, String notificationDeliveryMethod) {
    this.request = Preconditions.checkNotNull(request);
    Preconditions.checkArgument(HttpMethods.GET.equals(request.getRequestMethod()));
    request.setRequestMethod(HttpMethods.POST);
    request.setContent(new EmptyContent());
    setNotificationDeliveryMethod(notificationDeliveryMethod);
    setSubscriptionId(UUID.randomUUID().toString());
  }

  /**
   * Sets the subscription store and notification callback associated with this subscription.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param subscriptionStore subscription store
   * @param notificationCallback notification callback
   */
  public SubscribeRequest withNotificationCallback(
      final SubscriptionStore subscriptionStore, final NotificationCallback notificationCallback) {
    Preconditions.checkArgument(this.notificationCallback == null);
    this.subscriptionStore = Preconditions.checkNotNull(subscriptionStore);
    this.notificationCallback = Preconditions.checkNotNull(notificationCallback);
    // execute interceptor
    final HttpExecuteInterceptor executeInterceptor = request.getInterceptor();
    request.setInterceptor(new HttpExecuteInterceptor() {

      public void intercept(HttpRequest request) throws IOException {
        if (subscription == null) {
          subscription =
              new Subscription(notificationCallback, getClientToken(), getSubscriptionId());
          subscriptionStore.storeSubscription(subscription);
        }
        if (executeInterceptor != null) {
          executeInterceptor.intercept(request);
        }
      }
    });
    // response interceptor
    final HttpResponseInterceptor responseInterceptor = request.getResponseInterceptor();
    request.setResponseInterceptor(new HttpResponseInterceptor() {

      public void interceptResponse(HttpResponse response) throws IOException {
        HttpHeaders headers = response.getHeaders();
        Preconditions.checkArgument(
            SubscriptionHeaders.getSubscriptionId(headers).equals(getSubscriptionId()));
        if (response.isSuccessStatusCode()) {
          subscription.processResponse(SubscriptionHeaders.getSubscriptionExpires(headers),
              SubscriptionHeaders.getTopicId(headers));
          subscriptionStore.storeSubscription(subscription);
        } else {
          subscriptionStore.removeSubscription(subscription);
        }
        if (responseInterceptor != null) {
          responseInterceptor.interceptResponse(response);
        }
      }
    });
    return this;
  }

  /**
   * Sets the subscription store and typed notification callback associated with this subscription.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param subscriptionStore subscription store
   * @param notificationCallbackClass data class the successful notification will be parsed into or
   *        {@code Void.class} to ignore the content
   * @param typedNotificationCallback typed notification callback
   */
  public <N> SubscribeRequest withTypedNotificationCallback(SubscriptionStore subscriptionStore,
      Class<N> notificationCallbackClass, TypedNotificationCallback<N> typedNotificationCallback) {
    withNotificationCallback(subscriptionStore, typedNotificationCallback);
    typedNotificationCallback.setDataType(notificationCallbackClass);
    return this;
  }

  /** Returns the subscribe HTTP request. */
  public final HttpRequest getRequest() {
    return request;
  }

  /** Returns the notifications delivery method. */
  public final String getNotificationDeliveryMethod() {
    return SubscriptionHeaders.getSubscribe(request.getHeaders());
  }

  /**
   * Sets the notifications delivery method.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public SubscribeRequest setNotificationDeliveryMethod(String notificationDeliveryMethod) {
    SubscriptionHeaders.setSubscribe(
        request.getHeaders(), Preconditions.checkNotNull(notificationDeliveryMethod));
    return this;
  }

  /** Returns the notification callback or {@code null} for none. */
  public final NotificationCallback getNotificationCallback() {
    return notificationCallback;
  }

  /** Returns the subscription store or {@code null} for none. */
  public final SubscriptionStore getSubscriptionStore() {
    return subscriptionStore;
  }

  /**
   * Returns the client token (an opaque string) provided by the client or {@code null} for none.
   */
  public String getClientToken() {
    return SubscriptionHeaders.getClientToken(request.getHeaders());
  }

  /**
   * Sets the client token (an opaque string) provided by the client or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public SubscribeRequest setClientToken(String clientToken) {
    SubscriptionHeaders.setClientToken(request.getHeaders(), clientToken);
    return this;
  }

  /** Returns the subscription UUID provided by the client. */
  public String getSubscriptionId() {
    return SubscriptionHeaders.getSubscriptionId(request.getHeaders());
  }

  /**
   * Sets the subscription UUID provided by the client.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public SubscribeRequest setSubscriptionId(String subscriptionId) {
    SubscriptionHeaders.setSubscriptionId(
        request.getHeaders(), Preconditions.checkNotNull(subscriptionId));
    return this;
  }

  /**
   * Executes the subscribe request.
   *
   * @return subscribe response
   */
  public SubscribeResponse execute() throws IOException {
    HttpResponse response = request.execute();
    return new SubscribeResponse(response, subscription);
  }

  /**
   * Queues the subscribe request into the specified batch request container.
   *
   * <p>
   * Batched requests are then executed when {@link BatchRequest#execute()} is called.
   * </p>
   *
   * @param batchRequest batch request container
   * @param errorClass data class the unsuccessful response will be parsed into or
   *        {@code Void.class} to ignore the content
   * @param callback batch callback
   */
  public final <E> void queue(
      BatchRequest batchRequest, Class<E> errorClass, BatchCallback<Void, E> callback)
      throws IOException {
    batchRequest.queue(request, Void.class, errorClass, callback);
  }
}
