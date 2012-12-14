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


/**
 * Typed notification sent to this client about a subscribed resource.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    void handleNotification(
        Subscription subscription, TypedNotification&lt;ItemList&gt; notification) {
      for (Item item : notification.getContent().getItems()) {
        System.out.println(item.getId());
      }
    }
 * </pre>
 *
 * @param <T> Data content from the underlying response stored within this notification
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public final class TypedNotification<T> extends Notification {

  /** Typed content or {@code null} for none. */
  private final T content;

  /** Returns the typed content or {@code null} for none. */
  public final T getContent() {
    return content;
  }

  /**
   * @param notification notification whose information is copied
   * @param content typed content or {@code null} for none
   */
  public TypedNotification(Notification notification, T content) {
    super(notification);
    this.content = content;
  }

  /**
   * @param subscriptionId subscription UUID
   * @param topicId opaque ID for the subscribed resource that is stable across API versions
   * @param topicURI opaque ID (in the form of a canonicalized URI) for the subscribed resource that
   *        is sensitive to the API version
   * @param clientToken client token (an opaque string) or {@code null} for none
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param eventType event type (see {@link EventTypes})
   * @param changeType type of change performed on the resource or {@code null} for none
   * @param content typed content or {@code null} for none
   */
  public TypedNotification(String subscriptionId, String topicId, String topicURI,
      String clientToken, long messageNumber, String eventType, String changeType, T content) {
    super(subscriptionId, topicId, topicURI, clientToken, messageNumber, eventType, changeType);
    this.content = content;
  }
}
