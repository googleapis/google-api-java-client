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

import com.google.api.client.util.Experimental;

/**
 * {@link Experimental} <br/>
 * Headers for notifications.
 *
 * @author Kyle Marvin (kmarvin)
 * @since 1.14
 */
@Experimental
public final class NotificationHeaders {

  /**
   * Name of header for the client token (an opaque string) provided by the client in the subscribe
   * request and returned in the subscribe response.
   */
  public static final String CLIENT_TOKEN = "X-Goog-Client-Token";

  /**
   * Name of header for the subscription UUID provided by the client in the subscribe request and
   * returned in the subscribe response.
   */
  public static final String SUBSCRIPTION_ID = "X-Goog-Subscription-ID";

  /**
   * Name of header for the opaque ID for the subscribed resource that is stable across API versions
   * returned in the subscribe response.
   */
  public static final String TOPIC_ID = "X-Goog-Topic-ID";

  /**
   * Name of header for the opaque ID (in the form of a canonicalized URI) for the subscribed
   * resource that is sensitive to the API version returned in the subscribe response.
   */
  public static final String TOPIC_URI = "X-Goog-Topic-URI";

  /** Name of header for the event type (see {@link EventTypes}). */
  public static final String EVENT_TYPE_HEADER = "X-Goog-Event-Type";

  /**
   * Name of header for the type of change performed on the resource.
   */
  public static final String CHANGED_HEADER = "X-Goog-Changed";

  /**
   * Name of header for the message number (a monotonically increasing value starting with 1).
   */
  public static final String MESSAGE_NUMBER_HEADER = "X-Goog-Message-Number";

  private NotificationHeaders() {
  }
}
