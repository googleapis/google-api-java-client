/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utility class for the Google API Client Library.
 *
 * @since 1.12
 * @author rmistry@google.com (Ravi Mistry)
 */
public final class GoogleUtils {

  // NOTE: Integer instead of int so compiler thinks it isn't a constant, so it won't inline it
  /**
   * Major part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer MAJOR_VERSION = 1;

  /**
   * Minor part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer MINOR_VERSION = 19;

  /**
   * Bug fix part of the current release version.
   *
   * @since 1.14
   */
  public static final Integer BUGFIX_VERSION = 0;

  /** Current release version. */
  // NOTE: toString() so compiler thinks it isn't a constant, so it won't inline it
  public static final String VERSION = (MAJOR_VERSION + "." + MINOR_VERSION + "." + BUGFIX_VERSION
      + "-rc-SNAPSHOT").toString();

  private GoogleUtils() {
  }
}
