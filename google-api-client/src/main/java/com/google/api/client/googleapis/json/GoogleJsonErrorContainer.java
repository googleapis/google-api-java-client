/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 * Data class representing a container of {@link GoogleJsonError}.
 *
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
public class GoogleJsonErrorContainer extends GenericJson {

  @Key
  private GoogleJsonError error;

  /** Returns the {@link GoogleJsonError}. */
  public final GoogleJsonError getError() {
    return error;
  }

  /** Sets the {@link GoogleJsonError}. */
  public final void setError(GoogleJsonError error) {
    this.error = error;
  }

  @Override
  public GoogleJsonErrorContainer set(String fieldName, Object value) {
    return (GoogleJsonErrorContainer) super.set(fieldName, value);
  }

  @Override
  public GoogleJsonErrorContainer clone() {
    return (GoogleJsonErrorContainer) super.clone();
  }
}
