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

package com.google.api.client.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify that a field is a data key, optionally providing the data key name
 * to use.
 * <p>
 * If the data key name is not specifies, the default data key name is the field's name. For
 * example:
 *
 * <pre><code>
  public class A {

    // uses data key name of "dataKeyNameMatchesFieldName"
    &#64;Key
    public String dataKeyNameMatchesFieldName;

    // uses data key name of "some_other_name"
    &#64;Key("some_other_name")
    private String dataKeyNameIsOverriden;

    // not a data key
    private String notADataKey;
  }
 * </code></pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Key {

  /**
   * Override the data key name of the field or {@code "##default"} to use the field's name.
   */
  String value() default "##default";
}
