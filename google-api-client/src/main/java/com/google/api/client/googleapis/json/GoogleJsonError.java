/*
 * Copyright 2011 Google Inc.
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

package com.google.api.client.googleapis.json;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Data class representing the Google JSON error response content, as documented for example in <a
 * href="https://cloud.google.com/apis/design/errors">Error responses</a>.
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
public class GoogleJsonError extends GenericJson {

  /**
   * Parses the given error HTTP response using the given JSON factory.
   *
   * @param jsonFactory JSON factory
   * @param response HTTP response
   * @return new instance of the Google JSON error information
   * @throws IllegalArgumentException if content type is not {@link Json#MEDIA_TYPE} or if expected
   *     {@code "data"} or {@code "error"} key is not found
   */
  public static GoogleJsonError parse(JsonFactory jsonFactory, HttpResponse response)
      throws IOException {
    JsonObjectParser jsonObjectParser =
        new JsonObjectParser.Builder(jsonFactory)
            .setWrapperKeys(Collections.singleton("error"))
            .build();
    return jsonObjectParser.parseAndClose(
        response.getContent(), response.getContentCharset(), GoogleJsonError.class);
  }

  static {
    // hack to force ProGuard to consider ErrorInfo used, since otherwise it would be stripped out
    // see https://github.com/googleapis/google-api-java-client/issues/527
    Data.nullOf(ErrorInfo.class);
  }

  /** Detailed error information. */
  public static class ErrorInfo extends GenericJson {

    /** Error classification or {@code null} for none. */
    @Key private String domain;

    /** Error reason or {@code null} for none. */
    @Key private String reason;

    /** Human readable explanation of the error or {@code null} for none. */
    @Key private String message;

    /**
     * Location in the request that caused the error or {@code null} for none or {@code null} for
     * none.
     */
    @Key private String location;

    /** Type of location in the request that caused the error or {@code null} for none. */
    @Key private String locationType;

    /**
     * Returns the error classification or {@code null} for none.
     *
     * @since 1.8
     */
    public final String getDomain() {
      return domain;
    }

    /**
     * Sets the error classification or {@code null} for none.
     *
     * @since 1.8
     */
    public final void setDomain(String domain) {
      this.domain = domain;
    }

    /**
     * Returns the error reason or {@code null} for none.
     *
     * @since 1.8
     */
    public final String getReason() {
      return reason;
    }

    /**
     * Sets the error reason or {@code null} for none.
     *
     * @since 1.8
     */
    public final void setReason(String reason) {
      this.reason = reason;
    }

    /**
     * Returns the human readable explanation of the error or {@code null} for none.
     *
     * @since 1.8
     */
    public final String getMessage() {
      return message;
    }

    /**
     * Sets the human readable explanation of the error or {@code null} for none.
     *
     * @since 1.8
     */
    public final void setMessage(String message) {
      this.message = message;
    }

    /**
     * Returns the location in the request that caused the error or {@code null} for none or {@code
     * null} for none.
     *
     * @since 1.8
     */
    public final String getLocation() {
      return location;
    }

    /**
     * Sets the location in the request that caused the error or {@code null} for none or {@code
     * null} for none.
     *
     * @since 1.8
     */
    public final void setLocation(String location) {
      this.location = location;
    }

    /**
     * Returns the type of location in the request that caused the error or {@code null} for none.
     *
     * @since 1.8
     */
    public final String getLocationType() {
      return locationType;
    }

    /**
     * Sets the type of location in the request that caused the error or {@code null} for none.
     *
     * @since 1.8
     */
    public final void setLocationType(String locationType) {
      this.locationType = locationType;
    }

    @Override
    public ErrorInfo set(String fieldName, Object value) {
      return (ErrorInfo) super.set(fieldName, value);
    }

    @Override
    public ErrorInfo clone() {
      return (ErrorInfo) super.clone();
    }
  }

  public static class Details extends GenericJson {
    @Key("@type")
    private String type;

    @Key private String detail;
    @Key private String reason;
    @Key private List<ParameterViolations> parameterViolations;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getDetail() {
      return detail;
    }

    public void setDetail(String detail) {
      this.detail = detail;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    public List<ParameterViolations> getParameterViolations() {
      return parameterViolations;
    }

    /**
     * Sets parameterViolations list as immutable to prevent exposing mutable state.
     *
     * @param parameterViolations
     */
    public void setParameterViolations(List<ParameterViolations> parameterViolations) {
      this.parameterViolations = ImmutableList.copyOf(parameterViolations);
    }
  }

  public static class ParameterViolations {
    @Key private String parameter;
    @Key private String description;

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getParameter() {
      return parameter;
    }

    public void setParameter(String parameter) {
      this.parameter = parameter;
    }
  }

  /** List of detailed errors or {@code null} for none. */
  @Key private List<ErrorInfo> errors;

  /** HTTP status code of this response or {@code null} for none. */
  @Key private int code;

  /** Human-readable explanation of the error or {@code null} for none. */
  @Key private String message;

  /** Lists type and parameterViolation details of an Exception. */
  @Key private List<Details> details;

  /**
   * Returns the list of detailed errors or {@code null} for none.
   *
   * @since 1.8
   */
  public final List<ErrorInfo> getErrors() {
    return errors;
  }

  /**
   * Sets the list of detailed errors or {@code null} for none. Sets the list of detailed errors as
   * immutable to prevent exposing mutable state.
   *
   * @since 1.8
   */
  public final void setErrors(List<ErrorInfo> errors) {
    this.errors = ImmutableList.copyOf(errors);
  }

  /**
   * Returns the HTTP status code of this response or {@code null} for none.
   *
   * @since 1.8
   */
  public final int getCode() {
    return code;
  }

  /**
   * Sets the HTTP status code of this response or {@code null} for none.
   *
   * @since 1.8
   */
  public final void setCode(int code) {
    this.code = code;
  }

  /**
   * Returns the human-readable explanation of the error or {@code null} for none.
   *
   * @since 1.8
   */
  public final String getMessage() {
    return message;
  }

  /**
   * Sets the human-readable explanation of the error or {@code null} for none.
   *
   * @since 1.8
   */
  public final void setMessage(String message) {
    this.message = message;
  }

  public List<Details> getDetails() {
    return details;
  }

  /**
   * Sets the list of invalid parameter error details as immutable to prevent exposing mutable
   * state.
   *
   * @param details
   */
  public void setDetails(List<Details> details) {
    this.details = ImmutableList.copyOf(details);
  }

  @Override
  public GoogleJsonError set(String fieldName, Object value) {
    return (GoogleJsonError) super.set(fieldName, value);
  }

  @Override
  public GoogleJsonError clone() {
    return (GoogleJsonError) super.clone();
  }
}
