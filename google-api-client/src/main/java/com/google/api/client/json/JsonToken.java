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
 * JSON token in the low-level JSON library.
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
public enum JsonToken {

  /** Start of a JSON array ('['). */
  START_ARRAY,

  /** End of a JSON array (']'). */
  END_ARRAY,

  /** Start of a JSON object ('{'). */
  START_OBJECT,

  /** End of a JSON object ('}'). */
  END_OBJECT,

  /** JSON field name. */
  FIELD_NAME,

  /** JSON field string value. */
  VALUE_STRING,

  /**
   * JSON field number value of an integer with an arbitrary number of digits and no fractional
   * part.
   */
  VALUE_NUMBER_INT,

  /** JSON field number value of an arbitrary-precision decimal number. */
  VALUE_NUMBER_FLOAT,

  /** JSON field {@code true} value. */
  VALUE_TRUE,

  /** JSON field {@code false} value. */
  VALUE_FALSE,

  /** JSON {@code null}. */
  VALUE_NULL,

  /** Some other token. */
  NOT_AVAILABLE
}
