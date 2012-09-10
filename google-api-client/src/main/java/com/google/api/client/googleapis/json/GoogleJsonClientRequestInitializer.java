/*
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

import com.google.api.client.googleapis.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.GoogleClientRequestInitializer;

/**
 * Google JSON client request initializer.
 *
 * <p>
 * For example, this might be used to set a key URL query parameter on all requests:
 * </p>
 *
 * <pre>
  public class KeyRequestInitializer extends GoogleJsonClientRequestInitializer {
    &#64;Override
    protected void initialize(GoogleJsonClientRequest request) throws IOException {
      request.put("key", KEY);
    }
  }
 * </pre>
 *
 * <p>
 * Implementations should normally be thread-safe.
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public abstract class GoogleJsonClientRequestInitializer implements GoogleClientRequestInitializer {

  public final void initialize(AbstractGoogleClientRequest<?> request) throws Exception {
    initialize((AbstractGoogleJsonClientRequest<?>) request);
  }

  /** Initializes a Google JSON client request. */
  protected abstract void initialize(AbstractGoogleJsonClientRequest<?> request) throws Exception;
}
