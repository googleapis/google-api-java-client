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
 * Standard resource states used by notifications.
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public final class ResourceStates {

  /** Notification that the subscription is alive (comes with no payload). */
  public static final String SYNC = "SYNC";

  /** Resource exists, for example on a create or update. */
  public static final String EXISTS = "EXISTS";

  /** Resource does not exist, for example on a delete. */
  public static final String NOT_EXISTS = "NOT_EXISTS";

  private ResourceStates() {
  }
}
