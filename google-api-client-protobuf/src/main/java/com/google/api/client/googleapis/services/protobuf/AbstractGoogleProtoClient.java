/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.services.protobuf;

import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.protobuf.ProtoObjectParser;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Thread-safe Google protocol buffer client.
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
@Beta
public abstract class AbstractGoogleProtoClient extends AbstractGoogleClient {

  /**
   * @param builder builder
   */
  protected AbstractGoogleProtoClient(Builder builder) {
    super(builder);
  }

  @Override
  public ProtoObjectParser getObjectParser() {
    return (ProtoObjectParser) super.getObjectParser();
  }

  /**
   * {@link Beta} <br/>
   * Builder for {@link AbstractGoogleProtoClient}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   * @since 1.16
   */
  @Beta
  public abstract static class Builder extends AbstractGoogleClient.Builder {

    /**
     * @param transport HTTP transport
     * @param rootUrl root URL of the service
     * @param servicePath service path
     * @param httpRequestInitializer HTTP request initializer or {@code null} for none
     */
    protected Builder(HttpTransport transport, String rootUrl, String servicePath,
        HttpRequestInitializer httpRequestInitializer) {
      super(transport, rootUrl, servicePath, new ProtoObjectParser(), httpRequestInitializer);
    }

    @Override
    public final ProtoObjectParser getObjectParser() {
      return (ProtoObjectParser) super.getObjectParser();
    }

    @Override
    public abstract AbstractGoogleProtoClient build();

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
