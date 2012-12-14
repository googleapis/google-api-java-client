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

package com.google.api.client.googleapis.subscriptions.json;

import com.google.api.client.googleapis.subscriptions.TypedNotificationCallback;
import com.google.api.client.googleapis.subscriptions.UnparsedNotification;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.ObjectParser;

import java.io.IOException;

/**
 * A {@link TypedNotificationCallback} which uses an JSON content encoding.
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
    class MyJsonNotificationCallback extends JsonNotificationCallback&lt;ItemList&gt; {
      void handleNotification(
          Subscription subscription, TypedNotification&lt;ItemList&gt; notification) {
        for (Item item in notification.getContent().getItems()) {
          System.out.println(item.getId());
        }
      }
    }

    JsonFactory createJsonFactory() {
      return new JacksonFactory();
    }

    ...

    service.items.list("someID").subscribe(new MyJsonNotificationCallback()).execute()
 * </pre>
 *
 * @param <T> Type of the data contained within a notification
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
@SuppressWarnings("serial")
public abstract class JsonNotificationCallback<T> extends TypedNotificationCallback<T> {

  /** JSON factory used to deserialize notifications. {@code null} until first used. */
  private transient JsonFactory jsonFactory;

  /**
   * Returns the JSON-factory used by this handler.
   */
  public final JsonFactory getJsonFactory() throws IOException {
    if (jsonFactory == null) {
      jsonFactory = createJsonFactory();
    }
    return jsonFactory;
  }

  // TODO(mlinder): Don't have the user supply the JsonFactory, but serialize it instead.
  /**
   * Creates a new JSON factory which is used to deserialize notifications.
   */
  protected abstract JsonFactory createJsonFactory() throws IOException;

  @Override
  protected final ObjectParser getParser(UnparsedNotification notification) throws IOException {
    return new JsonObjectParser(getJsonFactory());
  }

  @Override
  public JsonNotificationCallback<T> setDataType(Class<T> dataClass) {
    return (JsonNotificationCallback<T>) super.setDataType(dataClass);
  }
}
