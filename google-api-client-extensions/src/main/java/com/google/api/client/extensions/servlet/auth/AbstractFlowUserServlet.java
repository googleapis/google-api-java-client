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

package com.google.api.client.extensions.servlet.auth;

import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.extensions.auth.helpers.ThreeLeggedFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that can be used to invoke and manage a {@link ThreeLeggedFlow} object in the App Engine
 * container. Developers should subclass this to provide the necessary information for their
 * specific use case.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
public abstract class AbstractFlowUserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  public AbstractFlowUserServlet() {
    httpTransport = newHttpTransportInstance();
    jsonFactory = newJsonFactoryInstance();
  }

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

    String userId = getUserId();

    ThreeLeggedFlow oauthFlow = newFlow(userId);
    oauthFlow.setJsonFactory(getJsonFactory());
    oauthFlow.setHttpTransport(getHttpTransport());

    try {
      Credential cred = oauthFlow.loadCredential(pm);

      if (cred == null) {
        pm.makePersistent(oauthFlow);

        String authorizationUrl = oauthFlow.getAuthorizationUrl();
        resp.sendRedirect(authorizationUrl);
      } else {
        // Invoke the user code
        doGetWithCredentials(req, resp, cred);
      }
    } finally {
      pm.close();
    }
  }

  /**
   * Return the {@link JsonFactory} instance for this servlet.
   */
  protected final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Return the {@link HttpTransport} instance for this servlet.
   */
  protected final HttpTransport getHttpTransport() {
    return httpTransport;
  }

  /**
   * Obtain a PersistenceManagerFactory for working with the datastore.
   *
   * @return PersistenceManagerFactory instance.
   */
  protected abstract PersistenceManagerFactory getPersistenceManagerFactory();

  /**
   * Create a flow object which will be used to obtain credentials
   *
   * @param userId User id to be passed to the constructor of the flow object
   * @return Flow object used to obtain credentials
   */
  protected abstract ThreeLeggedFlow newFlow(String userId);

  /**
   * Create a new {@link HttpTransport} instance. Implementations can create any type of applicable
   * transport and should be as simple as:
   *
   * <pre>
  new NetHttpTransport();
   * </pre>
   *
   * @return {@link HttpTransport} instance for your particular environment
   */
  protected abstract HttpTransport newHttpTransportInstance();

  /**
   * Create a new {@link JsonFactory} instance. Implementations can create any type of applicable
   * json factory and should be as simple as:
   *
   * <pre>
  new JacksonFactory();
   * </pre>
   *
   * @return {@link JsonFactory} instance for your particular environment
   */
  protected abstract JsonFactory newJsonFactoryInstance();

  /**
   * @return Get a string representation of a userId that can be used to associate credentials and
   *         flows with a specific user.
   */
  protected abstract String getUserId();

  /**
   * Entry point for user code.
   *
   * @param req Request object passed to the servlet when invoked.
   * @param resp Response object passed to the servlet when invoked.
   * @param credential Credential which can be used to build a request factory for authenticated
   *        calls.
   * @throws IOException
   */
  protected abstract void doGetWithCredentials(
      HttpServletRequest req, HttpServletResponse resp, Credential credential) throws IOException;
}
