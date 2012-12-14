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

package com.google.api.client.googleapis.extensions.servlet.subscriptions;

import com.google.api.client.http.GenericUrl;
import com.google.common.base.Preconditions;

/**
 * Builds delivery method strings for subscribing to notifications via WebHook.
 *
 * <p>
 * Should be used in conjunction with a {@link AbstractWebHookServlet}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
  private static final String DELIVERY_METHOD =
      new WebHookDeliveryMethod("https://example.com/notifications").build();

  ...

    request.subscribe(DELIVERY_METHOD, ...).execute();
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public class WebHookDeliveryMethod {

  /** The URL which is being built. */
  private GenericUrl url;

  /**
   * Builds delivery method strings for subscribing to notifications via WebHook.
   *
   * @param callbackURL The full URL of the registered Notification-Servlet. Should start with
   *        https://.
   */
  public WebHookDeliveryMethod(String callbackURL) {
    // Build the Callback URL and verify it.
    Preconditions.checkArgument(new GenericUrl(callbackURL).getScheme().equalsIgnoreCase("https"),
        "Callback scheme has to be https://");

    // Build the Subscription URL. Only the path and query parameters will actually get.
    GenericUrl url = new GenericUrl("http://example.com/web_hook");
    url.set("url", callbackURL);
    this.url = url;
  }

  /**
   * Returns the URL this builder is currently building.
   */
  public final GenericUrl getUrl() {
    return url;
  }

  /**
   * Builds and returns the resulting delivery method string.
   */
  public final String build() {
    return url.buildRelativeUrl().substring("/".length());
  }

  /**
   * Gets the host query parameter.
   */
  public final String getHost() {
    return (String) url.get("host");
  }

  /**
   * Sets the host query parameter.
   *
   * @param host New value for the host parameter
   */
  public WebHookDeliveryMethod setHost(String host) {
    url.set("host", host);
    return this;
  }

  /**
   * Returns {@code} true if notifications should contain a payload, or {@code false} if only
   * invalidations should be received.
   *
   * <p>
   * Default is {@code true}.
   * </p>
   */
  public final boolean isPayloadRequested() {
    return !Boolean.valueOf((String) url.get("invalidate"));
  }

  /**
   * Sets whether notifications should contain a payload, or whether only
   * invalidations should be received.
   *
   * <p>
   * Default is {@code true}.
   * </p>
   */
  public WebHookDeliveryMethod setPayloadRequested(boolean isPayloadRequested) {
    url.set("invalidate", String.valueOf(!isPayloadRequested));
    return this;
  }
}
