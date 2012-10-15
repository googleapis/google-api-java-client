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

import java.io.IOException;


/**
 * Google common client request initializer implementation for setting properties like key and
 * userIp.
 *
 * <p>
 * The simplest usage is to use it to set the key parameter:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer KEY_INITIALIZER =
      new CommonGoogleClientRequestInitializer(KEY);
 * </pre>
 *
 * <p>
 * There is also a constructor to set both the key and userIp parameters:
 * </p>
 *
 * <pre>
  public static final GoogleClientRequestInitializer INITIALIZER =
      new CommonGoogleClientRequestInitializer(KEY, USER_IP);
 * </pre>
 *
 * <p>
 * If you want to implement custom logic, extend it like this:
 * </p>
 *
 * <pre>
  public static class MyRequestInitializer extends CommonGoogleClientRequestInitializer {

    {@literal @}Override
    public void initialize(AbstractGoogleClientRequest{@literal <}?{@literal >} request)
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
  public static class MyRequestInitializer2 extends CommonGoogleClientRequestInitializer {

    public MyRequestInitializer2() {
      super(KEY, USER_IP);
    }

    {@literal @}Override
    public void initialize(AbstractGoogleClientRequest{@literal <}?{@literal >} request)
        throws IOException {
      super.initialize(request); // must be called to set the key and userIp parameters
      // insert some additional logic
    }
  }
 * </pre>
 *
 * <p>
 * Subclasses should be thread-safe.
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public class CommonGoogleClientRequestInitializer implements GoogleClientRequestInitializer {

  /** API key or {@code null} to leave it unchanged. */
  private final String key;

  /** User IP or {@code null} to leave it unchanged. */
  private final String userIp;

  public CommonGoogleClientRequestInitializer() {
    this(null);
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   */
  public CommonGoogleClientRequestInitializer(String key) {
    this(key, null);
  }

  /**
   * @param key API key or {@code null} to leave it unchanged
   * @param userIp user IP or {@code null} to leave it unchanged
   */
  public CommonGoogleClientRequestInitializer(String key, String userIp) {
    this.key = key;
    this.userIp = userIp;
  }

  /**
   * Subclasses should call super implementation in order to set the key and userIp.
   *
   * @throws IOException I/O exception
   */
  public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
    if (key != null) {
      request.put("key", key);
    }
    if (userIp != null) {
      request.put("userIp", userIp);
    }
  }

  /** Returns the API key or {@code null} to leave it unchanged. */
  public final String getKey() {
    return key;
  }

  /** Returns the user IP or {@code null} to leave it unchanged. */
  public final String getUserIp() {
    return userIp;
  }
}
