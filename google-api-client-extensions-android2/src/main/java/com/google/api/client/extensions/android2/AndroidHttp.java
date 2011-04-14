/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.extensions.android2;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import android.os.Build;

import java.net.HttpURLConnection;

/**
 * Utilities for Android HTTP transport.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class AndroidHttp {

  /** SDK 2.3 version build number. */
  private static final int GINGERBREAD = 9;

  /**
   * Returns a new thread-safe HTTP transport instance that is compatible with Android SDKs prior to
   * Gingerbread.
   * <p>
   * Don't use this for Android applications that anyway require Gingerbread. Instead just call
   * {@code new NetHttpTransport()}.
   * </p>
   * <p>
   * Prior to Gingerbread, the {@link HttpURLConnection} implementation was buggy, and the Apache
   * HTTP Client was preferred. However, starting with Gingerbread, the {@link HttpURLConnection}
   * implementation bugs were fixed, and is now better supported than the Apache HTTP Client. There
   * is no guarantee that Apache HTTP transport will continue to work in future SDKs. Therefore,
   * this method uses {@link NetHttpTransport} for Gingerbread or higher, and otherwise
   * {@link ApacheHttpTransport}.
   * </p>
   */
  public static HttpTransport newCompatibleTransport() {
    return isGingerbreadOrHigher() ? new NetHttpTransport() : new ApacheHttpTransport();
  }

  /** Returns whether the SDK version is Gingerbread (2.3) or higher. */
  public static boolean isGingerbreadOrHigher() {
    return Build.VERSION.SDK_INT >= GINGERBREAD;
  }
}
