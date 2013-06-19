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

/**
 * {@link Beta} <br/>
 * Notification metadata and parsed content sent to this client about a watched resource.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @param <T> Java type of the notification content
 *
 * @author Yaniv Inbar
 * @author Matthias Linder (mlinder)
 * @since 1.16
 */
@Beta
public class TypedNotification<T> extends AbstractNotification {

  /** Parsed notification content or {@code null} for none. */
  private T content;

  /**
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param resourceState {@link ResourceStates resource state}
   * @param resourceId opaque ID for the watched resource that is stable across API versions
   * @param resourceUri opaque ID (in the form of a canonicalized URI) for the watched resource that
   *        is sensitive to the API version
   * @param channelId notification channel UUID provided by the client in the watch request
   */
  public TypedNotification(long messageNumber, String resourceState, String resourceId,
      String resourceUri, String channelId) {
    super(messageNumber, resourceState, resourceId, resourceUri, channelId);
  }

  /**
   * @param sourceNotification source notification metadata to copy
   */
  public TypedNotification(UnparsedNotification sourceNotification) {
    super(sourceNotification);
  }

  /**
   * Returns the parsed notification content or {@code null} for none.
   */
  public final T getContent() {
    return content;
  }

  /**
   * Sets the parsed notification content or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TypedNotification<T> setContent(T content) {
    this.content = content;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setMessageNumber(long messageNumber) {
    return (TypedNotification<T>) super.setMessageNumber(messageNumber);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setResourceState(String resourceState) {
    return (TypedNotification<T>) super.setResourceState(resourceState);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setResourceId(String resourceId) {
    return (TypedNotification<T>) super.setResourceId(resourceId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setResourceUri(String resourceUri) {
    return (TypedNotification<T>) super.setResourceUri(resourceUri);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setChannelId(String channelId) {
    return (TypedNotification<T>) super.setChannelId(channelId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setChannelExpiration(String channelExpiration) {
    return (TypedNotification<T>) super.setChannelExpiration(channelExpiration);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setChannelToken(String channelToken) {
    return (TypedNotification<T>) super.setChannelToken(channelToken);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TypedNotification<T> setChanged(String changed) {
    return (TypedNotification<T>) super.setChanged(changed);
  }

  @Override
  public String toString() {
    return super.toStringHelper().add("content", content).toString();
  }
}
