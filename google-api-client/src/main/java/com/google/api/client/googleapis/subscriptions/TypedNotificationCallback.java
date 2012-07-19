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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.util.ObjectParser;

import java.nio.charset.Charset;

/**
 * Callback which is used to receive typed @{link Notification}s after subscribing to a topic.
 *
 * <p>
 * Must not be implemented in form of an anonymous class as this will break serialization.
 * </p>
 *
 * <p>
 * Implementation should be thread-safe.
 * </p>
 *
 * <p>
 * State will only be persisted once when a subscription is created. All state changes occurring
 * during the {@code .handleNotification(..)} call will be lost.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    class MyTypedNotificationCallback extends TypedNotificationCallback&lt;ItemList&gt; {
      void handleNotification(
          Subscription subscription, TypedNotification&lt;ItemList&gt; notification) {
        for (Item item in notification.getContent().getItems()) {
          System.out.println(item.getId());
        }
      }
    }

    ObjectParser getParser(UnparsedNotification notification) {
      return new JacksonFactory().createJsonObjectParser();
    }

    ...

    service.items.list("someID").subscribe(new MyTypedNotificationCallback()).execute()
 * </pre>
 *
 * @param <T> Type of the data contained within a notification
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
@SuppressWarnings("serial")
public abstract class TypedNotificationCallback<T> implements NotificationCallback {

  /** Data class of the expected content. */
  private Class<T> dataClass;

  /**
   * Returns the data type which this handler can parse.
   */
  public final Class<T> getDataClass() {
    return dataClass;
  }

  /**
   * Sets the data type which this handler can parse or {@code null}/{@code Void.class} if no data
   * type is expected.
   */
  public final void setDataType(Class<T> dataClass) {
    this.dataClass = dataClass;
  }

  /**
   * Creates a new blank TypedSubscriptionHandler.
   */
  public TypedNotificationCallback() {
  }

  /**
   * Handles a received push notification.
   *
   * @param subscription Subscription to which this notification belongs
   * @param notification Typed notification which was delivered to this application
   */
  protected abstract void handleNotification(
      Subscription subscription, TypedNotification<T> notification) throws Exception;

  /**
   * Returns an {@link ObjectParser} which can be used to parse this notification.
   *
   * @param notification Notification which should be parsable by the returned parser
   */
  protected abstract ObjectParser getParser(UnparsedNotification notification)
      throws Exception;

  /** Parses the specified content and closes the InputStream of the notification. */
  private Object parseContent(ObjectParser parser, UnparsedNotification notification)
      throws Exception {
    // Return null if no content is expected
    if (dataClass == null || notification.getContentType() == null
        || Void.class.equals(dataClass)) {
      return null;
    }

    // Parse the response otherwise
    Charset charset = notification.getContentType() == null ? null : new HttpMediaType(
        notification.getContentType()).getCharsetParameter();
    return parser.parseAndClose(notification.getContent(), charset, dataClass);
  }

  public void handleNotification(
      Subscription subscription, UnparsedNotification notification) throws Exception {
    ObjectParser parser = getParser(notification);
    @SuppressWarnings("unchecked")
    T content = (T) parseContent(parser, notification);
    handleNotification(subscription, new TypedNotification<T>(notification, content));
  }
}
