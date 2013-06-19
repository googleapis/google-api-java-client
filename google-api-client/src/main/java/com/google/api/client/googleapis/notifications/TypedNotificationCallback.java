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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.util.Beta;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * {@link Beta} <br/>
 * Callback to receive notifications for watched resource in which the . Callback which is used to
 * receive typed {@link AbstractNotification}s after subscribing to a topic.
 *
 * <p>
 * Must NOT be implemented in form of an anonymous class as this will break serialization.
 * </p>
 *
 * <p>
 * Implementation should be thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
  static class MyNotificationCallback
      extends JsonNotificationCallback{@literal <}ListResponse{@literal >} {

    private static final long serialVersionUID = 1L;

    {@literal @}Override
    protected void onNotification(
        StoredChannel subscription, Notification notification, ListResponse content) {
      switch (notification.getResourceState()) {
        case ResourceStates.SYNC:
          break;
        case ResourceStates.EXISTS:
          break;
        case ResourceStates.NOT_EXISTS:
          break;
      }
    }

    {@literal @}Override
    protected ObjectParser getObjectParser(Notification notification) throws IOException {
      return new JsonObjectParser(new JacksonFactory());
    }

    {@literal @}Override
    protected Class{@literal <}ListResponse{@literal >} getDataClass() throws IOException {
      return ListResponse.class;
    }
  }
 * </pre>
 *
 * @param <T> Java type of the notification content
 * @author Yaniv Inbar
 * @author Matthias Linder (mlinder)
 * @since 1.16
 */
@Beta
public abstract class TypedNotificationCallback<T> implements UnparsedNotificationCallback {

  private static final long serialVersionUID = 1L;

  /**
   * Handles a received typed notification.
   *
   * @param storedChannel stored notification channel
   * @param notification typed notification
   */
  protected abstract void onNotification(
      StoredChannel storedChannel, TypedNotification<T> notification) throws IOException;

  /** Returns an {@link ObjectParser} which can be used to parse this notification. */
  protected abstract ObjectParser getObjectParser() throws IOException;

  /**
   * Returns the data class to parse the notification content into or {@code Void.class} if no
   * notification content is expected.
   */
  protected abstract Class<T> getDataClass() throws IOException;

  public final void onNotification(StoredChannel storedChannel, UnparsedNotification notification)
      throws IOException {
    TypedNotification<T> typedNotification = new TypedNotification<T>(notification);
    // TODO(yanivi): how to properly detect if there is no content?
    String contentType = notification.getContentType();
    if (contentType != null) {
      Charset charset = new HttpMediaType(contentType).getCharsetParameter();
      Class<T> dataClass = Preconditions.checkNotNull(getDataClass());
      typedNotification.setContent(
          getObjectParser().parseAndClose(notification.getContentStream(), charset, dataClass));
    }
    onNotification(storedChannel, typedNotification);
  }
}
