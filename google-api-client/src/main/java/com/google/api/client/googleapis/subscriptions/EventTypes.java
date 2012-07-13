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
 * <pre>
    void handleNotification(Subscription subscription, UnparsedNotification notification) {
      if (notification.getEventType().equals(EventTypes.ADDED)) {
        // add items in the notification to the local client state ...
      }
    }
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class EventTypes {

  /** Sent when a resource was modified, but when DELETED or ADDED did not apply. */
  public static final String UPDATED = "updated";

  /** Sent when the subscribed-to resource has been deleted. */
  public static final String DELETED = "deleted";

  /** Sent when an item has been added to the subscribed-to collection. */
  public static final String ADDED = "added";

  /** Sent when an item has been removed from the subscribed-to collection. */
  public static final String REMOVED = "removed";

  /** Private constructor to prevent instantiation. */
  private EventTypes() {}
}
