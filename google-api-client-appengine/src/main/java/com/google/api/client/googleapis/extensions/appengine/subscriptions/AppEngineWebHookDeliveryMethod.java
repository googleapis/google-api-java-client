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

package com.google.api.client.googleapis.extensions.appengine.subscriptions;

import com.google.api.client.googleapis.extensions.servlet.subscriptions.AbstractWebHookServlet;
import com.google.api.client.googleapis.extensions.servlet.subscriptions.WebHookDeliveryMethod;

/**
 * Builds delivery method strings for subscribing to notifications via WebHook on AppEngine.
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
      new AppEngineWebHookDeliveryMethod("https://example.appspot.com/notifications").build();

  ...

    request.subscribe(DELIVERY_METHOD, ...).execute();
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
public class AppEngineWebHookDeliveryMethod extends WebHookDeliveryMethod {

  /**
   * Builds delivery method strings for subscribing to notifications via WebHook on AppEngine.
   *
   * @param callbackUrl The full URL of the registered Notification-Servlet. Should start with
   *        https://.
   */
  public AppEngineWebHookDeliveryMethod(String callbackUrl) {
    super(callbackUrl);
    getUrl().set("appEngine", "true");
  }

  @Override
  public AppEngineWebHookDeliveryMethod setHost(String host) {
    super.setHost(host);
    return this;
  }

  @Override
  public AppEngineWebHookDeliveryMethod setPayloadRequested(boolean isPayloadRequested) {
    super.setPayloadRequested(isPayloadRequested);
    return this;
  }
}
