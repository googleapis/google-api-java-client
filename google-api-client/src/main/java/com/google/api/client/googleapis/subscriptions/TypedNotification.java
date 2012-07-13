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
 * A typed notification which was sent to this application.
 *
 * <p>
 * Contains information about subscription ID, topic ID and URI, and the parsed content of the
 * notification.
 * </p>
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    void handleNotification(
        Subscription subscription, TypedNotification&lt;ItemList&gt; notification) {
      for (Item item in notification.getContent().getItems()) {
        System.out.println(item.getId());
      }
    }
 * </pre>
 *
 * @param <T> Data content from the underlying response stored within this notification
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class TypedNotification<T> extends Notification {

  /** Typed content of this notification or {@code null}. */
  private final T content;

  /**
   * @return content of this notification
   */
  public final T getContent() {
    return content;
  }

  /**
   * Creates a notification containing subscription information by using the {@link Notification} as
   * a basis.
   *
   * @param notification The notification whose subscription info is used
   * @param content The new (parsed) content
   */
  public TypedNotification(Notification notification, T content) {
    super(notification);
    this.content = content;
  }

  /**
   * Creates a notification containing information as well as the typed data content.
   * @param subscriptionID The subscription ID to which this notification is being sent
   * @param topicID The topic ID to which this subscription belongs
   * @param topicURI URI of the topic
   * @param content The normal data content of this notification or {@code null}
   * @param eventType Type of event which was performed on the resource
   * @param clientToken The token which is used for verification and was passed along the response,
   *        or {@code null}
   */
  public TypedNotification(String subscriptionID,
      String topicID,
      String topicURI,
      String clientToken,
      String eventType,
      T content) {
    super(subscriptionID, topicID, topicURI, clientToken, eventType);
    this.content = content;
  }
}
