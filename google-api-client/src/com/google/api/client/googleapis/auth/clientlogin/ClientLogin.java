/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.googleapis.auth.clientlogin;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.auth.AuthKeyValueParser;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Key;

import java.io.IOException;

/**
 * Client Login authentication method as described in <a
 * href="http://code.google.com/apis/accounts/docs/AuthForInstalledApps.html" >ClientLogin for
 * Installed Applications</a>.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class ClientLogin {

  @Key("source")
  public String applicationName;

  @Key("service")
  public String authTokenType;

  @Key("Email")
  public String username;

  @Key("Passwd")
  public String password;

  /**
   * Type of account to request authorization for. Possible values are:
   *
   * <ul>
   * <li>GOOGLE (get authorization for a Google account only)</li>
   * <li>HOSTED (get authorization for a hosted account only)</li>
   * <li>HOSTED_OR_GOOGLE (get authorization first for a hosted account; if attempt fails, get
   * authorization for a Google account)</li>
   * </ul>
   *
   * Use HOSTED_OR_GOOGLE if you're not sure which type of account you want authorization for. If
   * the user information matches both a hosted and a Google account, only the hosted account is
   * authorized.
   *
   * @since 1.1
   */
  @Key
  public String accountType;

  @Key("logintoken")
  public String captchaToken;

  @Key("logincaptcha")
  public String captchaAnswer;

  /** Key/value data to parse a success response. */
  public static final class Response {

    @Key("Auth")
    public String auth;

    public String getAuthorizationHeaderValue() {
      return GoogleHeaders.getGoogleLoginValue(auth);
    }

    /**
     * Sets the authorization header for the given Google transport using the authentication token.
     */
    public void setAuthorizationHeader(HttpTransport googleTransport) {
      googleTransport.defaultHeaders.authorization = GoogleHeaders.getGoogleLoginValue(auth);
    }
  }

  /** Key/value data to parse an error response. */
  public static final class ErrorInfo {

    @Key("Error")
    public String error;

    @Key("Url")
    public String url;

    @Key("CaptchaToken")
    public String captchaToken;

    @Key("CaptchaUrl")
    public String captchaUrl;
  }

  /**
   * Authenticates based on the provided field values.
   *
   * @throws HttpResponseException if the authentication response has an error code, such as for a
   *         CAPTCHA challenge. Call {@code
   *         exception.response.parseAs(ClientLoginAuthenticator.ErrorInfo.class) * } to parse the
   *         response.
   * @throws IOException some other kind of I/O exception
   */
  public Response authenticate() throws HttpResponseException, IOException {
    HttpTransport transport = new HttpTransport();
    transport.addParser(AuthKeyValueParser.INSTANCE);
    HttpRequest request = transport.buildPostRequest();
    request.setUrl("https://www.google.com/accounts/ClientLogin");
    UrlEncodedContent content = new UrlEncodedContent();
    content.data = this;
    request.disableContentLogging = true;
    request.content = content;
    return request.execute().parseAs(Response.class);
  }
}
