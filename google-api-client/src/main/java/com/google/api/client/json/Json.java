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

package com.google.api.client.json;

/**
 * JSON utilities.
 *
 * <p>
 * Upgrade warning: in prior version 1.2, there was a global static field {@code JSON_FACTORY} of
 * type {@code org.codehaus.jackson.JsonFactory}. However, now that the JSON library is pluggable
 * this doesn't make sense and instead the instance of the JSON factory must be specified every time
 * it is used. For this reason much of the functionality that used be here has been moved to
 * {@link JsonFactory}, {@link JsonGenerator}, and {@link JsonParser}.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class Json {

  /** {@code "application/json"} content type. */
  public static final String CONTENT_TYPE = "application/json";
}
