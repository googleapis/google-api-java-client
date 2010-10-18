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

package com.google.api.client.escape;

/**
 * An object that converts literal text into a format safe for inclusion in a particular context
 * (such as an XML document). Typically (but not always), the inverse process of "unescaping" the
 * text is performed automatically by the relevant parser.
 *
 * <p>
 * For example, an XML escaper would convert the literal string {@code "Foo<Bar>"} into {@code
 * "Foo&lt;Bar&gt;"} to prevent {@code "<Bar>"} from being confused with an XML tag. When the
 * resulting XML document is parsed, the parser API will return this text as the original literal
 * string {@code "Foo<Bar>"}.
 *
 * <p>
 * An {@code Escaper} instance is required to be stateless, and safe when used concurrently by
 * multiple threads.
 *
 * <p>
 * Several popular escapers are defined as constants in the class {@link CharEscapers}.
 *
 * @since 1.0
 */
public abstract class Escaper {

  /**
   * Returns the escaped form of a given literal string.
   *
   * <p>
   * Note that this method may treat input characters differently depending on the specific escaper
   * implementation.
   * <ul>
   * <li>{@link UnicodeEscaper} handles <a href="http://en.wikipedia.org/wiki/UTF-16">UTF-16</a>
   * correctly, including surrogate character pairs. If the input is badly formed the escaper should
   * throw {@link IllegalArgumentException}.
   * </ul>
   *
   * @param string the literal string to be escaped
   * @return the escaped form of {@code string}
   * @throws NullPointerException if {@code string} is null
   * @throws IllegalArgumentException if {@code string} contains badly formed UTF-16 or cannot be
   *         escaped for any other reason
   */
  public abstract String escape(String string);
}
