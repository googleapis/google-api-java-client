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
import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;

/**
 * {@link Beta} <br/>
 * Notification metadata sent to this client about a watched resource.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @author Matthias Linder (mlinder)
 * @since 1.16
 */
@Beta
public abstract class AbstractNotification {

  /** Message number (a monotonically increasing value starting with 1). */
  private long messageNumber;

  /** {@link ResourceStates Resource state}. */
  private String resourceState;

  /** Opaque ID for the watched resource that is stable across API versions. */
  private String resourceId;

  /**
   * Opaque ID (in the form of a canonicalized URI) for the watched resource that is sensitive to
   * the API version.
   */
  private String resourceUri;

  /** Notification channel UUID provided by the client in the watch request. */
  private String channelId;

  /** Notification channel expiration time or {@code null} for none. */
  private String channelExpiration;

  /**
   * Notification channel token (an opaque string) provided by the client in the watch request or
   * {@code null} for none.
   */
  private String channelToken;

  /** Type of change performed on the resource or {@code null} for none. */
  private String changed;

  /**
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param resourceState {@link ResourceStates resource state}
   * @param resourceId opaque ID for the watched resource that is stable across API versions
   * @param resourceUri opaque ID (in the form of a canonicalized URI) for the watched resource that
   *        is sensitive to the API version
   * @param channelId notification channel UUID provided by the client in the watch request
   */
  protected AbstractNotification(long messageNumber, String resourceState, String resourceId,
      String resourceUri, String channelId) {
    setMessageNumber(messageNumber);
    setResourceState(resourceState);
    setResourceId(resourceId);
    setResourceUri(resourceUri);
    setChannelId(channelId);
  }

  /** Copy constructor based on a source notification object. */
  protected AbstractNotification(AbstractNotification source) {
    this(source.getMessageNumber(), source.getResourceState(), source.getResourceId(), source
        .getResourceUri(), source.getChannelId());
    setChannelExpiration(source.getChannelExpiration());
    setChannelToken(source.getChannelToken());
    setChanged(source.getChanged());
  }

  @Override
  public String toString() {
    return toStringHelper().toString();
  }

  /** Returns the helper for {@link #toString()}. */
  protected Objects.ToStringHelper toStringHelper() {
    return Objects.toStringHelper(this).add("messageNumber", messageNumber)
        .add("resourceState", resourceState).add("resourceId", resourceId)
        .add("resourceUri", resourceUri).add("channelId", channelId)
        .add("channelExpiration", channelExpiration).add("channelToken", channelToken)
        .add("changed", changed);
  }

  /** Returns the message number (a monotonically increasing value starting with 1). */
  public final long getMessageNumber() {
    return messageNumber;
  }

  /**
   * Sets the message number (a monotonically increasing value starting with 1).
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setMessageNumber(long messageNumber) {
    Preconditions.checkArgument(messageNumber >= 1);
    this.messageNumber = messageNumber;
    return this;
  }

  /** Returns the {@link ResourceStates resource state}. */
  public final String getResourceState() {
    return resourceState;
  }

  /**
   * Sets the {@link ResourceStates resource state}.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setResourceState(String resourceState) {
    this.resourceState = Preconditions.checkNotNull(resourceState);
    return this;
  }

  /** Returns the opaque ID for the watched resource that is stable across API versions. */
  public final String getResourceId() {
    return resourceId;
  }

  /**
   * Sets the opaque ID for the watched resource that is stable across API versions.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setResourceId(String resourceId) {
    this.resourceId = Preconditions.checkNotNull(resourceId);
    return this;
  }

  /**
   * Returns the opaque ID (in the form of a canonicalized URI) for the watched resource that is
   * sensitive to the API version.
   */
  public final String getResourceUri() {
    return resourceUri;
  }

  /**
   * Sets the opaque ID (in the form of a canonicalized URI) for the watched resource that is
   * sensitive to the API version.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setResourceUri(String resourceUri) {
    this.resourceUri = Preconditions.checkNotNull(resourceUri);
    return this;
  }

  /** Returns the notification channel UUID provided by the client in the watch request. */
  public final String getChannelId() {
    return channelId;
  }

  /**
   * Sets the notification channel UUID provided by the client in the watch request.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setChannelId(String channelId) {
    this.channelId = Preconditions.checkNotNull(channelId);
    return this;
  }

  /** Returns the notification channel expiration time or {@code null} for none. */
  public final String getChannelExpiration() {
    return channelExpiration;
  }

  /**
   * Sets the notification channel expiration time or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setChannelExpiration(String channelExpiration) {
    this.channelExpiration = channelExpiration;
    return this;
  }

  /**
   * Returns the notification channel token (an opaque string) provided by the client in the watch
   * request or {@code null} for none.
   */
  public final String getChannelToken() {
    return channelToken;
  }

  /**
   * Sets the notification channel token (an opaque string) provided by the client in the watch
   * request or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setChannelToken(String channelToken) {
    this.channelToken = channelToken;
    return this;
  }

  /**
   * Returns the type of change performed on the resource or {@code null} for none.
   */
  public final String getChanged() {
    return changed;
  }

  /**
   * Sets the type of change performed on the resource or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AbstractNotification setChanged(String changed) {
    this.changed = changed;
    return this;
  }
}
