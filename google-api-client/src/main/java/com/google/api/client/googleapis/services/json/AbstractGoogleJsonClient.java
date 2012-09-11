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

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.googleapis.subscriptions.SubscriptionManager;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
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
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param rootUrl root URL of the service
   * @param servicePath service path
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   */
  protected AbstractGoogleJsonClient(HttpTransport transport, JsonFactory jsonFactory,
      String rootUrl, String servicePath, HttpRequestInitializer httpRequestInitializer) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, new JsonObjectParser(
        jsonFactory));
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
   * @param subscriptionManager subscription manager
   * @param suppressPatternChecks whether discovery pattern checks should be suppressed on required
   *        parameters
   */
  protected AbstractGoogleJsonClient(HttpTransport transport,
      HttpRequestInitializer httpRequestInitializer, String rootUrl, String servicePath,
      JsonObjectParser jsonObjectParser,
      GoogleClientRequestInitializer googleClientRequestInitializer, String applicationName,
      SubscriptionManager subscriptionManager, boolean suppressPatternChecks) {
    super(transport, httpRequestInitializer, rootUrl, servicePath, jsonObjectParser,
        googleClientRequestInitializer, applicationName, subscriptionManager,
        suppressPatternChecks);
  }

  @Override
  public JsonObjectParser getObjectParser() {
    return (JsonObjectParser) super.getObjectParser();
  }

  @Override
  protected HttpResponse executeUnparsed(HttpRequest request) throws Exception {
    return GoogleJsonResponseException.execute(getJsonFactory(), request);
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
     */
    protected Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer) {
      this(transport, new JsonObjectParser(jsonFactory), rootUrl, servicePath,
          httpRequestInitializer);
    }

    /**
     * @param transport HTTP transport
     * @param jsonObjectParser JSON object parser
     * @param rootUrl root URL of the service
     * @param servicePath service path
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     */
    protected Builder(HttpTransport transport, JsonObjectParser jsonObjectParser, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer) {
      super(transport, rootUrl, servicePath, jsonObjectParser, httpRequestInitializer);
    }

    @Override
    public JsonObjectParser getObjectParser() {
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
    public Builder setSubscriptionManager(SubscriptionManager subscriptionManager) {
      return (Builder) super.setSubscriptionManager(subscriptionManager);
    }

    @Override
    public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
      return (Builder) super.setSuppressPatternChecks(suppressPatternChecks);
    }

  }
}
