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
 * Standard event-types used by notifications.
 *
 * <b>Example usage:</b>
 *
 * <pre>
    void handleNotification(Subscription subscription, UnparsedNotification notification) {
      if (notification.getEventType().equals(EventTypes.UPDATED)) {
        // add items in the notification to the local client state ...
      }
    }
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public final class EventTypes {

  /** Notification that the subscription is alive (comes with no payload). */
  public static final String SYNC = "sync";

  /** Resource was modified. */
  public static final String UPDATED = "updated";

  /** Resource was deleted. */
  public static final String DELETED = "deleted";

  /** Private constructor to prevent instantiation. */
  private EventTypes() {
  }
}
