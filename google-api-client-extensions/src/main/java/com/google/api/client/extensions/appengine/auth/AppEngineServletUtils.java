/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.extensions.appengine.auth;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.common.base.Preconditions;

/**
 * Utility methods that can be shared across App Engine specializations of the abstract auth
 * servlets.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
class AppEngineServletUtils {
  /**
   * Private constructor to prevent instantiation
   */
  private AppEngineServletUtils() {
  }

  /**
   * Return the user id for the currently logged in user.
   */
  static final String getUserId() {
    UserService userService = UserServiceFactory.getUserService();
    User loggedIn = userService.getCurrentUser();
    Preconditions.checkState(loggedIn != null, "This servlet requires the user to be logged in.");
    return loggedIn.getUserId();
  }
}
