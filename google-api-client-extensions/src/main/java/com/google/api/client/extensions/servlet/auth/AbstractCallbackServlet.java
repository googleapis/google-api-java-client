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
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Callback that will retrieve and complete a {@link ThreeLeggedFlow} when redirected to by a token
 * server or service provider. Developer should subclass to provide the necessary information
 * tailored to their specific use case.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
public abstract class AbstractCallbackServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(AbstractCallbackServlet.class.getName());

  private static final String ERROR_PARAM = "error";

  private PersistenceManagerFactory pmf;

  private Class<? extends ThreeLeggedFlow> flowType;

  private String redirectUrl;

  private String deniedRedirectUrl;

  private String completionCodeQueryParam;

  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  /**
   * Constructor with will ask the concrete subclass for all required information on the
   * environment.
   */
  public AbstractCallbackServlet() {
    pmf = getPersistenceManagerFactory();
    flowType = getConcreteFlowType();
    redirectUrl = getSuccessRedirectUrl();
    deniedRedirectUrl = getDeniedRedirectUrl();
    completionCodeQueryParam = getCompletionCodeQueryParam();
    httpTransport = newHttpTransportInstance();
    jsonFactory = newJsonFactoryInstance();
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
   * Override with your chosen method to get a PersistenceManagerFactory. For app engine
   * applications this should be a singleton.
   *
   * @return PersistenceManagerFactory instance.
   */
  protected abstract PersistenceManagerFactory getPersistenceManagerFactory();

  /**
   * @return Specific ThreeLeggedFlow type that this callback should retreieve and complete.
   */
  protected abstract Class<? extends ThreeLeggedFlow> getConcreteFlowType();

  /**
   * @return Url to redirect the user to upon a successful credential exchange.
   */
  protected abstract String getSuccessRedirectUrl();

  /**
   * @return Url to redirect the user to upon failure.
   */
  protected abstract String getDeniedRedirectUrl();

  /**
   * @return Specific query parameter keyword to key off of to get completion code. (e.g. "code" for
   *         OAuth2 and "verifier" for OAuth1)
   */
  protected abstract String getCompletionCodeQueryParam();

  /**
   * @return Get a string representation of a userId that can be used to associate credentials and
   *         flows with a specific user.
   */
  protected abstract String getUserId();

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

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Parse the token that will be used to look up the flow object
    String completionCode = req.getParameter(completionCodeQueryParam);
    String errorCode = req.getParameter(ERROR_PARAM);

    if ((completionCode == null || "".equals(completionCode))
        && (errorCode == null || "".equals(errorCode))) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().print("Must have a query parameter: " + completionCodeQueryParam);
      return;
    } else if (errorCode != null && !"".equals(errorCode)) {
      resp.sendRedirect(deniedRedirectUrl);
      return;
    }

    // Get a key for the logged in user to retrieve the flow
    String userId = getUserId();

    // Get flow from the data store
    PersistenceManager manager = pmf.getPersistenceManager();
    try {
      ThreeLeggedFlow flow = null;
      try {
        flow = manager.getObjectById(flowType, userId);
      } catch (JDOObjectNotFoundException e) {
        LOG.severe("Unable to locate flow by user: " + userId);
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().print("Unable to find flow for user: " + userId);
        return;
      }

      flow.setHttpTransport(getHttpTransport());
      flow.setJsonFactory(getJsonFactory());

      // Complete the flow object with the token we got in our query parameters
      Credential c = flow.complete(completionCode);
      manager.makePersistent(c);
      manager.deletePersistent(flow);
      resp.sendRedirect(redirectUrl);
    } finally {
      manager.close();
    }
  }

}
