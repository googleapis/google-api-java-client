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

package com.google.api.client.googleapis.batch.json;

import com.google.api.client.googleapis.batch.BatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.http.HttpHeaders;

import java.io.IOException;

/**
 * Callback for an individual batch JSON response.
 *
 * <p>
 * Sample use:
 * </p>
 *
 * <pre>
   batch.queue(volumesList.buildHttpRequest(), Volumes.class, GoogleJsonErrorContainer.class,
       new JsonBatchCallback&lt;Volumes&gt;() {

     public void onSuccess(Volumes volumes, HttpHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
       log(e.getMessage());
     }
   });
 * </pre>
 *
 * @param <T> Type of the data model class
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
public abstract class JsonBatchCallback<T> implements BatchCallback<T, GoogleJsonErrorContainer> {

  public final void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders)
      throws IOException {
    onFailure(e.getError(), responseHeaders);
  }

  /**
   * Called if the individual batch response is unsuccessful.
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the response headers were of type
   * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
   * {@link HttpHeaders}.
   * </p>
   *
   * @param e Google JSON error response content
   * @param responseHeaders Headers of the batch response
   */
  public abstract void onFailure(GoogleJsonError e, HttpHeaders responseHeaders)
      throws IOException;
}
