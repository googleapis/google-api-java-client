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

package com.google.api.client.googleapis.notifications.json.gson;

import com.google.api.client.googleapis.notifications.TypedNotificationCallback;
import com.google.api.client.googleapis.notifications.json.JsonNotificationCallback;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br>
 * A {@link TypedNotificationCallback} which uses a JSON content encoding with {@link
 * GsonFactory#getDefaultInstance()}.
 *
 * <p>Must NOT be implemented in form of an anonymous class as this will break serialization.
 *
 * <p>Implementation should be thread-safe. <b>Example usage:</b>
 *
 * <pre>{@code
 * static class MyNotificationCallback
 *     extends GsonNotificationCallback{@literal <}ListResponse{@literal >} {
 *
 *   private static final long serialVersionUID = 1L;
 *
 *   {@literal @}Override
 *   protected void onNotification(StoredChannel channel,
 *       TypedNotification{@literal <}ListResponse{@literal >} notification) {
 *     ListResponse content = notification.getContent();
 *     switch (notification.getResourceState()) {
 *       case ResourceStates.SYNC:
 *         break;
 *       case ResourceStates.EXISTS:
 *         break;
 *       case ResourceStates.NOT_EXISTS:
 *         break;
 *     }
 *   }
 *
 *   {@literal @}Override
 *   protected Class{@literal <}ListResponse{@literal >} getDataClass() throws IOException {
 *     return ListResponse.class;
 *   }
 * }
 * }</pre>
 *
 * @param <T> Type of the data contained within a notification
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public abstract class GsonNotificationCallback<T> extends JsonNotificationCallback<T> {

  private static final long serialVersionUID = 1L;

  @Override
  protected JsonFactory getJsonFactory() {
    return GsonFactory.getDefaultInstance();
  }
}
