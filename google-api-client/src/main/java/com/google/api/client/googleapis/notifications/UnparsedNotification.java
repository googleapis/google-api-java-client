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

import java.io.InputStream;

/**
 * {@link Beta} <br/>
 * Notification metadata and unparsed content stream sent to this client about a watched resource.
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
public class UnparsedNotification extends AbstractNotification {

  /** Notification content media type for the content stream or {@code null} for none or unknown. */
  private String contentType;

  /** Notification content input stream or {@code null} for none. */
  private InputStream contentStream;

  /**
   * @param messageNumber message number (a monotonically increasing value starting with 1)
   * @param resourceState {@link ResourceStates resource state}
   * @param resourceId opaque ID for the watched resource that is stable across API versions
   * @param resourceUri opaque ID (in the form of a canonicalized URI) for the watched resource that
   *        is sensitive to the API version
   * @param channelId notification channel UUID provided by the client in the watch request
   */
  public UnparsedNotification(long messageNumber, String resourceState, String resourceId,
      String resourceUri, String channelId) {
    super(messageNumber, resourceState, resourceId, resourceUri, channelId);
  }

  /**
   * Returns the notification content media type for the content stream or {@code null} for none or
   * unknown.
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Sets the notification content media type for the content stream or {@code null} for none or
   * unknown.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public UnparsedNotification setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  /**
   * Returns the notification content input stream or {@code null} for none.
   */
  public final InputStream getContentStream() {
    return contentStream;
  }

  /**
   * Sets the notification content content input stream or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public UnparsedNotification setContentStream(InputStream contentStream) {
    this.contentStream = contentStream;
    return this;
  }

  @Override
  public UnparsedNotification setMessageNumber(long messageNumber) {
    return (UnparsedNotification) super.setMessageNumber(messageNumber);
  }

  @Override
  public UnparsedNotification setResourceState(String resourceState) {
    return (UnparsedNotification) super.setResourceState(resourceState);
  }

  @Override
  public UnparsedNotification setResourceId(String resourceId) {
    return (UnparsedNotification) super.setResourceId(resourceId);
  }

  @Override
  public UnparsedNotification setResourceUri(String resourceUri) {
    return (UnparsedNotification) super.setResourceUri(resourceUri);
  }

  @Override
  public UnparsedNotification setChannelId(String channelId) {
    return (UnparsedNotification) super.setChannelId(channelId);
  }

  @Override
  public UnparsedNotification setChannelExpiration(String channelExpiration) {
    return (UnparsedNotification) super.setChannelExpiration(channelExpiration);
  }

  @Override
  public UnparsedNotification setChannelToken(String channelToken) {
    return (UnparsedNotification) super.setChannelToken(channelToken);
  }

  @Override
  public UnparsedNotification setChanged(String changed) {
    return (UnparsedNotification) super.setChanged(changed);
  }

  @Override
  public String toString() {
    return super.toStringHelper().add("contentType", contentType).toString();
  }
}
