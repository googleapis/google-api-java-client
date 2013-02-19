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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

/**
 * Generic Google URL providing for some common query parameters used in Google API's such as the
 * {@link #alt} and {@link #fields} parameters.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.14) Use {@link GenericUrl}
 */
@Deprecated
public class GoogleUrl extends GenericUrl {

  /** Whether to pretty print the output. */
  @Key("prettyPrint")
  private Boolean prettyprint;

  /** Alternate wire format. */
  @Key
  private String alt;

  /** Partial fields mask. */
  @Key
  private String fields;

  /**
   * API key as described in the <a href="https://code.google.com/apis/console-help/">Google APIs
   * Console documentation</a>.
   */
  @Key
  private String key;

  /**
   * User IP used to enforce per-user limits for server-side applications, as described in the <a
   * href="https://code.google.com/apis/console-help/#EnforceUserLimits">Google APIs Console
   * documentation</a>.
   */
  @Key("userIp")
  private String userip;

  public GoogleUrl() {
  }

  /**
   * @param encodedUrl encoded URL, including any existing query parameters that should be parsed
   */
  public GoogleUrl(String encodedUrl) {
    super(encodedUrl);
  }

  @Override
  public GoogleUrl clone() {
    return (GoogleUrl) super.clone();
  }

  /**
   * Returns whether to pretty print the output.
   *
   * @since 1.8
   */
  public Boolean getPrettyPrint() {
    return prettyprint;
  }

  /**
   * Sets whether to pretty print the output.
   *
   * @since 1.8
   */
  public void setPrettyPrint(Boolean prettyPrint) {
    this.prettyprint = prettyPrint;
  }

  /**
   * Returns the alternate wire format.
   *
   * @since 1.8
   */
  public final String getAlt() {
    return alt;
  }

  /**
   * Sets the alternate wire format.
   *
   * @since 1.8
   */
  public final void setAlt(String alt) {
    this.alt = alt;
  }

  /**
   * Returns the partial fields mask.
   *
   * @since 1.8
   */
  public final String getFields() {
    return fields;
  }

  /**
   * Sets the partial fields mask.
   *
   * @since 1.8
   */
  public final void setFields(String fields) {
    this.fields = fields;
  }

  /**
   * Returns the API key as described in the <a
   * href="https://code.google.com/apis/console-help/">Google APIs Console documentation</a>.
   *
   * @since 1.8
   */
  public final String getKey() {
    return key;
  }

  /**
   * Sets the API key as described in the <a
   * href="https://code.google.com/apis/console-help/">Google APIs Console documentation</a>.
   *
   * @since 1.8
   */
  public final void setKey(String key) {
    this.key = key;
  }

  /**
   * Returns the user IP used to enforce per-user limits for server-side applications, as described
   * in the <a href="https://code.google.com/apis/console-help/#EnforceUserLimits">Google APIs
   * Console documentation</a>.
   *
   * @since 1.8
   */
  public final String getUserIp() {
    return userip;
  }

  /**
   * Sets the user IP used to enforce per-user limits for server-side applications, as described in
   * the <a href="https://code.google.com/apis/console-help/#EnforceUserLimits">Google APIs Console
   * documentation</a>.
   *
   * @since 1.8
   */
  public final void setUserIp(String userip) {
    this.userip = userip;
  }
}
