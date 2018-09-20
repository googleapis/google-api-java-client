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

package com.google.api.client.googleapis.extensions.servlet.notifications;

import com.google.api.client.googleapis.notifications.StoredChannel;
import com.google.api.client.googleapis.notifications.UnparsedNotification;
import com.google.api.client.googleapis.notifications.UnparsedNotificationCallback;
import com.google.api.client.util.Beta;
import com.google.api.client.util.LoggingInputStream;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Beta} <br/>
 * Utilities for Webhook notifications.
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public final class WebhookUtils {

  static final Logger LOGGER = Logger.getLogger(WebhookUtils.class.getName());

  /** Webhook notification channel type to use in the watch request. */
  public static final String TYPE = "web_hook";

  /**
   * Utility method to process the webhook notification from {@link HttpServlet#doPost} by finding
   * the notification channel in the given data store factory.
   *
   * <p>
   * It is a wrapper around
   * {@link #processWebhookNotification(HttpServletRequest, HttpServletResponse, DataStore)} that
   * uses the data store from {@link StoredChannel#getDefaultDataStore(DataStoreFactory)}.
   * </p>
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *        of the servlet
   * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends
   *        to the client
   * @param dataStoreFactory data store factory
   * @exception IOException if an input or output error is detected when the servlet handles the
   *            request
   * @exception ServletException if the request for the POST could not be handled
   */
  public static void processWebhookNotification(
      HttpServletRequest req, HttpServletResponse resp, DataStoreFactory dataStoreFactory)
      throws ServletException, IOException {
    processWebhookNotification(req, resp, StoredChannel.getDefaultDataStore(dataStoreFactory));
  }

  /**
   * Utility method to process the webhook notification from {@link HttpServlet#doPost}.
   *
   * <p>
   * The {@link HttpServletRequest#getInputStream()} is closed in a finally block inside this
   * method. If it is not detected to be a webhook notification, an
   * {@link HttpServletResponse#SC_BAD_REQUEST} error will be displayed. If the notification channel
   * is found in the given notification channel data store, it will call
   * {@link UnparsedNotificationCallback#onNotification} for the registered notification callback
   * method.
   * </p>
   *
   * @param req an {@link HttpServletRequest} object that contains the request the client has made
   *        of the servlet
   * @param resp an {@link HttpServletResponse} object that contains the response the servlet sends
   *        to the client
   * @param channelDataStore notification channel data store
   * @exception IOException if an input or output error is detected when the servlet handles the
   *            request
   * @exception ServletException if the request for the POST could not be handled
   */
  public static void processWebhookNotification(
      HttpServletRequest req, HttpServletResponse resp, DataStore<StoredChannel> channelDataStore)
      throws ServletException, IOException {
    Preconditions.checkArgument("POST".equals(req.getMethod()));
    InputStream contentStream = req.getInputStream();
    try {
      // log headers
      if (LOGGER.isLoggable(Level.CONFIG)) {
        StringBuilder builder = new StringBuilder();
        Enumeration<?> e = req.getHeaderNames();
        if (e != null) {
          while (e.hasMoreElements()) {
            Object nameObj = e.nextElement();
            if (nameObj instanceof String) {
              String name = (String) nameObj;
              Enumeration<?> ev = req.getHeaders(name);
              if (ev != null) {
                while (ev.hasMoreElements()) {
                  builder.append(name)
                      .append(": ").append(ev.nextElement()).append(StringUtils.LINE_SEPARATOR);
                }
              }
            }
          }
        }
        LOGGER.config(builder.toString());
        contentStream = new LoggingInputStream(contentStream, LOGGER, Level.CONFIG, 0x4000);
        // TODO(yanivi): allow to override logging content limit
      }
      // parse the relevant headers, and create a notification
      Long messageNumber;
      try {
        messageNumber = Long.valueOf(req.getHeader(WebhookHeaders.MESSAGE_NUMBER));
      } catch (NumberFormatException e) {
        messageNumber = null;
      }
      String resourceState = req.getHeader(WebhookHeaders.RESOURCE_STATE);
      String resourceId = req.getHeader(WebhookHeaders.RESOURCE_ID);
      String resourceUri = req.getHeader(WebhookHeaders.RESOURCE_URI);
      String channelId = req.getHeader(WebhookHeaders.CHANNEL_ID);
      String channelExpiration = req.getHeader(WebhookHeaders.CHANNEL_EXPIRATION);
      String channelToken = req.getHeader(WebhookHeaders.CHANNEL_TOKEN);
      String changed = req.getHeader(WebhookHeaders.CHANGED);
      if (messageNumber == null || resourceState == null || resourceId == null
          || resourceUri == null || channelId == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "Notification did not contain all required information.");
        return;
      }
      UnparsedNotification notification = new UnparsedNotification(messageNumber, resourceState,
          resourceId, resourceUri, channelId).setChannelExpiration(channelExpiration)
          .setChannelToken(channelToken)
          .setChanged(changed)
          .setContentType(req.getContentType())
          .setContentStream(contentStream);
      // check if we know about the channel, hand over the notification to the notification callback
      StoredChannel storedChannel = channelDataStore.get(notification.getChannelId());
      if (storedChannel != null) {
        storedChannel.getNotificationCallback().onNotification(storedChannel, notification);
      }
    } finally {
      contentStream.close();
    }
  }

  private WebhookUtils() {
  }
}
