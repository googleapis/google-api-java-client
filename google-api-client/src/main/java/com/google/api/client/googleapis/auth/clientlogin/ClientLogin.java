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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.Strings;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Client Login authentication method as described in <a
 * href="https://developers.google.com/identity/protocols/AuthForInstalledApps" >ClientLogin for
 * Installed Applications</a>.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class ClientLogin {

  /**
   * HTTP transport required for executing request in {@link #authenticate()}.
   *
   * @since 1.3
   */
  public HttpTransport transport;

  /**
   * URL for the Client Login authorization server.
   *
   * <p>
   * By default this is {@code "https://www.google.com"}, but it may be overridden for testing
   * purposes.
   * </p>
   *
   * @since 1.3
   */
  public GenericUrl serverUrl = new GenericUrl("https://www.google.com");

  /**
   * Short string identifying your application for logging purposes of the form:
   * "companyName-applicationName-versionID".
   */
  @Key("source")
  public String applicationName;

  /**
   * Name of the Google service you're requesting authorization for, for example {@code "cl"} for
   * Google Calendar.
   */
  @Key("service")
  public String authTokenType;

  /** User's full email address. */
  @Key("Email")
  public String username;

  /** User's password. */
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

  /** (optional) Token representing the specific CAPTCHA challenge. */
  @Key("logintoken")
  public String captchaToken;

  /** (optional) String entered by the user as an answer to a CAPTCHA challenge. */
  @Key("logincaptcha")
  public String captchaAnswer;

  /**
   * Key/value data to parse a success response.
   *
   * <p>
   * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
   * </p>
   *
   * <pre>
    public static HttpRequestFactory createRequestFactory(
        HttpTransport transport, Response response) {
      return transport.createRequestFactory(response);
    }
   * </pre>
   *
   * <p>
   * If you have a custom request initializer, take a look at the sample usage for
   * {@link HttpExecuteInterceptor}, which this class also implements.
   * </p>
   */
  public static final class Response implements HttpExecuteInterceptor, HttpRequestInitializer {

    /** Authentication token. */
    @Key("Auth")
    public String auth;

    /** Returns the authorization header value to use based on the authentication token. */
    public String getAuthorizationHeaderValue() {
      return ClientLogin.getAuthorizationHeaderValue(auth);
    }

    public void initialize(HttpRequest request) {
      request.setInterceptor(this);
    }

    public void intercept(HttpRequest request) {
      request.getHeaders().setAuthorization(getAuthorizationHeaderValue());
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
   * @throws ClientLoginResponseException if the authentication response has an error code, such as
   *         for a CAPTCHA challenge.
   */
  public Response authenticate() throws IOException {
    GenericUrl url = serverUrl.clone();
    url.appendRawPath("/accounts/ClientLogin");
    HttpRequest request =
        transport.createRequestFactory().buildPostRequest(url, new UrlEncodedContent(this));
    request.setParser(AuthKeyValueParser.INSTANCE);
    request.setContentLoggingLimit(0);
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    // check for an HTTP success response (2xx)
    if (response.isSuccessStatusCode()) {
      return response.parseAs(Response.class);
    }
    HttpResponseException.Builder builder = new HttpResponseException.Builder(
        response.getStatusCode(), response.getStatusMessage(), response.getHeaders());
    // On error, throw a ClientLoginResponseException with the parsed error details
    ErrorInfo details = response.parseAs(ErrorInfo.class);
    String detailString = details.toString();
    StringBuilder message = HttpResponseException.computeMessageBuffer(response);
    if (!Strings.isNullOrEmpty(detailString)) {
      message.append(StringUtils.LINE_SEPARATOR).append(detailString);
      builder.setContent(detailString);
    }
    builder.setMessage(message.toString());
    throw new ClientLoginResponseException(builder, details);
  }

  /**
   * Returns Google Login {@code "Authorization"} header value based on the given authentication
   * token.
   *
   * @since 1.13
   */
  public static String getAuthorizationHeaderValue(String authToken) {
    return "GoogleLogin auth=" + authToken;
  }
}
