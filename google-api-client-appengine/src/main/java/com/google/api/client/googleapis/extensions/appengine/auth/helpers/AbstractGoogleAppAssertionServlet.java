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

package com.google.api.client.googleapis.extensions.appengine.auth.helpers;

import com.google.api.client.extensions.appengine.auth.AbstractAppEngineTwoLeggedFlowServlet;
import com.google.api.client.extensions.auth.helpers.TwoLeggedFlow;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;

/**
 * Specialization of the two-legged auth servlet that assigns reasonable defaults for using the
 * {@link GoogleAppAssertionFlow} to talk to Google APIs on App Engine.
 *
 * @author moshenko@google.com (Jake Moshenko)
 *
 * @since 1.5
 */
public abstract class AbstractGoogleAppAssertionServlet
    extends AbstractAppEngineTwoLeggedFlowServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Required customization that returns a string of space separated OAuth scopes which you wish to
   * access.
   */
  protected abstract String getScopes();

  @Override
  protected String getUserId() {
    AppIdentityService service = AppIdentityServiceFactory.getAppIdentityService();
    return service.getServiceAccountName();
  }

  @Override
  protected TwoLeggedFlow newFlow(String userId) {
    return new GoogleAppAssertionFlow(userId, getScopes(), getHttpTransport(), getJsonFactory());
  }
}
