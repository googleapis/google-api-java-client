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

package com.google.api.client.googleapis.extensions.servlet.subscriptions;

import com.google.api.client.googleapis.subscriptions.NotificationHeaders;
import com.google.api.client.googleapis.subscriptions.SubscriptionHeaders;
import com.google.api.client.googleapis.subscriptions.SubscriptionStore;
import com.google.api.client.googleapis.subscriptions.UnparsedNotification;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WebHook-Servlet used in conjunction with the {@link WebHookDeliveryMethod} to receive
 * {@link UnparsedNotification}.
 *
 * <p>
 * In order to use this servlet you should create a class inheriting from
 * {@link AbstractWebHookServlet} and register the servlet in your web.xml.
 * </p>
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
    public class NotificationServlet extends AbstractWebHookServlet {

      private static final long serialVersionUID = 1L;

      {@literal @}Override
      protected SubscriptionStore createSubscriptionStore() {
        return new CachedAppEngineSubscriptionStore();
      }
    }
 * </pre>
 *
 * <b>web.xml setup:</b>
 *
 * <pre>
      &lt;servlet&gt;
          &lt;servlet-name&gt;NotificationServlet&lt;/servlet-name&gt;
          &lt;servlet-class&gt;com.mypackage.NotificationServlet&lt;/servlet-class&gt;
      &lt;/servlet&gt;
      &lt;servlet-mapping&gt;
          &lt;servlet-name&gt;NotificationServlet&lt;/servlet-name&gt;
          &lt;url-pattern&gt;/notificiations&lt;/url-pattern&gt;
      &lt;/servlet-mapping&gt;
      &lt;security-constraint&gt;
        &lt;!-- Lift any ACL imposed upon the servlet --&gt;
        &lt;web-resource-collection&gt;
          &lt;web-resource-name&gt;any&lt;/web-resource-name&gt;
          &lt;url-pattern&gt;/notifications&lt;/url-pattern&gt;
        &lt;/web-resource-collection&gt;
      &lt;/security-constraint&gt;
 * </pre>
 *
 * @author Matthias Linder (mlinder)
 * @since 1.14
 */
@SuppressWarnings("serial")
public abstract class AbstractWebHookServlet extends HttpServlet {

  /**
   * Name of header for the ID of the subscription for which you no longer wish to receive
   * notifications (used in the response to a WebHook notification).
   */
  public static final String UNSUBSCRIBE_HEADER = "X-Goog-Unsubscribe";

  /** Subscription store or {@code null} before initialized in {@link #getSubscriptionStore()}. */
  private static SubscriptionStore subscriptionStore;

  /**
   * Used to get access to the subscription store in order to handle incoming notifications.
   */
  protected abstract SubscriptionStore createSubscriptionStore();

  /** Returns the (cached) subscription store. */
  public final SubscriptionStore getSubscriptionStore() {
    if (subscriptionStore == null) {
      subscriptionStore = createSubscriptionStore();
    }
    return subscriptionStore;
  }

  /**
   * Responds to a notification with a 200 OK response with the X-Unsubscribe header which causes
   * the subscription to be removed.
   */
  protected void sendUnsubscribeResponse(
      HttpServletResponse resp, UnparsedNotification notification) {
    // Subscriptions can be removed by sending an 200 OK with the X-Unsubscribe header.
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setHeader(UNSUBSCRIBE_HEADER, notification.getSubscriptionId());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Parse the relevant headers and create an unparsed notification.
    String subscriptionId = req.getHeader(SubscriptionHeaders.SUBSCRIPTION_ID);
    String topicId = req.getHeader(SubscriptionHeaders.TOPIC_ID);
    String topicUri = req.getHeader(SubscriptionHeaders.TOPIC_URI);
    String eventType = req.getHeader(NotificationHeaders.EVENT_TYPE_HEADER);
    String clientToken = req.getHeader(SubscriptionHeaders.CLIENT_TOKEN);
    String messageNumber = req.getHeader(NotificationHeaders.MESSAGE_NUMBER_HEADER);
    String changeType = req.getHeader(NotificationHeaders.CHANGED_HEADER);
    if (subscriptionId == null || topicId == null || topicUri == null || eventType == null
        || messageNumber == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "Notification did not contain all required information.");
      return;
    }
    // Hand over the unparsed notification to the subscription manager.
    InputStream contentStream = req.getInputStream();
    try {
      UnparsedNotification notification = new UnparsedNotification(subscriptionId,
          topicId,
          topicUri,
          clientToken,
          Long.valueOf(messageNumber),
          eventType,
          changeType,
          req.getContentType(),
          contentStream);
      if (!notification.deliverNotification(getSubscriptionStore())) {
        sendUnsubscribeResponse(resp, notification);
      }
    } finally {
      contentStream.close();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }
}
