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

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ObjectParser;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Strings;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Abstract thread-safe Google client.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleClient {

  private static final Logger logger = Logger.getLogger(AbstractGoogleClient.class.getName());

  /** The request factory for connections to the server. */
  private final HttpRequestFactory requestFactory;

  /**
   * Initializer to use when creating an {@link AbstractGoogleClientRequest} or {@code null} for
   * none.
   */
  private final GoogleClientRequestInitializer googleClientRequestInitializer;

  /**
   * Root URL of the service, for example {@code "https://www.googleapis.com/"}. Must be URL-encoded
   * and must end with a "/".
   */
  private final String rootUrl;

  /** Service path, for example {@code "tasks/v1/"}. Must be URL-encoded and must end with a "/". */
  private final String servicePath;

  /** Batch path, for example {@code "batch/tasks"}. Must be URL-encoded. */
  private final String batchPath;

  /**
   * Application name to be sent in the User-Agent header of each request or {@code null} for none.
   */
  private final String applicationName;

  /** Object parser or {@code null} for none. */
  private final ObjectParser objectParser;

  /** Whether discovery pattern checks should be suppressed on required parameters. */
  private final boolean suppressPatternChecks;

  /** Whether discovery required parameter checks should be suppressed. */
  private final boolean suppressRequiredParameterChecks;

  private final String universeDomain;

  private final HttpRequestInitializer httpRequestInitializer;

  /**
   * @param builder builder
   * @since 1.14
   */
  protected AbstractGoogleClient(Builder builder) {
    googleClientRequestInitializer = builder.googleClientRequestInitializer;
    universeDomain = builder.universeDomain;
    rootUrl = normalizeRootUrl(builder.rootUrl);
    servicePath = normalizeServicePath(builder.servicePath);
    batchPath = builder.batchPath;
    if (Strings.isNullOrEmpty(builder.applicationName)) {
      logger.warning("Application name is not set. Call Builder#setApplicationName.");
    }
    applicationName = builder.applicationName;
    requestFactory =
        builder.httpRequestInitializer == null
            ? builder.transport.createRequestFactory()
            : builder.transport.createRequestFactory(builder.httpRequestInitializer);
    objectParser = builder.objectParser;
    suppressPatternChecks = builder.suppressPatternChecks;
    suppressRequiredParameterChecks = builder.suppressRequiredParameterChecks;
    httpRequestInitializer = builder.httpRequestInitializer;
  }

  protected void validateUniverseDomain() {
    HttpRequestInitializer requestInitializer = getHttpRequestInitializer();
    if (!getUniverseDomain().equals("googleapis.com") &&  !(requestInitializer instanceof HttpCredentialsAdapter)) {
      throw new IllegalStateException("You must pass in Credentials to configure the Universe Domain");
    }
    Credentials credentials = ((HttpCredentialsAdapter) requestInitializer).getCredentials();
    try {
      if (!credentials.getUniverseDomain().equals(getUniverseDomain())) {
        throw new IllegalStateException(String.format(
                "The configured universe domain (%s) does not match the universe domain found in the credentials (%s). If you haven't configured the universe domain explicitly, `googleapis.com` is the default.",
                getUniverseDomain(),
                credentials.getUniverseDomain()
        ));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to retrieve the Universe Domain from the Credentials.", e);
    }
  }

  /**
   * Returns the URL-encoded root URL of the service, for example {@code
   * "https://www.googleapis.com/"}.
   *
   * <p>Must end with a "/".
   */
  public final String getRootUrl() {
    return rootUrl;
  }

  /**
   * Returns the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
   *
   * <p>Must end with a "/" and not begin with a "/". It is allowed to be an empty string {@code ""}
   * or a forward slash {@code "/"}, if it is a forward slash then it is treated as an empty string
   */
  public final String getServicePath() {
    return servicePath;
  }

  /**
   * Returns the URL-encoded base URL of the service, for example {@code
   * "https://www.googleapis.com/tasks/v1/"}.
   *
   * <p>Must end with a "/". It is guaranteed to be equal to {@code getRootUrl() +
   * getServicePath()}.
   */
  public final String getBaseUrl() {
    return rootUrl + servicePath;
  }

  /**
   * Returns the application name to be sent in the User-Agent header of each request or {@code
   * null} for none.
   */
  public final String getApplicationName() {
    return applicationName;
  }

  /** Returns the HTTP request factory. */
  public final HttpRequestFactory getRequestFactory() {
    return requestFactory;
  }

  /** Returns the Google client request initializer or {@code null} for none. */
  public final GoogleClientRequestInitializer getGoogleClientRequestInitializer() {
    return googleClientRequestInitializer;
  }

  public final String getUniverseDomain() {
    return universeDomain;
  }

  public final HttpRequestInitializer getHttpRequestInitializer() {
    return httpRequestInitializer;
  }

  /**
   * Returns the object parser or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public ObjectParser getObjectParser() {
    return objectParser;
  }

  /**
   * Initializes a {@link AbstractGoogleClientRequest} using a {@link
   * GoogleClientRequestInitializer}.
   *
   * <p>Must be called before the Google client request is executed, preferably right after the
   * request is instantiated. Sample usage:
   *
   * <pre>{@code
   * public class Get extends HttpClientRequest {
   *   ...
   * }
   *
   * public Get get(String userId) throws IOException {
   *   Get result = new Get(userId);
   *   initialize(result);
   *   return result;
   * }
   * }</pre>
   *
   * <p>Subclasses may override by calling the super implementation.
   *
   * @param httpClientRequest Google client request type
   */
  protected void initialize(AbstractGoogleClientRequest<?> httpClientRequest) throws IOException {
    if (getGoogleClientRequestInitializer() != null) {
      getGoogleClientRequestInitializer().initialize(httpClientRequest);
    }
  }

  /**
   * Create an {@link BatchRequest} object from this Google API client instance.
   *
   * <p>Sample usage:
   *
   * <pre>{@code
   * client.batch()
   *    .queue(...)
   *    .queue(...)
   *    .execute();
   * }</pre>
   *
   * @return newly created Batch request
   */
  public final BatchRequest batch() {
    return batch(null);
  }

  /**
   * Create an {@link BatchRequest} object from this Google API client instance.
   *
   * <p>Sample usage:
   *
   * <pre>{@code
   * client.batch(httpRequestInitializer)
   *    .queue(...)
   *    .queue(...)
   *    .execute();
   * }</pre>
   *
   * @param httpRequestInitializer The initializer to use when creating the top-level batch HTTP
   *     request or {@code null} for none
   * @return newly created Batch request
   */
  public final BatchRequest batch(HttpRequestInitializer httpRequestInitializer) {
    @SuppressWarnings("deprecated")
    BatchRequest batch =
        new BatchRequest(getRequestFactory().getTransport(), httpRequestInitializer);
    if (Strings.isNullOrEmpty(batchPath)) {
      batch.setBatchUrl(new GenericUrl(getRootUrl() + "batch"));
    } else {
      batch.setBatchUrl(new GenericUrl(getRootUrl() + batchPath));
    }
    return batch;
  }

  /** Returns whether discovery pattern checks should be suppressed on required parameters. */
  public final boolean getSuppressPatternChecks() {
    return suppressPatternChecks;
  }

  /**
   * Returns whether discovery required parameter checks should be suppressed.
   *
   * @since 1.14
   */
  public final boolean getSuppressRequiredParameterChecks() {
    return suppressRequiredParameterChecks;
  }

  /** If the specified root URL does not end with a "/" then a "/" is added to the end. */
  static String normalizeRootUrl(String rootUrl) {
    Preconditions.checkNotNull(rootUrl, "root URL cannot be null.");
    if (!rootUrl.endsWith("/")) {
      rootUrl += "/";
    }
    return rootUrl;
  }

  /**
   * If the specified service path does not end with a "/" then a "/" is added to the end. If the
   * specified service path begins with a "/" then the "/" is removed.
   */
  static String normalizeServicePath(String servicePath) {
    Preconditions.checkNotNull(servicePath, "service path cannot be null");
    if (servicePath.length() == 1) {
      Preconditions.checkArgument(
          "/".equals(servicePath), "service path must equal \"/\" if it is of length 1.");
      servicePath = "";
    } else if (servicePath.length() > 0) {
      if (!servicePath.endsWith("/")) {
        servicePath += "/";
      }
      if (servicePath.startsWith("/")) {
        servicePath = servicePath.substring(1);
      }
    }
    return servicePath;
  }

  /**
   * Builder for {@link AbstractGoogleClient}.
   *
   * <p>Implementation is not thread-safe.
   */
  public abstract static class Builder {

    /** HTTP transport. */
    final HttpTransport transport;

    /**
     * Initializer to use when creating an {@link AbstractGoogleClientRequest} or {@code null} for
     * none.
     */
    GoogleClientRequestInitializer googleClientRequestInitializer;

    /** HTTP request initializer or {@code null} for none. */
    HttpRequestInitializer httpRequestInitializer;

    /** Object parser to use for parsing responses. */
    final ObjectParser objectParser;

    /** The root URL of the service, for example {@code "https://www.googleapis.com/"}. */
    String rootUrl;

    /** The service path of the service, for example {@code "tasks/v1/"}. */
    String servicePath;

    /** The batch path of the service, for example {@code "batch/tasks"}. */
    String batchPath;

    /**
     * Application name to be sent in the User-Agent header of each request or {@code null} for
     * none.
     */
    String applicationName;

    /** Whether discovery pattern checks should be suppressed on required parameters. */
    boolean suppressPatternChecks;

    /** Whether discovery required parameter checks should be suppressed. */
    boolean suppressRequiredParameterChecks;

    String universeDomain;

    boolean userSetEndpoint;

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param rootUrl root URL of the service. Must end with a "/"
     * @param servicePath service path
     * @param objectParser object parser or {@code null} for none
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     */
    protected Builder(
        HttpTransport transport,
        String rootUrl,
        String servicePath,
        ObjectParser objectParser,
        HttpRequestInitializer httpRequestInitializer) {
      this.transport = Preconditions.checkNotNull(transport);
      this.objectParser = objectParser;
      setRootUrl(rootUrl);
      setServicePath(servicePath);
      this.httpRequestInitializer = httpRequestInitializer;
      this.universeDomain = "googleapis.com";
      this.userSetEndpoint = false;
    }

    /** Builds a new instance of {@link AbstractGoogleClient}. */
    public abstract AbstractGoogleClient build();

    /** Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return transport;
    }

    /**
     * Returns the object parser or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public ObjectParser getObjectParser() {
      return objectParser;
    }

    /**
     * Returns the URL-encoded root URL of the service, for example {@code
     * https://www.googleapis.com/}.
     *
     * <p>Must be URL-encoded and must end with a "/".
     */
    public final String getRootUrl() {
      return rootUrl;
    }

    /**
     * Sets the URL-encoded root URL of the service, for example {@code https://www.googleapis.com/}
     * .
     *
     * <p>If the specified root URL does not end with a "/" then a "/" is added to the end.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setRootUrl(String rootUrl) {
      this.userSetEndpoint = true;
      this.rootUrl = normalizeRootUrl(rootUrl);
      return this;
    }

    /**
     * Returns the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
     *
     * <p>Must be URL-encoded and must end with a "/" and not begin with a "/". It is allowed to be
     * an empty string {@code ""}.
     */
    public final String getServicePath() {
      return servicePath;
    }

    /**
     * Sets the URL-encoded service path of the service, for example {@code "tasks/v1/"}.
     *
     * <p>It is allowed to be an empty string {@code ""} or a forward slash {@code "/"}, if it is a
     * forward slash then it is treated as an empty string. This is determined when the library is
     * generated and normally should not be changed.
     *
     * <p>If the specified service path does not end with a "/" then a "/" is added to the end. If
     * the specified service path begins with a "/" then the "/" is removed.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setServicePath(String servicePath) {
      this.servicePath = normalizeServicePath(servicePath);
      return this;
    }

    /** Sets the URL-encoded batch path of the service, for example {@code "batch/tasks"}. */
    public Builder setBatchPath(String batchPath) {
      this.batchPath = batchPath;
      return this;
    }

    /** Returns the Google client request initializer or {@code null} for none. */
    public final GoogleClientRequestInitializer getGoogleClientRequestInitializer() {
      return googleClientRequestInitializer;
    }

    /**
     * Sets the Google client request initializer or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setGoogleClientRequestInitializer(
        GoogleClientRequestInitializer googleClientRequestInitializer) {
      this.googleClientRequestInitializer = googleClientRequestInitializer;
      return this;
    }

    /** Returns the HTTP request initializer or {@code null} for none. */
    public final HttpRequestInitializer getHttpRequestInitializer() {
      return httpRequestInitializer;
    }

    /**
     * Sets the HTTP request initializer or {@code null} for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      this.httpRequestInitializer = httpRequestInitializer;
      return this;
    }

    /**
     * Returns the application name to be used in the UserAgent header of each request or {@code
     * null} for none.
     */
    public final String getApplicationName() {
      return applicationName;
    }

    /**
     * Sets the application name to be used in the UserAgent header of each request or {@code null}
     * for none.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setApplicationName(String applicationName) {
      this.applicationName = applicationName;
      return this;
    }

    /** Returns whether discovery pattern checks should be suppressed on required parameters. */
    public final boolean getSuppressPatternChecks() {
      return suppressPatternChecks;
    }

    /**
     * Sets whether discovery pattern checks should be suppressed on required parameters.
     *
     * <p>Default value is {@code false}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      this.suppressPatternChecks = suppressPatternChecks;
      return this;
    }

    /**
     * Returns whether discovery required parameter checks should be suppressed.
     *
     * @since 1.14
     */
    public final boolean getSuppressRequiredParameterChecks() {
      return suppressRequiredParameterChecks;
    }

    /**
     * Sets whether discovery required parameter checks should be suppressed.
     *
     * <p>Default value is {@code false}.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.14
     */
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      this.suppressRequiredParameterChecks = suppressRequiredParameterChecks;
      return this;
    }

    /**
     * Suppresses all discovery pattern and required parameter checks.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.14
     */
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return setSuppressPatternChecks(true).setSuppressRequiredParameterChecks(true);
    }

    public Builder setUniverseDomain(String universeDomain) {
      if (universeDomain.isEmpty()) {
        throw new IllegalArgumentException("The universe domain value cannot be empty.");
      }
      this.universeDomain = universeDomain;
      return this;
    }

    public final String getUniverseDomain() {
      return universeDomain;
    }

    protected void determineEndpoint() {
      if (rootUrl.contains("mtls") && !universeDomain.equals("googleapis.com")) {
        throw new IllegalArgumentException(
                "mTLS is not supported in any universe other than googleapis.com");
      }
      String serviceName = "bigquery";
      if (!userSetEndpoint) {
        rootUrl = "https://" + serviceName + "." + universeDomain + ":443";
      }
    }
  }
}
