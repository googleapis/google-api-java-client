/*
 * Copyright (c) 2014 Google Inc.
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Utilities used by the com.google.api.client.googleapis.auth.oauth2 namespace.
 *
 */
class OAuth2Utils {

  static final Charset UTF_8 = Charset.forName("UTF-8");

  private static final String METADATA_SERVER_URL = "http://metadata.google.internal";

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

  static boolean runningOnComputeEngine(HttpTransport transport) {
    try {
      GenericUrl tokenUrl = new GenericUrl(METADATA_SERVER_URL);
      HttpRequest request = transport.createRequestFactory().buildGetRequest(tokenUrl);
      HttpResponse response = request.execute();
      HttpHeaders headers = response.getHeaders();
      if (headersContainValue(headers, "Metadata-Flavor", "Google")) {
        return true;
      }
    } catch (IOException expected) {
    }
    return false;
  }

  private OAuth2Utils() {
  }
}
