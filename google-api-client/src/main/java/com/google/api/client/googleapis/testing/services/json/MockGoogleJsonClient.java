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

package com.google.api.client.googleapis.testing.services.json;

import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Thread-safe mock Google JSON client.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
@Beta
public class MockGoogleJsonClient extends AbstractGoogleJsonClient {

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected MockGoogleJsonClient(Builder builder) {
    super(builder);
  }

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param rootUrl root URL of the service
   * @param servicePath service path
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   * @param legacyDataWrapper whether using the legacy data wrapper in responses
   */
  public MockGoogleJsonClient(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
      String servicePath, HttpRequestInitializer httpRequestInitializer,
      boolean legacyDataWrapper) {
    this(new Builder(
        transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer, legacyDataWrapper));
  }

  /**
   * {@link Beta} <br/>
   * Builder for {@link MockGoogleJsonClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  @Beta
  public static class Builder extends AbstractGoogleJsonClient.Builder {

    /**
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param rootUrl root URL of the service
     * @param servicePath service path
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     * @param legacyDataWrapper whether using the legacy data wrapper in responses
     */
    public Builder(HttpTransport transport, JsonFactory jsonFactory, String rootUrl,
        String servicePath, HttpRequestInitializer httpRequestInitializer,
        boolean legacyDataWrapper) {
      super(
          transport, jsonFactory, rootUrl, servicePath, httpRequestInitializer, legacyDataWrapper);
    }

    @Override
    public MockGoogleJsonClient build() {
      return new MockGoogleJsonClient(this);
    }

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

    @Override
    public Builder setSuppressRequiredParameterChecks(boolean suppressRequiredParameterChecks) {
      return (Builder) super.setSuppressRequiredParameterChecks(suppressRequiredParameterChecks);
    }

    @Override
    public Builder setSuppressAllChecks(boolean suppressAllChecks) {
      return (Builder) super.setSuppressAllChecks(suppressAllChecks);
    }
  }
}
