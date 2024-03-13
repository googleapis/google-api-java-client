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
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract thread-safe Google client.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleClient {

  private static final Logger logger = Logger.getLogger(AbstractGoogleClient.class.getName());

  private static final String GOOGLE_CLOUD_UNIVERSE_DOMAIN = "GOOGLE_CLOUD_UNIVERSE_DOMAIN";

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
    universeDomain = determineUniverseDomain(builder);
    rootUrl = normalizeRootUrl(determineEndpoint(builder));
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

  /**
   * Resolve the Universe Domain to be used when resolving the endpoint. The logic for resolving the
   * universe domain is the following order: 1. Use the user configured value is set, 2. Use the
   * Universe Domain Env Var if set, 3. Default to the Google Default Universe
   */
  private String determineUniverseDomain(Builder builder) {
    String resolvedUniverseDomain = builder.universeDomain;
    if (resolvedUniverseDomain == null) {
      resolvedUniverseDomain = System.getenv(GOOGLE_CLOUD_UNIVERSE_DOMAIN);
    }
    return resolvedUniverseDomain == null
        ? Credentials.GOOGLE_DEFAULT_UNIVERSE
        : resolvedUniverseDomain;
  }

  /**
   * Resolve the endpoint based on user configurations. If the user has configured a custom rootUrl,
   * use that value. Otherwise, construct the endpoint based on the serviceName and the
   * universeDomain.
   */
  private String determineEndpoint(Builder builder) {
    boolean mtlsEnabled = builder.rootUrl.contains(".mtls.");
    if (mtlsEnabled && !universeDomain.equals(Credentials.GOOGLE_DEFAULT_UNIVERSE)) {
      throw new IllegalStateException(
          "mTLS is not supported in any universe other than googleapis.com");
    }
    // If the serviceName is null, we cannot construct a valid resolved endpoint. Simply return
    // the rootUrl as this was custom rootUrl passed in.
    if (builder.isUserConfiguredEndpoint || builder.serviceName == null) {
      return builder.rootUrl;
    }
    if (mtlsEnabled) {
      return "https://" + builder.serviceName + ".mtls." + universeDomain + "/";
    }
    return "https://" + builder.serviceName + "." + universeDomain + "/";
  }

  /**
   * Check that the User configured universe domain matches the Credentials' universe domain. This
   * uses the HttpRequestInitializer to get the Credentials and is enforced that the
   * HttpRequestInitializer is of the {@see <a
   * href="https://github.com/googleapis/google-auth-library-java/blob/main/oauth2_http/java/com/google/auth/http/HttpCredentialsAdapter.java">HttpCredentialsAdapter</a>}
   * from the google-auth-library.
   *
   * <p>To use a non-GDU Credentials, you must use the HttpCredentialsAdapter class.
   *
   * @throws IOException if there is an error reading the Universe Domain from the credentials
   * @throws IllegalStateException if the configured Universe Domain does not match the Universe
   *     Domain in the Credentials
   */
  public void validateUniverseDomain() throws IOException {
    if (!(httpRequestInitializer instanceof HttpCredentialsAdapter)) {
      return;
    }
    Credentials credentials = ((HttpCredentialsAdapter) httpRequestInitializer).getCredentials();
    // No need for a null check as HttpCredentialsAdapter cannot be initialized with null
    // Credentials
    String expectedUniverseDomain = credentials.getUniverseDomain();
    if (!expectedUniverseDomain.equals(getUniverseDomain())) {
      throw new IllegalStateException(
          String.format(
              "The configured universe domain (%s) does not match the universe domain found"
                  + " in the credentials (%s). If you haven't configured the universe domain"
                  + " explicitly, `googleapis.com` is the default.",
              getUniverseDomain(), expectedUniverseDomain));
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

  /**
   * Universe Domain is the domain for Google Cloud Services. It follows the format of
   * `{ServiceName}.{UniverseDomain}`. For example, speech.googleapis.com would have a Universe
   * Domain value of `googleapis.com` and cloudasset.test.com would have a Universe Domain of
   * `test.com`. If this value is not set, this will default to `googleapis.com`.
   *
   * @return The configured Universe Domain or the Google Default Universe (googleapis.com)
   */
  public final String getUniverseDomain() {
    return universeDomain;
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
    validateUniverseDomain();
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

    /** User configured Universe Domain. Defaults to `googleapis.com`. */
    String universeDomain;

    /**
     * Regex pattern to check if the URL passed in matches the default endpoint configured from a
     * discovery doc. Follows the format of `https://{serviceName}(.mtls).googleapis.com/`
     */
    Pattern defaultEndpointRegex =
        Pattern.compile("https://([a-zA-Z]*)(\\.mtls)?\\.googleapis.com/?");

    /**
     * Whether the user has configured an endpoint via {@link #setRootUrl(String)}. This is added in
     * because the rootUrl is set in the Builder's constructor. ,
     *
     * <p>Apiary clients don't allow user configurations to this Builder's constructor, so this
     * would be set to false by default for Apiary libraries. User configuration to the rootUrl is
     * done via {@link #setRootUrl(String)}.
     *
     * <p>For other uses cases that touch this Builder's constructor directly, check if the rootUrl
     * passed matches the default endpoint regex. If it doesn't match, it is a user configured
     * endpoint.
     */
    boolean isUserConfiguredEndpoint;

    /** The parsed serviceName value from the rootUrl from the Discovery Doc. */
    String serviceName;

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
      this.rootUrl = normalizeRootUrl(rootUrl);
      this.servicePath = normalizeServicePath(servicePath);
      this.httpRequestInitializer = httpRequestInitializer;
      Matcher matcher = defaultEndpointRegex.matcher(rootUrl);
      boolean matches = matcher.matches();
      // Checked here for the use case where users extend this class and may pass in
      // a custom endpoint
      this.isUserConfiguredEndpoint = !matches;
      this.serviceName = matches ? matcher.group(1) : null;
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
      this.isUserConfiguredEndpoint = true;
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

    /**
     * Sets the user configured Universe Domain value. This value will be used to try and construct
     * the endpoint to connect to GCP services.
     *
     * @throws IllegalArgumentException if universeDomain is passed in with an empty string ("")
     */
    public Builder setUniverseDomain(String universeDomain) {
      if (universeDomain != null && universeDomain.isEmpty()) {
        throw new IllegalArgumentException("The universe domain value cannot be empty.");
      }
      this.universeDomain = universeDomain;
      return this;
    }

    @VisibleForTesting
    String getServiceName() {
      return serviceName;
    }
  }
}
