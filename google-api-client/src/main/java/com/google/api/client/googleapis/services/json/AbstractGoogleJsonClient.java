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

package com.google.api.client.googleapis.services.json;

import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;

/**
 * Thread-safe Google JSON client.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class AbstractGoogleJsonClient extends AbstractGoogleClient {

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected AbstractGoogleJsonClient(Builder builder) {
    super(builder);
  }

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param rootUrl root URL of the service
   * @param servicePath service path
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @param legacyDataWrapper whether using the legacy data wrapper in responses
   * @deprecated (scheduled to be removed in 1.15) Use {@link #AbstractGoogleJsonClient(Builder)}
   */
  @Deprecated
  protected AbstractGoogleJsonClient(HttpTransport transport, JsonFactory jsonFactory,
      String rootUrl, String servicePath, HttpRequestInitializer httpRequestInitializer,
      boolean legacyDataWrapper) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, legacyDataWrapper
        ? new JsonCParser(jsonFactory) : new JsonObjectParser(jsonFactory));
  }

  /**
   * @param transport HTTP transport
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @param rootUrl root URL of the service
   * @param servicePath service path
   * @param jsonObjectParser JSON object parser
   * @param googleClientRequestInitializer Google request initializer or {@code null} for none
   * @param applicationName application name to be sent in the User-Agent header of requests or
   *        {@code null} for none
   * @param suppressPatternChecks whether discovery pattern checks should be suppressed on required
   *        parameters
   * @deprecated (scheduled to be removed in 1.15) Use {@link #AbstractGoogleJsonClient(Builder)}
   */
  @Deprecated
  protected AbstractGoogleJsonClient(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser jsonObjectParser,
      GoogleClientRequestInitializer googleClientRequestInitializer, String applicationName,
      boolean suppressPatternChecks) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, jsonObjectParser,
        googleClientRequestInitializer, applicationName, suppressPatternChecks);
  }

  @Override
  public JsonObjectParser getObjectParser() {
    return (JsonObjectParser) super.getObjectParser();
  }

  /** Returns the JSON Factory. */
  public final JsonFactory getJsonFactory() {
    return getObjectParser().getJsonFactory();
  }

  /**
   * Builder for {@link AbstractGoogleJsonClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public abstract static class Builder extends AbstractGoogleClient.Builder {

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param rootUrl root URL of the service
     * @param servicePath service path
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @param legacyDataWrapper whether using the legacy data wrapper in responses
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer,
        boolean legacyDataWrapper) {
      super(transport, rootUrl, servicePath, legacyDataWrapper
          ? new JsonCParser(jsonFactory) : new JsonObjectParser(jsonFactory),
          httpRequestInitializer);
    }

    @Override
    public final JsonObjectParser getObjectParser() {
      return (JsonObjectParser) super.getObjectParser();
    }

    /** Returns the JSON Factory. */
    public final JsonFactory getJsonFactory() {
      return getObjectParser().getJsonFactory();
    }

    @Override
    public abstract AbstractGoogleJsonClient build();

    @Override
    public Builder setRootUrl(String rootUrl) {
      return (Builder) super.setRootUrl(rootUrl);
    }

    @Override
    public Builder setServicePath(String servicePath) {
      return (Builder) super.setServicePath(servicePath);
    }

    @Override
    public Builder setGoogleClientRequestInitializer(
        GoogleClientRequestInitializer googleClientRequestInitializer) {
      return (Builder) super.setGoogleClientRequestInitializer(googleClientRequestInitializer);
    }

    @Override
    public Builder setHttpRequestInitializer(HttpRequestInitializer httpRequestInitializer) {
      return (Builder) super.setHttpRequestInitializer(httpRequestInitializer);
    }

    @Override
    public Builder setApplicationName(String applicationName) {
      return (Builder) super.setApplicationName(applicationName);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

  }
}
