/*
 * Copyright 2012 Google Inc.
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

package com.google.api.client.googleapis.services.json;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import java.io.IOException;

/**
 * Google JSON client request initializer implementation for setting properties like key and userIp.
 *
 * <p>The simplest usage is to use it to set the key parameter:
 *
 * <pre>{@code
 * public static final GoogleClientRequestInitializer KEY_INITIALIZER =
 *        CommonGoogleJsonClientRequestInitializer.newBuilder()
 *              .setKey(KEY)
 *              .build();
 * }</pre>
 *
 * <p>There is also a constructor to set both the key and userIp parameters:
 *
 * <pre>{@code
 * public static final GoogleClientRequestInitializer INITIALIZER =
 *       CommonGoogleJsonClientRequestInitializer.newBuilder()
 *              .setKey(KEY)
 *              .setUserIp(USER_IP)
 *              .build();
 * }</pre>
 *
 * <p>If you want to implement custom logic, extend it like this:
 *
 * <pre>{@code
 * public static class MyRequestInitializer extends CommonGoogleJsonClientRequestInitializer {
 *
 *   {@literal @}Override
 *   public void initialize(AbstractGoogleJsonClientRequest{@literal <}?{@literal >} request)
 *     throws IOException {
 *     // custom logic
 *   }
 * }
 * }</pre>
 *
 * <p>Finally, to set the key and userIp parameters and insert custom logic, extend it like this:
 *
 * <pre>{@code
 * public static class MyKeyRequestInitializer extends CommonGoogleJsonClientRequestInitializer {
 *
 *   public MyKeyRequestInitializer() {
 *     super(KEY, USER_IP);
 *   }
 *
 *   {@literal @}Override
 *   public void initializeJsonRequest
 *       (AbstractGoogleJsonClientRequest{@literal <}?{@literal >} request) throws IOException {
 *     // custom logic
 *   }
 * }
 * }</pre>
 *
 * <p>Subclasses should be thread-safe.
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public class CommonGoogleJsonClientRequestInitializer extends CommonGoogleClientRequestInitializer {

  /** @deprecated Please use the builder interface */
  @Deprecated
  public CommonGoogleJsonClientRequestInitializer() {
    super();
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleJsonClientRequestInitializer(String key) {
    super(key);
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @param userIp user IP or {@code null} to leave it unchanged
   * @deprecated Please use the builder interface
   */
  @Deprecated
  public CommonGoogleJsonClientRequestInitializer(String key, String userIp) {
    super(key, userIp);
  }

  @Override
  public final void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
    super.initialize(request);
    initializeJsonRequest((AbstractGoogleJsonClientRequest<?>) request);
  }

  /**
   * Initializes a Google JSON client request.
   *
   * <p>Default implementation does nothing. Called from {@link
   * #initialize(AbstractGoogleClientRequest)}.
   *
   * @throws IOException I/O exception
   */
  protected void initializeJsonRequest(AbstractGoogleJsonClientRequest<?> request)
      throws IOException {}

  /**
   * Builder for {@code CommonGoogleJsonClientRequestInitializer}.
   *
   * @since 1.30
   */
  public static class Builder extends CommonGoogleClientRequestInitializer.Builder {
    @Override
    protected Builder self() {
      return this;
    }
  }
}
