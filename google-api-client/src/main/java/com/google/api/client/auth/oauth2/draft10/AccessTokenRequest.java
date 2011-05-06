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

package com.google.api.client.auth.oauth2.draft10;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.client.util.Value;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * OAuth 2.0 (draft 10) request for an access token as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4">Obtaining an Access Token</a>.
 * <p>
 * The {@link #AccessTokenRequest()} or
 * {@link #AccessTokenRequest(HttpTransport, JsonFactory, String, String, String)} constructors may
 * be used directly when no access grant is included, such as when the client is requesting access
 * to the protected resources under its control. Otherwise, use one of the subclasses, which add
 * custom parameters to specify the access grant. Call {@link #execute()} to execute the request and
 * use the {@link AccessTokenResponse}. On error, use {@link AccessTokenErrorResponse} instead.
 * <p>
 * Sample usage when the client is requesting access to the protected resources under its control:
 *
 * <pre>
 * <code>
  static void requestAccessToken() throws IOException {
    try {
      AccessTokenRequest request =
          new AccessTokenRequest(new NetHttpTransport(), new JacksonFactory(),
              "https://server.example.com/authorize", "s6BhdRkqt3", "gX1fBat3bV");
      AccessTokenResponse response = request.execute();
      System.out.println("Access token: " + response.accessToken);
    } catch (HttpResponseException e) {
      AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
      System.out.println("Error: " + response.error);
    }
  }
 * </code>
 * </pre>
 * </p>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class AccessTokenRequest extends GenericData {

  /**
   * OAuth 2.0 Web Server Flow: request an access token based on a verification code as specified in
   * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.1">Authorization
   * Code</a>.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        AuthorizationCodeGrant request = new AuthorizationCodeGrant(new NetHttpTransport(),
            new JacksonFactory(),
            "https://server.example.com/authorize",
            "s6BhdRkqt3",
            "gX1fBat3bV",
            "i1WsRn1uB1",
            "https://client.example.com/cb");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class AuthorizationCodeGrant extends AccessTokenRequest {

    /** (REQUIRED) The authorization code received from the authorization server. */
    @Key
    public String code;

    /** (REQUIRED) The redirection URI used in the initial request. */
    @Key("redirect_uri")
    public String redirectUri;

    {
      grantType = GrantType.AUTHORIZATION_CODE;
    }

    public AuthorizationCodeGrant() {
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param authorizationServerUrl encoded authorization server URL
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param code authorization code received from the authorization server
     * @param redirectUri redirection URI used in the initial request
     */
    public AuthorizationCodeGrant(HttpTransport transport,
        JsonFactory jsonFactory,
        String authorizationServerUrl,
        String clientId,
        String clientSecret,
        String code,
        String redirectUri) {
      super(transport, jsonFactory, authorizationServerUrl, clientId, clientSecret);
      this.code = Preconditions.checkNotNull(code);
      this.redirectUri = Preconditions.checkNotNull(redirectUri);
    }
  }

  /**
   * OAuth 2.0 Username and Password Flow: request an access token based on resource owner
   * credentials used in the as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.2">Resource Owner Password
   * Credentials</a>.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        ResourceOwnerPasswordCredentialsGrant request =
            new ResourceOwnerPasswordCredentialsGrant(new NetHttpTransport(),
                new JacksonFactory(),
                "https://server.example.com/authorize",
                "s6BhdRkqt3",
                "gX1fBat3bV",
                "johndoe",
                "A3ddj3w");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class ResourceOwnerPasswordCredentialsGrant extends AccessTokenRequest {

    /** (REQUIRED) The resource owner's username. */
    @Key
    public String username;

    /** (REQUIRED) The resource owner's password. */
    public String password;

    {
      grantType = GrantType.PASSWORD;
    }

    public ResourceOwnerPasswordCredentialsGrant() {
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param authorizationServerUrl encoded authorization server URL
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param username resource owner's username
     * @param password resource owner's password
     */
    public ResourceOwnerPasswordCredentialsGrant(HttpTransport transport,
        JsonFactory jsonFactory,
        String authorizationServerUrl,
        String clientId,
        String clientSecret,
        String username,
        String password) {
      super(transport, jsonFactory, authorizationServerUrl, clientId, clientSecret);
      this.username = Preconditions.checkNotNull(username);
      this.password = Preconditions.checkNotNull(password);
    }
  }

  /**
   * OAuth 2.0 Assertion Flow: request an access token based on as assertion as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.3">Assertion</a>.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        AssertionGrant request = new AssertionGrant(new NetHttpTransport(),
            new JacksonFactory(),
            "https://server.example.com/authorize",
            "gX1fBat3bV",
            "urn:oasis:names:tc:SAML:2.0:",
            "PHNhbWxwOl...[omitted for brevity]...ZT4=");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class AssertionGrant extends AccessTokenRequest {

    /**
     * (REQUIRED) The format of the assertion as defined by the authorization server. The value MUST
     * be an absolute URI.
     */
    @Key("assertion_type")
    public String assertionType;

    /** (REQUIRED) The assertion. */
    @Key
    public String assertion;

    {
      grantType = GrantType.ASSERTION;
    }

    public AssertionGrant() {
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param authorizationServerUrl encoded authorization server URL
     * @param clientSecret client secret
     * @param assertionType format of the assertion as defined by the authorization server. The
     *        value MUST be an absolute URI
     * @param assertion assertion
     */
    public AssertionGrant(HttpTransport transport,
        JsonFactory jsonFactory,
        String authorizationServerUrl,
        String clientSecret,
        String assertionType,
        String assertion) {
      super(transport, jsonFactory, authorizationServerUrl, clientSecret);
      this.assertionType = Preconditions.checkNotNull(assertionType);
      this.assertion = Preconditions.checkNotNull(assertion);
    }
  }

  /**
   * OAuth 2.0 request to refresh an access token as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.1.4">Refresh Token</a>.
   * <p>
   * Sample usage:
   *
   * <pre>
   * <code>
    static void requestAccessToken() throws IOException {
      try {
        RefreshTokenGrant request = new RefreshTokenGrant(new NetHttpTransport(),
            new JacksonFactory(),
            "https://server.example.com/authorize",
            "s6BhdRkqt3",
            "gX1fBat3bV",
            "n4E9O119d");
        AccessTokenResponse response = request.execute();
        System.out.println("Access token: " + response.accessToken);
      } catch (HttpResponseException e) {
        AccessTokenErrorResponse response = e.response.parseAs(AccessTokenErrorResponse.class);
        System.out.println("Error: " + response.error);
      }
    }
   * </code>
   * </pre>
   * </p>
   */
  public static class RefreshTokenGrant extends AccessTokenRequest {

    /**
     * (REQUIRED) The refresh token associated with the access token to be refreshed.
     */
    @Key("refresh_token")
    public String refreshToken;

    {
      grantType = GrantType.REFRESH_TOKEN;
    }

    public RefreshTokenGrant() {
    }

    /**
     * @param transport HTTP transport for executing request in {@link #execute()}
     * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
     * @param authorizationServerUrl encoded authorization server URL
     * @param clientId client identifier
     * @param clientSecret client secret
     * @param refreshToken refresh token associated with the access token to be refreshed
     */
    public RefreshTokenGrant(HttpTransport transport,
        JsonFactory jsonFactory,
        String authorizationServerUrl,
        String clientId,
        String clientSecret,
        String refreshToken) {
      super(transport, jsonFactory, authorizationServerUrl, clientId, clientSecret);
      this.refreshToken = Preconditions.checkNotNull(refreshToken);
    }
  }

  public AccessTokenRequest() {
  }

  /**
   * @param transport HTTP transport for executing request in {@link #execute()}
   * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
   * @param authorizationServerUrl encoded authorization server URL
   * @param clientSecret client secret
   */
  protected AccessTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
      String authorizationServerUrl, String clientSecret) {
    this();
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.authorizationServerUrl = Preconditions.checkNotNull(authorizationServerUrl);
    this.clientSecret = Preconditions.checkNotNull(clientSecret);
  }

  /**
   * @param transport HTTP transport for executing request in {@link #execute()}
   * @param jsonFactory JSON factory to use for parsing response in {@link #execute()}
   * @param authorizationServerUrl encoded authorization server URL
   * @param clientId client identifier
   * @param clientSecret client secret
   */
  public AccessTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
      String authorizationServerUrl, String clientId, String clientSecret) {
    this(transport, jsonFactory, authorizationServerUrl, clientSecret);
    this.clientId = Preconditions.checkNotNull(clientId);
  }

  /**
   * (REQUIRED) HTTP transport required for executing request in {@link #execute()}.
   */
  public HttpTransport transport;

  /**
   * (REQUIRED) JSON factory to use for parsing response in {@link #execute()}.
   */
  public JsonFactory jsonFactory;

  /** Access grant type. */
  public enum GrantType {

    @Value("authorization_code")
    AUTHORIZATION_CODE,
    @Value("password")
    PASSWORD,
    @Value("assertion")
    ASSERTION,
    @Value("refresh_token")
    REFRESH_TOKEN,
    @Value("none")
    NONE
  }

  /** (REQUIRED) The access grant type included in the request. */
  @Key("grant_type")
  public GrantType grantType = GrantType.NONE;

  /**
   * (REQUIRED, unless the client identity can be establish via other means, for example assertion)
   * The client identifier or {@code null} for none.
   */
  @Key("client_id")
  public String clientId;

  /**
   * (REQUIRED) The client secret.
   */
  public String clientSecret;

  /**
   * (OPTIONAL) The scope of the access request expressed as a list of space-delimited strings or
   * {@code null} for none. The value of the "scope" parameter is defined by the authorization
   * server. If the value contains multiple space-delimited strings, their order does not matter,
   * and each string adds an additional access range to the requested scope. If the access grant
   * being used already represents an approved scope (e.g. authorization code, assertion), the
   * requested scope MUST be equal or lesser than the scope previously granted.
   */
  @Key
  public String scope;

  /** (REQUIRED) Encoded authorization server URL. */
  public String authorizationServerUrl;

  /**
   * {@code false} to specify the password in the request body using the {@code "clientSecret"}
   * parameter in the HTTP body or {@code true} to use Basic Authentication as recommended in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-2.1">Client Password
   * Credentials</a>.
   * <p>
   * Defaults to {@code false}.
   * </p>
   */
  public boolean useBasicAuthorization;

  /**
   * Executes request for an access token, and returns the HTTP response.
   *
   * <p>
   * To execute and parse the response to {@link AccessTokenResponse}, use {@link #execute()}
   * </p>
   *
   * @return HTTP response, which can then be parsed directly using
   *         {@link HttpResponse#parseAs(Class)} or some other parsing method
   * @throws HttpResponseException for an HTTP error response, which can then be parsed using
   *         {@link HttpResponse#parseAs(Class)} on {@link HttpResponseException#response} using
   *         {@link AccessTokenErrorResponse}
   */
  public final HttpResponse executeUnparsed() throws IOException {
    JsonHttpParser parser = new JsonHttpParser();
    parser.jsonFactory = jsonFactory;
    UrlEncodedContent content = new UrlEncodedContent();
    content.data = this;
    HttpRequest request = transport.createRequestFactory().buildPostRequest(
        new GenericUrl(authorizationServerUrl), content);
    request.addParser(parser);
    if (useBasicAuthorization) {
      request.headers.setBasicAuthentication(clientId, clientSecret);
    } else {
      put("client_secret", clientSecret);
    }
    return request.execute();
  }

  /**
   * Executes request for an access token, and returns the parsed access token response.
   *
   * <p>
   * To execute without parsing the response, use {@link #executeUnparsed()}
   * </p>
   *
   * @return parsed access token response
   * @throws HttpResponseException for an HTTP error response, which can then be parsed using
   *         {@link HttpResponse#parseAs(Class)} on {@link HttpResponseException#response} using
   *         {@link AccessTokenErrorResponse}
   */
  public final AccessTokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(AccessTokenResponse.class);
  }
}
