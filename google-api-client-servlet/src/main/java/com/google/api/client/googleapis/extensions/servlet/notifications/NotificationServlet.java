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
import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Beta} <br/>
 * Thread-safe Webhook Servlet to receive notifications.
 *
 * <p>
 * In order to use this servlet you should create a class inheriting from
 * {@link NotificationServlet} and register the servlet in your web.xml.
 * </p>
 *
 * <p>
 * It is a simple wrapper around {@link WebhookUtils#processWebhookNotification}, so if you you may
 * alternatively call that method instead from your {@link HttpServlet#doPost} with no loss of
 * functionality.
 * </p>
 *
 * <b>Example usage:</b>
 *
 * <pre>
  public class MyNotificationServlet extends NotificationServlet {

    private static final long serialVersionUID = 1L;

    public MyNotificationServlet() throws IOException {
      super(new SomeDataStoreFactory());
    }
  }
 * </pre>
 *
 * <b>Sample web.xml setup:</b>
 *
 * <pre>
  {@literal <}servlet{@literal >}
      {@literal <}servlet-name{@literal >}MyNotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}servlet-class{@literal >}com.mypackage.MyNotificationServlet{@literal <}/servlet-class{@literal >}
  {@literal <}/servlet{@literal >}
  {@literal <}servlet-mapping{@literal >}
      {@literal <}servlet-name{@literal >}MyNotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}url-pattern{@literal >}/notifications{@literal <}/url-pattern{@literal >}
  {@literal <}/servlet-mapping{@literal >}
 * </pre>
 *
 * <p>
 * WARNING: by default it uses {@link MemoryDataStoreFactory#getDefaultInstance()} which means it
 * will NOT persist the notification channels when the servlet process dies, so it is a BAD CHOICE
 * for a production application. But it is a convenient choice when testing locally, in which case
 * you don't need to override it, and can simply reference it directly in your web.xml file. For
 * example:
 * </p>
 *
 * <pre>
  {@literal <}servlet{@literal >}
      {@literal <}servlet-name{@literal >}NotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}servlet-class{@literal >}com.google.api.client.googleapis.extensions.servlet.notificationsNotificationServlet{@literal <}/servlet-class{@literal >}
  {@literal <}/servlet{@literal >}
  {@literal <}servlet-mapping{@literal >}
      {@literal <}servlet-name{@literal >}NotificationServlet{@literal <}/servlet-name{@literal >}
      {@literal <}url-pattern{@literal >}/notifications{@literal <}/url-pattern{@literal >}
  {@literal <}/servlet-mapping{@literal >}
 * </pre>
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public class NotificationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /** Notification channel data store. */
  private final transient DataStore<StoredChannel> channelDataStore;

  /**
   * Constructor to be used for testing and demo purposes that uses
   * {@link MemoryDataStoreFactory#getDefaultInstance()} which means it will NOT persist the
   * notification channels when the servlet process dies, so it is a bad choice for a production
   * application.
   */
  public NotificationServlet() throws IOException {
    this(MemoryDataStoreFactory.getDefaultInstance());
  }

  /**
   * Constructor which uses {@link StoredChannel#getDefaultDataStore(DataStoreFactory)} on the given
   * data store factory, which is the normal use case.
   *
   * @param dataStoreFactory data store factory
   */
  protected NotificationServlet(DataStoreFactory dataStoreFactory) throws IOException {
    this(StoredChannel.getDefaultDataStore(dataStoreFactory));
  }

  /**
   * Constructor that allows a specific notification data store to be specified.
   *
   * @param channelDataStore notification channel data store
   */
  protected NotificationServlet(DataStore<StoredChannel> channelDataStore) {
    this.channelDataStore = channelDataStore;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    WebhookUtils.processWebhookNotification(req, resp, channelDataStore);
  }
}
