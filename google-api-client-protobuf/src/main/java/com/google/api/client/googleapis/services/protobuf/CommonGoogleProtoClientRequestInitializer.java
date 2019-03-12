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

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;
import com.google.api.client.util.Beta;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Google protocol buffer client request initializer implementation for setting properties like key
 * and userIp.
 *
 * <p>
 * The simplest usage is to use it to set the key parameter:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer KEY_INITIALIZER =
      new CommonGoogleProtoClientRequestInitializer(KEY);
 * </pre>
 *
 * <p>
 * There is also a constructor to set both the key and userIp parameters:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer INITIALIZER =
      new CommonGoogleProtoClientRequestInitializer(KEY, USER_IP);
 * </pre>
 *
 * <p>
 * If you want to implement custom logic, extend it like this:
 * </p>
 *
 * <pre>
  public static class MyRequestInitializer extends CommonGoogleProtoClientRequestInitializer {

    {@literal @}Override
    public void initialize(AbstractGoogleProtoClientRequest{@literal <}?{@literal >} request)
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
  public static class MyKeyRequestInitializer extends CommonGoogleProtoClientRequestInitializer {

    public MyKeyRequestInitializer() {
      super(KEY, USER_IP);
    }

    {@literal @}Override
    public void initializeProtoRequest(
        AbstractGoogleProtoClientRequest{@literal <}?{@literal >} request) throws IOException {
      // custom logic
    }
  }
 * </pre>
 *
 * <p>
 * Subclasses should be thread-safe.
 * </p>
 *
 * @since 1.16
 * @author Yaniv Inbar
 */
@Beta
public class CommonGoogleProtoClientRequestInitializer
    extends CommonGoogleClientRequestInitializer {

  public CommonGoogleProtoClientRequestInitializer() {
    super();
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   */
  public CommonGoogleProtoClientRequestInitializer(String key) {
    super(key);
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @param userIp user IP or {@code null} to leave it unchanged
   */
  public CommonGoogleProtoClientRequestInitializer(String key, String userIp) {
    super(key, userIp);
  }

  @Override
  public final void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
    super.initialize(request);
    initializeProtoRequest((AbstractGoogleProtoClientRequest<?>) request);
  }

  /**
   * Initializes a Google protocol buffer client request.
   *
   * <p>
   * Default implementation does nothing. Called from
   * {@link #initialize(AbstractGoogleClientRequest)}.
   * </p>
   *
   * @throws IOException I/O exception
   */
  protected void initializeProtoRequest(AbstractGoogleProtoClientRequest<?> request)
      throws IOException {
  }
}
