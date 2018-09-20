/*
 * Copyright 2014 Google Inc.
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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities used by the com.google.api.client.googleapis.auth.oauth2 namespace.
 */
@Beta
public class OAuth2Utils {

  static final Charset UTF_8 = Charset.forName("UTF-8");

  private static final Logger LOGGER = Logger.getLogger(OAuth2Utils.class.getName());

  // Note: the explicit IP address is used to avoid name server resolution issues.
  private static final String DEFAULT_METADATA_SERVER_URL = "http://169.254.169.254";

  // Note: the explicit `timeout` and `tries` below is a workaround. The underlying
  // issue is that resolving an unknown host on some networks will take
  // 20-30 seconds; making this timeout short fixes the issue, but
  // could lead to false negatives in the event that we are on GCE, but
  // the metadata resolution was particularly slow. The latter case is
  // "unlikely" since the expected 4-nines time is about 0.5 seconds.
  // This allows us to limit the total ping maximum timeout to 1.5 seconds
  // for developer desktop scenarios.
  private static final int MAX_COMPUTE_PING_TRIES = 3;
  private static final int COMPUTE_PING_CONNECTION_TIMEOUT_MS = 500;

  static <T extends Throwable> T exceptionWithCause(T exception, Throwable cause) {
    exception.initCause(cause);
    return exception;
  }

  static boolean headersContainValue(HttpHeaders headers, String headerName, String value) {
    Object values = headers.get(headerName);
    if (values instanceof Collection) {
      @SuppressWarnings("unchecked")
      Collection<Object> valuesList = (Collection<Object>) values;
      for (Object headerValue : valuesList) {
        if (headerValue instanceof String && ((String) headerValue).equals(value)) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean runningOnComputeEngine(HttpTransport transport,
      SystemEnvironmentProvider environment) {
    // If the environment has requested that we do no GCE checks, return immediately.
    if (Boolean.parseBoolean(environment.getEnv("NO_GCE_CHECK"))) {
      return false;
    }

    GenericUrl tokenUrl = new GenericUrl(getMetadataServerUrl(environment));
    for (int i = 1; i <= MAX_COMPUTE_PING_TRIES; ++i) {
      try {
        HttpRequest request = transport.createRequestFactory().buildGetRequest(tokenUrl);
        request.setConnectTimeout(COMPUTE_PING_CONNECTION_TIMEOUT_MS);
        HttpResponse response = request.execute();
        try {
          HttpHeaders headers = response.getHeaders();
          return headersContainValue(headers, "Metadata-Flavor", "Google");
        } finally {
          response.disconnect();
        }
      } catch (SocketTimeoutException expected) {
        // Ignore logging timeouts which is the expected failure mode in non GCE environments.
      } catch (IOException e) {
        LOGGER.log(
            Level.WARNING,
            "Failed to detect whether we are running on Google Compute Engine.",
            e);
      }
    }
    return false;
  }

  public static String getMetadataServerUrl() {
    return getMetadataServerUrl(SystemEnvironmentProvider.INSTANCE);
  }

  static String getMetadataServerUrl(SystemEnvironmentProvider environment) {
    String metadataServerAddress = environment.getEnv("GCE_METADATA_HOST");
    if (metadataServerAddress != null) {
      return "http://" + metadataServerAddress;
    }
    return DEFAULT_METADATA_SERVER_URL;
  }
}
