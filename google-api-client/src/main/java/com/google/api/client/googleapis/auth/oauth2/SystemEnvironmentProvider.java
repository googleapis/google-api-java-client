/*
 * Copyright 2015 Google Inc.
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
package com.google.api.client.googleapis.auth.oauth2;

/**
 * Abstraction which allows overriding of the system environment for tests.
 */
class SystemEnvironmentProvider {
  static final SystemEnvironmentProvider INSTANCE = new SystemEnvironmentProvider();

  /**
   * Override in test code to isolate from environment.
   */
  String getEnv(String name) {
    return System.getenv(name);
  }

  /**
   * Override in test code to isolate from environment.
   */
  boolean getEnvEquals(String name, String value) {
    return System.getenv().containsKey(name) && System.getenv(name).equals(value);
  }
}
