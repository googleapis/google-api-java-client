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

package com.google.api.client.googleapis.extensions.appengine.notifications;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.extensions.servlet.notifications.WebhookUtils;
import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Beta} <br/>
 * Thread-safe Webhook App Engine Servlet to receive notifications.
 *
 * <p>
 * In order to use this servlet you need to register the servlet in your web.xml. You may optionally
 * extend {@link AppEngineNotificationServlet} with custom behavior.
 * </p>
 *
 * <p>
 * It is a simple wrapper around {@link WebhookUtils#processWebhookNotification(HttpServletRequest,
 * HttpServletResponse, DataStoreFactory)} that uses
 * {@link AppEngineDataStoreFactory#getDefaultInstance()}, so you may alternatively call that method
 * instead from your {@link HttpServlet#doPost} with no loss of functionality.
 * </p>
 *
 * <b>Sample web.xml setup:</b>
 *
 * <pre>
  {@literal <}servlet{@literal >}
      {@literal <}servlet-name{@literal >}AppEngineNotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}servlet-class{@literal >}com.google.api.client.googleapis.extensions.appengine.notifications.AppEngineNotificationServlet{@literal <}/servlet-class{@literal >}
  {@literal <}/servlet{@literal >}
  {@literal <}servlet-mapping{@literal >}
      {@literal <}servlet-name{@literal >}AppEngineNotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}url-pattern{@literal >}/notifications{@literal <}/url-pattern{@literal >}
  {@literal <}/servlet-mapping{@literal >}
 * </pre>
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public class AppEngineNotificationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    WebhookUtils.processWebhookNotification(
        req, resp, AppEngineDataStoreFactory.getDefaultInstance());
  }
}
