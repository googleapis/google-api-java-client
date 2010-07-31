/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;

/**
 * HTTP transport for Google API's. It's only purpose is to allow for method
 * overriding when the firewall does not accept DELETE, PATCH or PUT methods.
 * <p>
 * Warning: scheduled in version 1.1 to no longer extend HttpTransport
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public class GoogleTransport extends HttpTransport {

  /**
   * Creates and returns a new HTTP transport with basic default behaviors for
   * working with Google API's.
   * <p>
   * Includes:
   * <ul>
   * <li>Setting the {@link HttpTransport#defaultHeaders} to a new instance of
   * {@link GoogleHeaders}.</li>
   * <li>Adding a {@link MethodOverrideIntercepter} as the first HTTP execute
   * intercepter to use HTTP method override for unsupported HTTP methods (calls
   * {@link MethodOverrideIntercepter#setAsFirstFor(HttpTransport)}.</li>
   * </ul>
   * <p>
   * Sample usage:
   * 
   * <pre>
   * <code>static HttpTransport createTransport() {
   *   HttpTransport transport = GoogleHeaders.create();
   *   GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
   *   headers.setApplicationName("acme-rocket-2");
   *   headers.gdataVersion = "2";
   * }
   * </code>
   * </pre>
   * 
   * @return HTTP transport
   */
  public static HttpTransport create() {
    HttpTransport transport = new HttpTransport();
    MethodOverrideIntercepter.setAsFirstFor(transport);
    transport.defaultHeaders = new GoogleHeaders();
    return transport;
  }

  /**
   * @deprecated (scheduled to be removed in version 1.1) Use {@link #create()}
   */
  @Deprecated
  public GoogleTransport() {
  }

  /**
   * If {@code true}, the GData HTTP client library will use POST to send data
   * to the associated GData service and will specify the actual method using
   * the method override HTTP header. This can be used as a workaround for HTTP
   * proxies or gateways that do not handle PUT, PATCH, or DELETE HTTP methods
   * properly. If {@code false}, the regular verbs will be used.
   * 
   * @deprecated (scheduled to be removed in version 1.1) Use {@link #create()}
   */
  @Deprecated
  public static boolean ENABLE_METHOD_OVERRIDE = false;

  /**
   * Required application name of the format {@code
   * "[company-id]-[app-name]-[app-version]"}.
   * 
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link GoogleHeaders#setApplicationName(String)} on
   *             {@link #defaultHeaders}
   */
  @Deprecated
  public String applicationName;

  /**
   * Sets the {@code "GData-Version"} header required by Google Data API's.
   * 
   * @param version version of the Google Data API being access, for example
   *        {@code "2"}.
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link GoogleHeaders#gdataVersion} on {@link #defaultHeaders}
   */
  @Deprecated
  public void setVersionHeader(String version) {
    this.defaultHeaders.set("GData-Version", version);
  }

  /**
   * Sets the Client Login token (implemented as a {@code GoogleLogin} {@code
   * Authorization} header) based on the given authentication token. This is
   * primarily intended for use in the Android environment after retrieving the
   * authentication token from the AccountManager.
   * 
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link GoogleHeaders#setGoogleLogin(String)}
   */
  @Deprecated
  public void setClientLoginToken(String authToken) {
    this.defaultHeaders.authorization = getClientLoginHeaderValue(authToken);
  }

  /**
   * Returns Client Login authentication header value based on the given
   * authentication token.
   * 
   * @deprecated (scheduled to be removed in version 1.1) Use
   *             {@link GoogleHeaders#getGoogleLoginValue(String)}
   */
  @Deprecated
  public static String getClientLoginHeaderValue(String authToken) {
    return "GoogleLogin auth=" + authToken;
  }

  @Override
  public HttpRequest buildDeleteRequest() {
    if (!ENABLE_METHOD_OVERRIDE) {
      return super.buildDeleteRequest();
    }
    return buildMethodOverride("DELETE");
  }

  @Override
  public HttpRequest buildPatchRequest() {
    if (!ENABLE_METHOD_OVERRIDE && useLowLevelHttpTransport().supportsPatch()) {
      return super.buildPatchRequest();
    }
    return buildMethodOverride("PATCH");
  }

  @Override
  public HttpRequest buildPutRequest() {
    if (!ENABLE_METHOD_OVERRIDE) {
      return super.buildPutRequest();
    }
    return buildMethodOverride("PUT");
  }

  private HttpRequest buildMethodOverride(String method) {
    HttpRequest request = buildPostRequest();
    request.headers.set("X-HTTP-Method-Override", method);
    return request;
  }
}
