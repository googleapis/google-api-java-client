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

package com.google.api.client.googleapis.subscriptions.json;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.googleapis.subscriptions.NotificationCallback;
import com.google.api.client.googleapis.subscriptions.SubscribeRequest;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.api.client.googleapis.subscriptions.TypedNotificationCallback;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * Subscribe JSON request.
 *
 * @author Yaniv Inbar
 * @since 1.14
 */
public class JsonSubscribeRequest extends SubscribeRequest {

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /**
   * @param request HTTP GET request
   * @param notificationDeliveryMethod notification delivery method
   * @param jsonFactory JSON factory
   */
  public JsonSubscribeRequest(
      HttpRequest request, String notificationDeliveryMethod, JsonFactory jsonFactory) {
    super(request, notificationDeliveryMethod);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
  }

  @Override
  public JsonSubscribeRequest withNotificationCallback(
      SubscriptionStore subscriptionStore, NotificationCallback notificationCallback) {
    return (JsonSubscribeRequest) super.withNotificationCallback(
        subscriptionStore, notificationCallback);
  }

  @Override
  public <N> JsonSubscribeRequest withTypedNotificationCallback(SubscriptionStore subscriptionStore,
      Class<N> notificationCallbackClass, TypedNotificationCallback<N> typedNotificationCallback) {
    return (JsonSubscribeRequest) super.withTypedNotificationCallback(
        subscriptionStore, notificationCallbackClass, typedNotificationCallback);
  }

  @Override
  public JsonSubscribeRequest setNotificationDeliveryMethod(String notificationDeliveryMethod) {
    return (JsonSubscribeRequest) super.setNotificationDeliveryMethod(notificationDeliveryMethod);
  }

  @Override
  public JsonSubscribeRequest setClientToken(String clientToken) {
    return (JsonSubscribeRequest) super.setClientToken(clientToken);
  }

  @Override
  public JsonSubscribeRequest setSubscriptionId(String subscriptionId) {
    return (JsonSubscribeRequest) super.setSubscriptionId(subscriptionId);
  }

  /**
   * Sets the subscription store and JSON notification callback associated with this subscription.
   *
   * @param subscriptionStore subscription store
   * @param notificationCallbackClass data class the successful notification will be parsed into or
   *        {@code Void.class} to ignore the content
   * @param jsonNotificationCallback JSON notification callback
   */
  public <N> JsonSubscribeRequest withJsonNotificationCallback(SubscriptionStore subscriptionStore,
      Class<N> notificationCallbackClass, JsonNotificationCallback<N> jsonNotificationCallback) {
    return withTypedNotificationCallback(
        subscriptionStore, notificationCallbackClass, jsonNotificationCallback);
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Queues the subscribe JSON request into the specified batch request container.
   *
   * <p>
   * Batched requests are then executed when {@link BatchRequest#execute()} is called.
   * </p>
   *
   * <p>
   * Example usage:
   * </p>
   *
   * <pre>
     request.queue(batchRequest, new JsonBatchCallback&lt;Void&gt;() {

       public void onSuccess(Void ignored, HttpHeaders responseHeaders) {
         log("Success");
       }

       public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
         log(e.getMessage());
       }
     });
   * </pre>
   *
   *
   * @param batchRequest batch request container
   * @param callback batch callback
   */
  public final void queue(BatchRequest batchRequest, JsonBatchCallback<Void> callback)
      throws IOException {
    super.queue(batchRequest, GoogleJsonErrorContainer.class, callback);
  }
}
