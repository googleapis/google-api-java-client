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

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Customizes the behavior of a JSON parser.
 * <p>
 * All methods have a default trivial implementation, so subclasses need only implement the methods
 * whose behavior needs customization.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class CustomizeJsonParser {

  /**
   * Returns whether to stop parsing at the given key of the given context object.
   */
  public boolean stopAt(Object context, String key) {
    return false;
  }

  /**
   * Called when the given unrecognized key is encountered in the given context object.
   */
  public void handleUnrecognizedKey(Object context, String key) {
  }

  /**
   * Returns a new instance value for the given field in the given context object for a JSON array
   * or {@code null} for the default behavior.
   */
  public Collection<Object> newInstanceForArray(Object context, Field field) {
    return null;
  }

  /**
   * Returns a new instance value for the given field class in the given context object for JSON
   * Object or {@code null} for the default behavior.
   */
  public Object newInstanceForObject(Object context, Class<?> fieldClass) {
    return null;
  }
}
