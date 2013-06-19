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

package com.google.api.client.googleapis.notifications.json;

import com.google.api.client.googleapis.notifications.TypedNotificationCallback;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Beta;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * A {@link TypedNotificationCallback} which uses an JSON content encoding.
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
        StoredChannel channel, TypedNotification{@literal <}ListResponse{@literal >} notification) {
      ListResponse content = notification.getContent();
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
    protected JsonFactory getJsonFactory() throws IOException {
      return new JacksonFactory();
    }

    {@literal @}Override
    protected Class{@literal <}ListResponse{@literal >} getDataClass() throws IOException {
      return ListResponse.class;
    }
  }
 * </pre>
 *
 * @param <T> Type of the data contained within a notification
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public abstract class JsonNotificationCallback<T> extends TypedNotificationCallback<T> {

  private static final long serialVersionUID = 1L;

  @Override
  protected final JsonObjectParser getObjectParser() throws IOException {
    return new JsonObjectParser(getJsonFactory());
  }

  /** Returns the JSON factory to use to parse the notification content. */
  protected abstract JsonFactory getJsonFactory() throws IOException;
}
