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

package com.google.api.client.googleapis.testing.services;

import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import com.google.api.client.util.ObjectParser;

/**
 * {@link Beta} <br/>
 * Thread-safe mock Google client.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
@Beta
public class MockGoogleClient extends AbstractGoogleClient {

  /**
   * @param transport The transport to use for requests
   * @param rootUrl root URL of the service. Must end with a "/"
   * @param servicePath service path
   * @param objectParser object parser or {@code null} for none
   * @param httpRequestInitializer HTTP request initializer or {@code null} for none
   *
   * @since 1.14
   */
  public MockGoogleClient(HttpTransport transport, String rootUrl, String servicePath,
      ObjectParser objectParser, HttpRequestInitializer httpRequestInitializer) {
    this(new Builder(transport, rootUrl, servicePath, objectParser, httpRequestInitializer));
  }

  /**
   * @param builder builder
   *
   * @since 1.14
   */
  protected MockGoogleClient(Builder builder) {
    super(builder);
  }

  /**
   * Builder for {@link MockGoogleClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  @Beta
  public static class Builder extends AbstractGoogleClient.Builder {

    /**
     * @param transport The transport to use for requests
     * @param rootUrl root URL of the service. Must end with a "/"
     * @param servicePath service path
     * @param objectParser object parser or {@code null} for none
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     */
    public Builder(HttpTransport transport, String rootUrl, String servicePath,
        ObjectParser objectParser, HttpRequestInitializer httpRequestInitializer) {
      super(transport, rootUrl, servicePath, objectParser, httpRequestInitializer);
    }

    @Override
    public MockGoogleClient build() {
      return new MockGoogleClient(this);
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
