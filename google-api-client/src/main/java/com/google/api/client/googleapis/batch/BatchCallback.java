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

import com.google.api.client.googleapis.GoogleHeaders;

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

     public void onSuccess(Volumes volumes, GoogleHeaders responseHeaders) {
       log("Success");
       printVolumes(volumes.getItems());
     }

     public void onFailure(GoogleJsonErrorContainer e, GoogleHeaders responseHeaders) {
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
   * @param t Instance of the parsed data model class
   * @param responseHeaders Headers of the batch response
   */
  void onSuccess(T t, GoogleHeaders responseHeaders);

  /**
   * Called if the individual batch response is unsuccessful.
   *
   * @param e Data class representing the error response content
   * @param responseHeaders Headers of the batch response
   */
  void onFailure(E e, GoogleHeaders responseHeaders) throws IOException;
}
