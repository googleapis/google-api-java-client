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

package com.google.api.client.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify that a declared numeric Java field should map to a JSON string.
 *
 * <p>
 * By default declared Java numeric fields are stored as JSON numbers. For example:
 *
 * <pre>
 * <code>
class A {
  @Key BigInteger value;
}
 * </code>
 * </pre>
 *
 *  can be used for this JSON content:
 *
 * <pre>
 * <code>
{"value" : 12345768901234576890123457689012345768901234576890}
 * </code>
 * </pre>
 *
 *  However, if instead the JSON content uses a JSON String to store the value, one needs to use the
 * {@link JsonString} annotation. For example:
 *
 * <pre>
 * <code>
class B {
  @Key @JsonString BigInteger value;
}
 * </code>
 * </pre>
 *
 *  can be used for this JSON content:
 *
 * <pre>
 * <code>
{"value" : "12345768901234576890123457689012345768901234576890"}
 * </code>
 * </pre>
 * </p>
 *
 * @since 1.3
 * @author Yaniv Inbar
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonString {
}
