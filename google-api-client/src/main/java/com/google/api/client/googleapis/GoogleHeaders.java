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

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.util.Key;
import com.google.api.client.util.escape.PercentEscaper;

/**
 * HTTP headers for Google API's.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.14) Use {@link HttpHeaders}
 */
@Deprecated
public class GoogleHeaders extends HttpHeaders {

  /** Escaper for the {@link #slug} header. */
  public static final PercentEscaper SLUG_ESCAPER =
      new PercentEscaper(" !\"#$&'()*+,-./:;<=>?@[\\]^_`{|}~", false);

  /** {@code "GData-Version"} header. */
  @Key("GData-Version")
  private String gdataVersion;

  /**
   * Escaped {@code "Slug"} header value, which must be escaped using {@link #SLUG_ESCAPER}.
   *
   * @see #setSlugFromFileName(String)
   */
  @Key("Slug")
  private String slug;

  /** {@code "X-GData-Client"} header. */
  @Key("X-GData-Client")
  private String gdataClient;

  /**
   * {@code "X-GData-Key"} header, which must be of the form {@code "key=[developerId]"}.
   *
   * @see #setDeveloperId(String)
   */
  @Key("X-GData-Key")
  private String gdataKey;

  /** {@code "X-HTTP-Method-Override"} header. */
  @Key("X-HTTP-Method-Override")
  private String methodOverride;

  /** {@code "X-Upload-Content-Length"} header. */
  @Key("X-Upload-Content-Length")
  private Long uploadContentLength;

  /** {@code "X-Upload-Content-Type"} header. */
  @Key("X-Upload-Content-Type")
  private String uploadContentType;

  /**
   * Creates an empty GoogleHeaders object.
   */
  public GoogleHeaders() {
  }

  /**
   * Creates a GoogleHeaders object using the headers present in the specified {@link HttpHeaders}.
   *
   * @param headers HTTP headers object including set headers
   * @since 1.11
   */
  public GoogleHeaders(HttpHeaders headers) {
    this.fromHttpHeaders(headers);
  }

  /**
   * Sets the {@code "Slug"} header for the given file name, properly escaping the header value. See
   * <a href="http://tools.ietf.org/html/rfc5023#section-9.7">The Slug Header</a>.
   */
  public void setSlugFromFileName(String fileName) {
    slug = SLUG_ESCAPER.escape(fileName);
  }

  /**
   * Sets the {@code "User-Agent"} header of the form
   * {@code "[company-id]-[app-name]/[app-version]"}, for example {@code "Google-Sample/1.0"}.
   */
  public void setApplicationName(String applicationName) {
    setUserAgent(applicationName);
  }

  /** Sets the {@link #gdataKey} header using the given developer ID. */
  public void setDeveloperId(String developerId) {
    gdataKey = "key=" + developerId;
  }

  /**
   * Sets the Google Login {@code "Authorization"} header for the given authentication token.
   */
  public void setGoogleLogin(String authToken) {
    setAuthorization(getGoogleLoginValue(authToken));
  }

  /**
   * Returns the {@code "X-Upload-Content-Length"} header or {@code null} for none.
   *
   * <p>
   * Upgrade warning: this method now returns a {@link Long}. In prior version 1.11 it returned a
   * {@code long}.
   * </p>
   *
   * @since 1.7
   */
  public final Long getUploadContentLength() {
    return uploadContentLength;
  }

  /**
   * Sets the {@code "X-Upload-Content-Length"} header or {@code null} for none.
   *
   * @since 1.12
   */
  public final void setUploadContentLength(Long uploadContentLength) {
    this.uploadContentLength = uploadContentLength;
  }

  /**
   * Sets the {@code "X-Upload-Content-Length"} header.
   *
   * @since 1.7
   */
  @Deprecated
  public final void setUploadContentLength(long uploadContentLength) {
    this.uploadContentLength = uploadContentLength;
  }

  /**
   * Returns the {@code "X-Upload-Content-Type"} header or {@code null} for none.
   *
   * @since 1.7
   */
  public final String getUploadContentType() {
    return uploadContentType;
  }

  /**
   * Sets the {@code "X-Upload-Content-Type"} header or {@code null} for none.
   *
   * @since 1.7
   */
  public final void setUploadContentType(String uploadContentType) {
    this.uploadContentType = uploadContentType;
  }

  /**
   * Returns Google Login {@code "Authorization"} header value based on the given authentication
   * token.
   * @deprecated (scheduled to be removed in 1.14) Use
   *             {@code ClientLogin.getAuthorizationHeaderValue}
   */
  @Deprecated
  public static String getGoogleLoginValue(String authToken) {
    return "GoogleLogin auth=" + authToken;
  }

  /**
   * Returns the {@code "GData-Version"} header.
   *
   * @since 1.8
   */
  public final String getGDataVersion() {
    return gdataVersion;
  }

  /**
   * Sets the {@code "GData-Version"} header.
   *
   * @since 1.8
   */
  public final void setGDataVersion(String gdataVersion) {
    this.gdataVersion = gdataVersion;
  }

  /**
   * Returns the escaped {@code "Slug"} header value, which must be escaped using
   * {@link #SLUG_ESCAPER}.
   *
   * @since 1.8
   */
  public final String getSlug() {
    return slug;
  }

  /**
   * Sets the escaped {@code "Slug"} header value, which must be escaped using
   * {@link #SLUG_ESCAPER}.
   *
   * @since 1.8
   */
  public final void setSlug(String slug) {
    this.slug = slug;
  }

  /**
   * Returns the {@code "X-GData-Client"} header.
   *
   * @since 1.8
   */
  public final String getGDataClient() {
    return gdataClient;
  }

  /**
   * Sets the {@code "X-GData-Client"} header.
   *
   * @since 1.8
   */
  public final void setGDataClient(String gdataClient) {
    this.gdataClient = gdataClient;
  }

  /**
   * Returns the {@code "X-GData-Key"} header, which must be of the form {@code "key=[developerId]"}
   * .
   *
   * @since 1.8
   */
  public final String getGDataKey() {
    return gdataKey;
  }

  /**
   * Sets the {@code "X-GData-Key"} header, which must be of the form {@code "key=[developerId]"}.
   *
   * @since 1.8
   */
  public final void setGDataKey(String gdataKey) {
    this.gdataKey = gdataKey;
  }

  /**
   * Returns the {@code "X-HTTP-Method-Override"} header.
   *
   * @since 1.8
   */
  public final String getMethodOverride() {
    return methodOverride;
  }

  /**
   * Sets the {@code "X-HTTP-Method-Override"} header.
   *
   * @since 1.8
   */
  public final void setMethodOverride(String methodOverride) {
    this.methodOverride = methodOverride;
  }
}
