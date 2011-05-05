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

import com.google.api.client.extensions.appengine.http.urlfetch.UrlFetchTransport;
import com.google.api.client.extensions.servlet.auth.AbstractCallbackServlet;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

/**
 * This servlet fills in some of the required information for the {@link AbstractCallbackServlet}
 * with reasonable defaults for App Engine. It will default the servlet to creating
 * {@link UrlFetchTransport} objects whenever an {@link HttpTransport} is needed. It will also
 * default the user identifier to the logged in App Engine user. This servlet requires that the App
 * Engine user must be logged in to work correctly. This can be accomplished by adding a security
 * constraint in your web.xml for the path at which this servlet will live.
 * <p>
 * Example that requires login for all pages:
 *
 * <pre>
 * <code>
  <security-constraint>
    <web-resource-collection>
      <web-resource-name>any</web-resource-name>
      <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
      <role-name>*</role-name>
    </auth-constraint>
  </security-constraint>
 * </code>
 * </pre>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
public abstract class AbstractAppEngineCallbackServlet extends AbstractCallbackServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Return the user ID of the user that is logged in.
   *
   * @throws IllegalStateException Thrown when no user is logged in.
   */
  @Override
  protected String getUserId() {
    return AppEngineServletUtils.getUserId();
  }

  @Override
  protected HttpTransport newHttpTransportInstance() {
    return new UrlFetchTransport();
  }

  @Override
  protected JsonFactory newJsonFactoryInstance() {
    return new JacksonFactory();
  }
}
