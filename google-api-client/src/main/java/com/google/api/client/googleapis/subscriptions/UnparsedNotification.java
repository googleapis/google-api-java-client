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

import com.google.common.base.Preconditions;

import java.io.InputStream;

/**
 * A notification whose content has not been parsed yet.
 *
 * <p>
 * Thread-safe implementation.
 * </p>
 *
 * <b>Example usage:</b>
 * <pre>
    void handleNotification(Subscription subscription, UnparsedNotification notification)
        throws Exception {
      BufferedReader reader = new BufferedReader(new InputStreamReader(notification.getContent()));
      System.out.println(reader.readLine());
      reader.close();
    }
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.11
 */
public final class UnparsedNotification extends Notification {

  /** The input stream containing the content. */
  private final InputStream content;

  /** The content-type of the stream or {@code null} if not specified. */
  private final String contentType;

  /**
   * Creates a {@link Notification} whose content has not yet been read and parsed.
   *
   * @param subscriptionID ID of the linked {@link Subscription}
   * @param topicID Topic ID of the subscribed topic
   * @param topicURI Topic URL of the subscribed topic
   * @param clientToken Client-Token used for origin verification
   * @param eventType Type of Event which caused this notification to be sent
   * @param contentType Content-Type of the unparsed content or {@code null}
   * @param unparsedStream Unparsed content in form of a {@link InputStream}. Caller has the
   *        responsibility of closing the stream.
   */
  public UnparsedNotification(String subscriptionID,
      String topicID,
      String topicURI,
      String clientToken,
      String eventType,
      String contentType,
      InputStream unparsedStream) {
    super(subscriptionID, topicID, topicURI, clientToken, eventType);
    this.contentType = contentType;
    this.content = Preconditions.checkNotNull(unparsedStream);
  }

  /**
   * Returns the Content-Type of the Content of this notification.
   */
  public final String getContentType() {
    return contentType;
  }

  /**
   * Returns the content stream of this notification.
   */
  public final InputStream getContent() {
    return content;
  }
}
