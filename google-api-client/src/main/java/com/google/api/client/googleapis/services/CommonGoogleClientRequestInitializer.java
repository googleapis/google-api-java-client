/*
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

package com.google.api.client.googleapis.services;

import java.io.IOException;


/**
 * Google common client request initializer implementation for setting properties like key and
 * userIp.
 *
 * <p>
 * The simplest usage is to use it to set the key parameter:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer KEY_INITIALIZER =
      new CommonGoogleClientRequestInitializer(CommonGoogleOptions.newBuilder()
          .setKey(KEY)
          .build());
 * </pre>
 *
 * <p>
 * There is also a constructor to set both the key and userIp parameters:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer INITIALIZER =
      new CommonGoogleClientRequestInitializer(CommonGoogleOptions.newBuilder()
          .setKey(KEY)
          .setUserIp(USER_IP)
          .build());
 * </pre>
 *
 * <p>
 * If you want to implement custom logic, extend it like this:
 * </p>
 *
 * <pre>
  public static class MyRequestInitializer extends CommonGoogleClientRequestInitializer {

    {@literal @}Override
    public void initialize(AbstractGoogleClientRequest{@literal <}?{@literal >} request)
        throws IOException {
      // custom logic
    }
  }
 * </pre>
 *
 * <p>
 * Finally, to set the key and userIp parameters and insert custom logic, extend it like this:
 * </p>
 *
 * <pre>
  public static class MyRequestInitializer2 extends CommonGoogleClientRequestInitializer {

    public MyRequestInitializer2() {
      super(KEY, USER_IP);
    }

    {@literal @}Override
    public void initialize(AbstractGoogleClientRequest{@literal <}?{@literal >} request)
        throws IOException {
      super.initialize(request); // must be called to set the key and userIp parameters
      // insert some additional logic
    }
  }
 * </pre>
 *
 * <p>
 * Subclasses should be thread-safe.
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public class CommonGoogleClientRequestInitializer implements GoogleClientRequestInitializer {

  /**
   * Contains a reason for making the request, which is intended to be recorded in audit logging.
   * An example reason would be a support-case ticket number.
   */
  private static final String REQUEST_REASON_HEADER_NAME = "X-Goog-Request-Reason";

  /**
   * A caller-specified project for quota and billing purposes. The caller must have
   * serviceusage.services.use permission on the project.
   */
  private static final String USER_PROJECT_HEADER_NAME = "X-Goog-User-Project";

  private final CommonGoogleOptions options;

  /**
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleClientRequestInitializer() {
    this(CommonGoogleOptions.newBuilder().build());
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleClientRequestInitializer(String key) {
    this(key, null);
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @param userIp user IP or {@code null} to leave it unchanged
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleClientRequestInitializer(String key, String userIp) {
    this(CommonGoogleOptions.newBuilder().setKey(key).setUserIp(userIp).build());
  }

  /**
   *
   * @param options
   */
  public CommonGoogleClientRequestInitializer(CommonGoogleOptions options) {
    this.options = options;
  }

  /**
   * Subclasses should call super implementation in order to set the key and userIp.
   *
   * @throws IOException I/O exception
   */
  public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
    if (getKey() != null) {
      request.put("key", getKey());
    }
    if (getUserIp() != null) {
      request.put("userIp", getUserIp());
    }
    if (getUserAgent() != null) {
      request.getRequestHeaders().setUserAgent(getUserAgent());
    }
    if (getRequestReason() != null) {
      request.getRequestHeaders().set(REQUEST_REASON_HEADER_NAME, getRequestReason());
    }
    if (getUserProject() != null) {
      request.getRequestHeaders().set(USER_PROJECT_HEADER_NAME, getUserProject());
    }
  }

  /** Returns the API key or {@code null} to leave it unchanged. */
  public final String getKey() {
    return options.getKey();
  }

  /** Returns the user IP or {@code null} to leave it unchanged. */
  public final String getUserIp() {
    return options.getUserIp();
  }

  /** Returns the user agent or {@code null} to leave it unchanged. */
  public final String getUserAgent() {
    return options.getUserAgent();
  }

  /** Returns the request reason or {@code null} to leave it unchanged. */
  public final String getRequestReason() {
    return options.getRequestReason();
  }

  /** Returns the user project or {@code null} to leave it unchanged. */
  public final String getUserProject() {
    return options.getUserProject();
  }

}
