/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.notifications;

import com.google.api.client.util.Beta;

import java.io.IOException;
import java.io.Serializable;

/**
 * {@link Beta} <br/>
 * Callback to receive unparsed notifications for watched resource.
 *
 * <p>
 * Must NOT be implemented in form of an anonymous class since this would break serialization.
 * </p>
 *
 * <p>
 * Should be thread-safe as several notifications might be processed at the same time.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
  static class MyNotificationCallback implements UnparsedNotificationCallback {

    private static final long serialVersionUID = 1L;

    {@literal @}Override
    public void onNotification(StoredChannel storedChannel, UnparsedNotification notification) {
      String contentType = notification.getContentType();
      InputStream contentStream = notification.getContentStream();
      switch (notification.getResourceState()) {
        case ResourceStates.SYNC:
          break;
        case ResourceStates.EXISTS:
          break;
        case ResourceStates.NOT_EXISTS:
          break;
      }
    }
  }
 * </pre>
 *
 * @author Yaniv Inbar
 * @author Matthias Linder (mlinder)
 * @since 1.16
 */
@Beta
public interface UnparsedNotificationCallback extends Serializable {

  /**
   * Handles a received unparsed notification.
   *
   * @param storedChannel stored notification channel
   * @param notification unparsed notification
   */
  void onNotification(StoredChannel storedChannel, UnparsedNotification notification)
      throws IOException;
}
