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

package com.google.api.client.googleapis.batch;

import com.google.api.client.http.HttpHeaders;

import java.io.IOException;

/**
 * Callback for an individual batch response.
 *
 * <p>
 * Sample use:
 * </p>
 *
 * <pre>
   batch.queue(volumesList.buildHttpRequest(), Volumes.class, GoogleJsonErrorContainer.class,
       new BatchCallback&lt;Volumes, GoogleJsonErrorContainer&gt;() {

     public void onSuccess(Volumes volumes, HttpHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
       log(e.getError().getMessage());
     }
   });
 * </pre>
 *
 * @param <T> Type of the data model class
 * @param <E> Type of the error data model class
 * @since 1.9
 * @author rmistry@google.com (Ravi Mistry)
 */
public interface BatchCallback<T, E> {

  /**
   * Called if the individual batch response is successful.
   *
   * <p>
   * Upgrade warning: this method now throws an {@link IOException}. In prior version 1.11 it did
   * not throw an exception.
   * </p>
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the response headers were of type
   * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
   * {@link HttpHeaders}.
   * </p>
   *
   * @param t instance of the parsed data model class
   * @param responseHeaders Headers of the batch response
   */
  void onSuccess(T t, HttpHeaders responseHeaders) throws IOException;

  /**
   * Called if the individual batch response is unsuccessful.
   *
   * <p>
   * Upgrade warning: in prior version 1.12 the response headers were of type
   * {@code GoogleHeaders}, but as of version 1.13 that type is deprecated, so we now use type
   * {@link HttpHeaders}.
   * </p>
   *
   * @param e instance of data class representing the error response content
   * @param responseHeaders Headers of the batch response
   */
  void onFailure(E e, HttpHeaders responseHeaders) throws IOException;
}
