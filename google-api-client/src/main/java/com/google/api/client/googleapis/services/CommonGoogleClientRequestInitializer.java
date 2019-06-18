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
      CommonGoogleClientRequestInitializer.newBuilder()
          .setKey(KEY)
          .build();
 * </pre>
 *
 * <p>
 * There is also a constructor to set both the key and userIp parameters:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer INITIALIZER =
      CommonGoogleClientRequestInitializer.newBuilder()
          .setKey(KEY)
          .setUserIp(USER_IP)
          .build();
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

  /** API key or {@code null} to leave it unchanged. */
  private final String key;

  /** User IP or {@code null} to leave it unchanged. */
  private final String userIp;

  /** User Agent or {@code null} to leave it unchanged. */
  private final String userAgent;

  /** Reason for request or {@code null} to leave it unchanged. */
  private final String requestReason;

  /** Project for quota and billing purposes of {@code null} to leave it unchanged. */
  private final String userProject;

  /**
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleClientRequestInitializer() {
    this(newBuilder());
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
    this(newBuilder().setKey(key).setUserIp(userIp));
  }

  protected CommonGoogleClientRequestInitializer(Builder builder) {
    this.key = builder.getKey();
    this.userIp = builder.getUserIp();
    this.userAgent = builder.getUserAgent();
    this.requestReason = builder.getRequestReason();
    this.userProject = builder.getUserProject();
  }

  /**
   * Returns new builder.
   *
   * @since 1.30
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Subclasses should call super implementation in order to set the key and userIp.
   *
   * @throws IOException I/O exception
   */
  public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
    if (key != null) {
      request.put("key", key);
    }
    if (userIp != null) {
      request.put("userIp", userIp);
    }
    if (userAgent != null) {
      request.getRequestHeaders().setUserAgent(userAgent);
    }
    if (requestReason != null) {
      request.getRequestHeaders().set(REQUEST_REASON_HEADER_NAME, requestReason);
    }
    if (userProject != null) {
      request.getRequestHeaders().set(USER_PROJECT_HEADER_NAME, userProject);
    }
  }

  /** Returns the API key or {@code null} to leave it unchanged. */
  public final String getKey() {
    return key;
  }

  /** Returns the user IP or {@code null} to leave it unchanged. */
  public final String getUserIp() {
    return userIp;
  }

  /** Returns the user agent or {@code null} to leave it unchanged. */
  public final String getUserAgent() {
    return userAgent;
  }

  /** Returns the request reason or {@code null} to leave it unchanged. */
  public final String getRequestReason() {
    return requestReason;
  }

  /** Returns the user project or {@code null}. */
  public final String getUserProject() {
    return userProject;
  }

  /**
   * Builder for {@code CommonGoogleClientRequestInitializer}.
   *
   * @since 1.30
   */
  public static class Builder {
    private String key;
    private String userIp;
    private String userAgent;
    private String requestReason;
    private String userProject;

    /**
     * Set the API Key for outgoing requests.
     *
     * @param key the API key
     * @return the builder
     */
    public Builder setKey(String key) {
      this.key = key;
      return self();
    }

    /**
     * Returns the API key.
     *
     * @return the API key
     */
    public String getKey() {
      return key;
    }

    /**
     * Set the IP address of the end user for whom the API call is being made.
     *
     * @param userIp the user's IP address
     * @return the builder
     */
    public Builder setUserIp(String userIp) {
      this.userIp = userIp;
      return self();
    }

    /**
     * Returns the configured userIp.
     *
     * @return the userIp
     */
    public String getUserIp() {
      return userIp;
    }

    /**
     * Set the user agent.
     *
     * @param userAgent the user agent
     * @return the builder
     */
    public Builder setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return self();
    }

    /**
     * Returns the configured user agent.
     *
     * @return the user agent
     */
    public String getUserAgent() {
      return userAgent;
    }

    /**
     * Set the reason for making the request, which is intended to be recorded in audit logging. An
     * example reason would be a support-case ticket number.
     *
     * @param requestReason the reason for making the request
     * @return the builder
     */
    public Builder setRequestReason(String requestReason) {
      this.requestReason = requestReason;
      return self();
    }

    /**
     * Get the configured request reason.
     *
     * @return the request reason
     */
    public String getRequestReason() {
      return requestReason;
    }

    /**
     * Set the user project for the request. This is a caller-specified project for quota and
     * billing purposes. The caller must have serviceusage.services.use permission on the project.
     *
     * @param userProject the user project
     * @return the builder
     */
    public Builder setUserProject(String userProject) {
      this.userProject = userProject;
      return self();
    }

    /**
     * Get the configured user project.
     *
     * @return the user project
     */
    public String getUserProject() {
      return userProject;
    }

    /**
     * Returns the constructed CommonGoogleClientRequestInitializer instance.
     *
     * @return the constructed CommonGoogleClientRequestInitializer instance
     */
    public CommonGoogleClientRequestInitializer build() {
      return new CommonGoogleClientRequestInitializer(this);
    }

    protected Builder self() {
      return this;
    }

    protected Builder() {}
  }
}
