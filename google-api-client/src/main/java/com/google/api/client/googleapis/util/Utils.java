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

package com.google.api.client.googleapis.util;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * Utility class for the Google API Client Library.
 *
 * @since 1.19
 */
@Beta
public final class Utils {

  /**
   * Returns a cached default implementation of the JsonFactory interface.
   */
  public static JsonFactory getDefaultJsonFactory() {
    return JsonFactoryInstanceHolder.INSTANCE;
  }

  private static class JsonFactoryInstanceHolder {
    // The jackson2.JacksonFactory was introduced as a product dependency in 1.19 to enable
    // other APIs to not require one of these for input. This was the most commonly used
    // implementation in public samples. This is a compile-time dependency to help detect the
    // dependency as early as possible.
    static final JsonFactory INSTANCE = new JacksonFactory();
  }

  /**
   * Returns a cached default implementation of the HttpTransport interface.
   */
  public static HttpTransport getDefaultTransport() {
    return TransportInstanceHolder.INSTANCE;
  }

  private static class TransportInstanceHolder {
    static final HttpTransport INSTANCE = new NetHttpTransport();
  }

  private Utils() {
  }
}
