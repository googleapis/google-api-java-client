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
 * Contains all header constants related to notifications.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Kyle Marvin (kmarvin)
 * @since 1.13
 */
public class NotificationHeaders {

  /**
   * Header name for the textual indication of the type of event that occurred to the underlying
   * resource or topic provided on notification delivery.
   */
  public static final String EVENT_TYPE_HEADER = "X-Goog-Event-Type";
  /**
   * Header name for the textual indication of the type of change that occurred to the underlying
   * resource or topic provided on notification delivery.
   */
  public static final String CHANGED_HEADER = "X-Goog-Changed";
  /**
   * Header name for the message number provided on notification delivery.   The message number is
   * a monotonically increasing value for notifications associated with a subscription.
   */
  public static final String MESSAGE_NUMBER_HEADER = "X-Goog-Message-Number";

}
