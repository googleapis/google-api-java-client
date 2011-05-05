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

package com.google.api.client.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify that an enum constant is a string data value, optionally providing
 * the string data value to use.
 * <p>
 * If the string data value is not specified, the default is the Java field's name. For example:
 * </p>
 *
 * <pre>
  public enum A {

    // value is "USE_FIELD_NAME"
    &#64;Value
    USE_FIELD_NAME,
    
    // value is "specifiedValue"
    &#64;Value("specifiedValue")
    USE_SPECIFIED_VALUE, 
    
    // value is null
    &#64;NullValue
    NULL_VALUE

    // not a value
    NOT_A_VALUE
  }
 * </pre>
 *
 * @since 1.4
 * @author Yaniv Inbar
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {

  /**
   * Override the string data value of the field or {@code "##default"} to use the Java field's
   * name.
   */
  String value() default "##default";
}
