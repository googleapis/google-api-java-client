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

package com.google.api.client.googleapis.services;

import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethod;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.json.JsonHttpClient;
import com.google.api.client.http.json.JsonHttpRequest;
import com.google.api.client.http.json.JsonHttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;

import java.io.IOException;
import java.util.Arrays;

/**
 * Google API client.
 *
 * @since 1.6
 * @author Ravi Mistry
 * @deprecated (scheduled to be removed in 1.14) Use {@code
 *             com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient} instead.
 */
@Deprecated
public class GoogleClient extends JsonHttpClient {

  /** Whether discovery pattern checks should be suppressed on required parameters. */
  private boolean suppressPatternChecks;

  /**
   * Constructor with required parameters.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param baseUrl The base URL of the service. Must end with a "/"
   */
  public GoogleClient(HttpTransport transport, JsonFactory jsonFactory, String baseUrl) {
    super(transport, jsonFactory, baseUrl);
  }

  /**
   * Constructor with required parameters.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   *
   * @param transport The transport to use for requests
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param rootUrl The root URL of the service. Must end with a "/"
   * @param servicePath The service path of the service. Must end with a "/" and not begin with a
   *        "/". It is allowed to be an empty string {@code ""}
   * @param httpRequestInitializer The HTTP request initializer or {@code null} for none
   * @since 1.10
   */
  public GoogleClient(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
      String servicePath, HttpRequestInitializer httpRequestInitializer) {
    super(transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer);
  }

  /**
   * Construct the {@link GoogleClient}.
   *
   * @param transport The transport to use for requests
   * @param jsonHttpRequestInitializer The initializer to use when creating an
   *        {@link JsonHttpRequest} or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param jsonObjectParser JSON parser to use or {@code null} if unused
   * @param baseUrl The base URL of the service. Must end with a "/"
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   */
  protected GoogleClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer, JsonFactory jsonFactory,
      JsonObjectParser jsonObjectParser, String baseUrl, String applicationName) {
    super(transport, jsonHttpRequestInitializer, httpRequestInitializer, jsonFactory,
        jsonObjectParser, baseUrl, applicationName);
  }

  /**
   * Construct the {@link GoogleClient}.
   *
   * @param transport The transport to use for requests
   * @param jsonHttpRequestInitializer The initializer to use when creating an
   *        {@link JsonHttpRequest} or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param jsonObjectParser JSON parser to use or {@code null} if unused
   * @param rootUrl The root URL of the service. Must end with a "/"
   * @param servicePath The service path of the service. Must end with a "/" and not begin with a
   *        "/". It is allowed to be an empty string {@code ""}
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   * @since 1.10
   */
  protected GoogleClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer, JsonFactory jsonFactory,
      JsonObjectParser jsonObjectParser, String rootUrl, String servicePath,
      String applicationName) {
    super(transport, jsonHttpRequestInitializer, httpRequestInitializer, jsonFactory,
        jsonObjectParser, rootUrl, servicePath, applicationName);
  }

  /**
   * Construct the {@link GoogleClient}.
   *
   * @param transport The transport to use for requests
   * @param jsonHttpRequestInitializer The initializer to use when creating an
   *        {@link JsonHttpRequest} or {@code null} for none
   * @param httpRequestInitializer The initializer to use when creating an {@link HttpRequest} or
   *        {@code null} for none
   * @param jsonFactory A factory for creating JSON parsers and serializers
   * @param jsonObjectParser JSON parser to use or {@code null} if unused
   * @param rootUrl The root URL of the service. Must end with a "/"
   * @param servicePath The service path of the service. Must end with a "/" and not begin with a
   *        "/". It is allowed to be an empty string {@code ""}
   * @param applicationName The application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   * @since 1.11
   */
  protected GoogleClient(HttpTransport transport,
      JsonHttpRequestInitializer jsonHttpRequestInitializer,
      HttpRequestInitializer httpRequestInitializer, JsonFactory jsonFactory,
      JsonObjectParser jsonObjectParser, String rootUrl, String servicePath, String applicationName,
      boolean suppressPatternChecks) {
    super(transport, jsonHttpRequestInitializer, httpRequestInitializer, jsonFactory,
        jsonObjectParser, rootUrl, servicePath, applicationName);
    this.suppressPatternChecks = suppressPatternChecks;
  }


  /**
   * Create an {@link HttpRequest} suitable for use against this service.
   *
   * @param method HTTP Method type
   * @param url The complete URL of the service where requests should be sent. It includes the base
   *        path along with the URI template
   * @param body A POJO that can be serialized into JSON or {@code null} for none
   * @return newly created {@link HttpRequest}
   */
  @Override
  protected HttpRequest buildHttpRequest(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest httpRequest = super.buildHttpRequest(method, url, body);
    new MethodOverride().intercept(httpRequest);
    // custom methods may use POST with no content but require a Content-Length header
    if (body == null && method.equals(HttpMethod.POST)) {
      httpRequest.setContent(new EmptyContent());
    }
    return httpRequest;
  }

  /**
   * Create an {@link BatchRequest} object from this Google API client instance.
   *
   * <p>
   * Sample usage:
   * </p>
   *
   * <pre>
     client.batch()
         .queue(...)
         .queue(...)
         .execute();
   * </pre>
   *
   * @return newly created Batch request
   */
  public BatchRequest batch() {
    return batch(null);
  }

  /**
   * Create an {@link BatchRequest} object from this Google API client instance.
   *
   * <p>
   * Sample usage:
   * </p>
   *
   * <pre>
     client.batch(httpRequestInitializer)
         .queue(...)
         .queue(...)
         .execute();
   * </pre>
   *
   * @param httpRequestInitializer The initializer to use when creating the top-level batch HTTP
   *        request or {@code null} for none
   * @return newly created Batch request
   */
  public BatchRequest batch(HttpRequestInitializer httpRequestInitializer) {
    BatchRequest batch =
        new BatchRequest(getRequestFactory().getTransport(), httpRequestInitializer);
    GenericUrl baseUrl;
    if (isBaseUrlUsed()) {
      baseUrl = new GenericUrl(getBaseUrl());
      baseUrl.setPathParts(Arrays.asList("", "batch"));
    } else {
      baseUrl = new GenericUrl(getRootUrl() + "batch");
    }
    batch.setBatchUrl(baseUrl);
    return batch;
  }

  @Override
  protected HttpResponse executeUnparsed(HttpMethod method, GenericUrl url, Object body)
      throws IOException {
    HttpRequest request = buildHttpRequest(method, url, body);
    return executeUnparsed(request);
  }

  @Override
  protected HttpResponse executeUnparsed(HttpRequest request) throws IOException {
    return GoogleJsonResponseException.execute(getJsonFactory(), request);
  }

  /**
   * Returns whether discovery pattern checks should be suppressed on required parameters.
   *
   * @since 1.11
   */
  public final boolean getSuppressPatternChecks() {
    return suppressPatternChecks;
  }

  /**
   * Builder for {@link GoogleClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.6
   * @deprecated (scheduled to be removed in 1.14) Use
   *             {@link com.google.api.client.googleapis.services.AbstractGoogleClient.Builder}
   *             instead.
   */
  @Deprecated
  public static class Builder extends JsonHttpClient.Builder {

    /** Whether discovery pattern checks should be suppressed on required parameters. */
    private boolean suppressPatternChecks;

    /**
     * Returns whether discovery pattern checks should be suppressed on required parameters.
     *
     * @since 1.11
     */
    public final boolean getSuppressPatternChecks() {
      return suppressPatternChecks;
    }

    /**
     * Sets whether discovery pattern checks should be suppressed on required parameters.
     *
     * <p>
     * Default value is {@code false}.
     * </p>
     *
     * @since 1.11
     */
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      this.suppressPatternChecks = suppressPatternChecks;
      return this;
    }

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param jsonFactory A factory for creating JSON parsers and serializers
     * @param baseUrl The base URL of the service. Must end with a "/"
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, GenericUrl baseUrl) {
      super(transport, jsonFactory, baseUrl);
    }

    /**
     * Returns an instance of a new builder.
     *
     * @param transport The transport to use for requests
     * @param jsonFactory A factory for creating JSON parsers and serializers
     * @param rootUrl The root URL of the service. Must end with a "/"
     * @param servicePath The service path of the service. Must end with a "/" and not begin with a
     *        "/". It is allowed to be an empty string {@code ""}
     * @param httpRequestInitializer The HTTP request initializer or {@code null} for none
     * @since 1.10
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer) {
      super(transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer);
    }

    /** Builds a new instance of {@link GoogleClient}. */
    @Override
    public GoogleClient build() {
      if (isBaseUrlUsed()) {
        return new GoogleClient(getTransport(), getJsonHttpRequestInitializer(),
            getHttpRequestInitializer(), getJsonFactory(), getObjectParser(), getBaseUrl().build(),
            getApplicationName());
      }
      return new GoogleClient(getTransport(), getJsonHttpRequestInitializer(),
          getHttpRequestInitializer(), getJsonFactory(), getObjectParser(), getRootUrl(),
          getServicePath(), getApplicationName(), getSuppressPatternChecks());
    }
  }
}
