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

import java.io.IOException;
import java.io.Serializable;

/**
 * Callback which is used to receive {@link UnparsedNotification}s after subscribing to a topic.
 *
 * <p>
 * Must not be implemented in form of an anonymous class as this will break serialization.
 * </p>
 *
 * <p>
 * Should be thread-safe as several notifications might be processed at the same time.
 * </p>
 *
 * <p>
 * State will only be persisted once when a subscription is created. All state changes occurring
 * during the {@code .handleNotification(..)} call will be lost.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    class MyNotificationCallback extends NotificationCallback {
      void handleNotification(
          Subscription subscription, UnparsedNotification notification) {
         if (notification.getEventType().equals(EventTypes.UPDATED)) {
          // add items in the notification to the local client state ...
        }
      }
    }

    ...

    myRequest.subscribe(new MyNotificationCallback()).execute();
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public interface NotificationCallback extends Serializable {

  /**
   * Handles a received push notification.
   *
   * @param subscription Subscription to which this notification belongs
   * @param notification Notification which was delivered to this application
   */
  void handleNotification(Subscription subscription, UnparsedNotification notification)
      throws IOException;
}
