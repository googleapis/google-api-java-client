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
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;

/**
 * A notification whose content has not been parsed yet.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    void handleNotification(Subscription subscription, UnparsedNotification notification)
        throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(notification.getContent()));
      System.out.println(reader.readLine());
      reader.close();
    }
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public final class UnparsedNotification extends Notification {

  /** The input stream containing the content. */
  private final InputStream content;

  /** The content-type of the stream or {@code null} if not specified. */
  private final String contentType;

  /**
   * Creates a {@link Notification} whose content has not yet been read and parsed.
   *
   * @param subscriptionId subscription UUID
   * @param topicId opaque ID for the subscribed resource that is stable across API versions
   * @param topicURI opaque ID (in the form of a canonicalized URI) for the subscribed resource that
   *        is sensitive to the API version
   * @param clientToken client token (an opaque string) or {@code null} for none
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param eventType event type (see {@link EventTypes})
   * @param changeType type of change performed on the resource or {@code null} for none
   * @param unparsedStream Unparsed content in form of a {@link InputStream}. Caller has the
   *        responsibility of closing the stream.
   */
  public UnparsedNotification(String subscriptionId, String topicId, String topicURI,
      String clientToken, long messageNumber, String eventType, String changeType,
      String contentType, InputStream unparsedStream) {
    super(subscriptionId, topicId, topicURI, clientToken, messageNumber, eventType, changeType);
    this.contentType = contentType;
    this.content = Preconditions.checkNotNull(unparsedStream);
  }

  /**
   * Returns the Content-Type of the Content of this notification.
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Returns the content stream of this notification.
   */
  public final InputStream getContent() {
    return content;
  }

  /**
   * Handles a newly received notification, and delegates it to the registered handler.
   *
   * @param subscriptionStore subscription store
   * @return {@code true} if the notification was delivered successfully, or {@code false} if this
   *         notification could not be delivered and the subscription should be cancelled.
   * @throws IllegalArgumentException if there is a client-token mismatch
   */
  public boolean deliverNotification(SubscriptionStore subscriptionStore) throws IOException {
    // Find out the handler to whom this notification should go.
    Subscription subscription =
        subscriptionStore.getSubscription(Preconditions.checkNotNull(getSubscriptionId()));
    if (subscription == null) {
      return false;
    }
    // Validate the notification.
    String expectedToken = subscription.getClientToken();
    Preconditions.checkArgument(
        Strings.isNullOrEmpty(expectedToken) || expectedToken.equals(getClientToken()),
        "Token mismatch for subscription with id=%s -- got=%s expected=%s", getSubscriptionId(),
        getClientToken(), expectedToken);
    // Invoke the handler associated with this subscription.
    NotificationCallback h = subscription.getNotificationCallback();
    h.handleNotification(subscription, this);
    return true;
  }
}
