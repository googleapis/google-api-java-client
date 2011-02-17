/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.auth.oauth2;

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

/**
 * OAuth 2.0 access token error response as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.3">Error Response</a>.
 *
 * @since 1.2
 * @author Yaniv Inbar
 */
public class AccessTokenErrorResponse extends GenericData {

  /**
   * Error codes listed in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4.3.1">Error Codes</a>.
   */
  public enum KnownError {

    /**
     * The request is missing a required parameter, includes an unsupported parameter or parameter
     * value, repeats a parameter, includes multiple credentials, utilizes more than one mechanism
     * for authenticating the client, or is otherwise malformed.
     */
    INVALID_REQUEST,

    /**
     * The client identifier provided is invalid, the client failed to authenticate, the client did
     * not include its credentials, provided multiple client credentials, or used unsupported
     * credentials type.
     */
    INVALID_CLIENT,

    /**
     * The authenticated client is not authorized to use the access grant type provided.
     */
    UNAUTHORIZED_CLIENT,

    /**
     *The provided access grant is invalid, expired, or revoked (e.g. invalid assertion, expired
     * authorization token, bad end-user password credentials, or mismatching authorization code and
     * redirection URI).
     */
    INVALID_GRANT,

    /**
     * The access grant included - its type or another attribute - is not supported by the
     * authorization server.
     */
    UNSUPPORTED_GRANT_TYPE,

    /**
     * The requested scope is invalid, unknown, malformed, or exceeds the previously granted scope.
     */
    INVALID_SCOPE;
  }

  /**
   * (REQUIRED) A single error code.
   *
   * @see #getErrorCodeIfKnown()
   */
  @Key
  public String error;

  /**
   * (OPTIONAL) A human-readable text providing additional information, used to assist in the
   * understanding and resolution of the error occurred.
   */
  @Key("error_description")
  public String errorDescription;

  /**
   * (OPTIONAL) A URI identifying a human-readable web page with information about the error, used
   * to provide the end-user with additional information about the error.
   */
  @Key("error_uri")
  public String errorUri;

  /**
   * Returns a known error code if {@link #error} is one of the error codes listed in the OAuth 2
   * specification or {@code null} if the {@link #error} is {@code null} or not known.
   */
  public final KnownError getErrorCodeIfKnown() {
    if (error != null) {
      try {
        return KnownError.valueOf(error.toUpperCase());
      } catch (IllegalArgumentException e) {
        // ignore; most likely due to an unrecognized error code
      }
    }
    return null;
  }
}
