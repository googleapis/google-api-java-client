/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.extensions.servlet.notifications;

import com.google.api.client.googleapis.notifications.ResourceStates;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Headers for Webhook notifications.
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public final class WebhookHeaders {

  /** Name of header for the message number (a monotonically increasing value starting with 1). */
  public static final String MESSAGE_NUMBER = "X-Goog-Message-Number";

  /** Name of header for the {@link ResourceStates resource state}. */
  public static final String RESOURCE_STATE = "X-Goog-Resource-State";

  /**
   * Name of header for the opaque ID for the watched resource that is stable across API versions.
   */
  public static final String RESOURCE_ID = "X-Goog-Resource-ID";

  /**
   * Name of header for the opaque ID (in the form of a canonicalized URI) for the watched resource
   * that is sensitive to the API version.
   */
  public static final String RESOURCE_URI = "X-Goog-Resource-URI";

  /**
   * Name of header for the notification channel UUID provided by the client in the watch request.
   */
  public static final String CHANNEL_ID = "X-Goog-Channel-ID";

  /** Name of header for the notification channel expiration time. */
  public static final String CHANNEL_EXPIRATION = "X-Goog-Channel-Expiration";

  /**
   * Name of header for the notification channel token (an opaque string) provided by the client in
   * the watch request.
   */
  public static final String CHANNEL_TOKEN = "X-Goog-Channel-Token";

  /** Name of header for the type of change performed on the resource. */
  public static final String CHANGED = "X-Goog-Changed";

  private WebhookHeaders() {
  }
}
