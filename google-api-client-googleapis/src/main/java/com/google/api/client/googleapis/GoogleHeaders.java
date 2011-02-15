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

package com.google.api.client.googleapis;

import com.google.api.client.escape.PercentEscaper;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Key;

/**
 * HTTP headers for Google API's.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class GoogleHeaders extends HttpHeaders {

  /** Escaper for the {@link #slug} header. */
  public static final PercentEscaper SLUG_ESCAPER =
      new PercentEscaper(" !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", false);

  /** {@code "GData-Version"} header. */
  @Key("GData-Version")
  public String gdataVersion;

  /**
   * Escaped {@code "Slug"} header value, which must be escaped using {@link #SLUG_ESCAPER}.
   *
   * @see #setSlugFromFileName(String)
   */
  @Key("Slug")
  public String slug;

  /** {@code "X-GData-Client"} header. */
  @Key("X-GData-Client")
  public String gdataClient;

  /**
   * {@code "X-GData-Key"} header, which must be of the form {@code "key=[developerId]"}.
   *
   * @see #setDeveloperId(String)
   */
  @Key("X-GData-Key")
  public String gdataKey;

  /**
   * {@code "x-goog-acl"} header that lets you apply predefined (canned) ACLs to a bucket or object
   * when you upload it or create it.
   */
  @Key("x-goog-acl")
  public String googAcl;

  /**
   * {@code "x-goog-copy-source"} header that specifies the destination bucket and object for a copy
   * operation.
   */
  @Key("x-goog-copy-source")
  public String googCopySource;

  /**
   * {@code "x-goog-copy-source-if-match"} header that specifies the conditions for a copy
   * operation.
   */
  @Key("x-goog-copy-source-if-match")
  public String googCopySourceIfMatch;

  /**
   * {@code "x-goog-copy-source-if-none-match"} header that specifies the conditions for a copy
   * operation.
   */
  @Key("x-goog-copy-source-if-none-match")
  public String googCopySourceIfNoneMatch;

  /**
   * {@code "x-goog-copy-source-if-modified-since"} header that specifies the conditions for a copy
   * operation.
   */
  @Key("x-goog-copy-source-if-modified-since")
  public String googCopySourceIfModifiedSince;

  /**
   * {@code "x-goog-copy-source-if-unmodified-since"} header that specifies the conditions for a
   * copy operation.
   */
  @Key("x-goog-copy-source-if-unmodified-since")
  public String googCopySourceIfUnmodifiedSince;

  /**
   * {@code "x-goog-date"} header that specifies a time stamp for authenticated requests.
   */
  @Key("x-goog-date")
  public String googDate;

  /**
   * {@code "x-goog-metadata-directive"} header that specifies metadata handling during a copy
   * operation.
   */
  @Key("x-goog-metadata-directive")
  public String googMetadataDirective;

  /** {@code "X-HTTP-Method-Override"} header. */
  @Key("X-HTTP-Method-Override")
  public String methodOverride;

  /**
   * Sets the {@code "Slug"} header for the given file name, properly escaping the header value. See
   * <a href="http://tools.ietf.org/html/rfc5023#section-9.7">The Slug Header</a>.
   */
  public void setSlugFromFileName(String fileName) {
    slug = SLUG_ESCAPER.escape(fileName);
  }

  /**
   * Sets the {@code "User-Agent"} header of the form {@code
   * "[company-id]-[app-name]/[app-version]"}, for example {@code "Google-Sample/1.0"}.
   */
  public void setApplicationName(String applicationName) {
    userAgent = applicationName;
  }

  /** Sets the {@link #gdataKey} header using the given developer ID. */
  public void setDeveloperId(String developerId) {
    gdataKey = "key=" + developerId;
  }

  /**
   * Sets the Google Login {@code "Authorization"} header for the given authentication token.
   */
  public void setGoogleLogin(String authToken) {
    authorization = getGoogleLoginValue(authToken);
  }

  /**
   * Returns Google Login {@code "Authorization"} header value based on the given authentication
   * token.
   */
  public static String getGoogleLoginValue(String authToken) {
    return "GoogleLogin auth=" + authToken;
  }
}
