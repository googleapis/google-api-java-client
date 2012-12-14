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

package com.google.api.client.googleapis.testing.subscriptions;

import com.google.api.client.googleapis.subscriptions.NotificationCallback;
import com.google.api.client.googleapis.subscriptions.Subscription;
import com.google.api.client.googleapis.subscriptions.UnparsedNotification;

/**
 * Mock for the {@link NotificationCallback} class.
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
@SuppressWarnings("rawtypes")
public class MockNotificationCallback implements NotificationCallback {

  private static final long serialVersionUID = 0L;

  /** True if this handler was called. */
  private boolean wasCalled = false;

  /** Returns {@code true} if this handler was called. */
  public boolean wasCalled() {
    return wasCalled;
  }

  public MockNotificationCallback() {
  }

  public void handleNotification(
      Subscription subscription, UnparsedNotification notification) {
    wasCalled = true;
  }
}
