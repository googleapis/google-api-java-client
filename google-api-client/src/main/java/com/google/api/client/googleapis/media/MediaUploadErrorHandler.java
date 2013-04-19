/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.googleapis.media;

import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MediaUpload error handler handles an {@link IOException} and an abnormal HTTP response by calling
 * to {@link MediaHttpUploader#serverErrorCallback()}.
 *
 * @author Eyal Peled
 */
@Beta
class MediaUploadErrorHandler implements HttpUnsuccessfulResponseHandler, HttpIOExceptionHandler {

  static final Logger LOGGER = Logger.getLogger(MediaUploadErrorHandler.class.getName());

  /** The uploader to callback on if there is a server error. */
  private final MediaHttpUploader uploader;

  /** The original {@link HttpIOExceptionHandler} of the HTTP request. */
  private final HttpIOExceptionHandler originalIOExceptionHandler;

  /** The original {@link HttpUnsuccessfulResponseHandler} of the HTTP request. */
  private final HttpUnsuccessfulResponseHandler originalUnsuccessfulHandler;

  /**
   * Constructs a new instance from {@link MediaHttpUploader} and {@link HttpRequest}.
   */
  public MediaUploadErrorHandler(MediaHttpUploader uploader, HttpRequest request) {
    this.uploader = Preconditions.checkNotNull(uploader);
    originalIOExceptionHandler = request.getIOExceptionHandler();
    originalUnsuccessfulHandler = request.getUnsuccessfulResponseHandler();

    request.setIOExceptionHandler(this);
    request.setUnsuccessfulResponseHandler(this);
  }

  public boolean handleIOException(HttpRequest request, boolean supportsRetry) throws IOException {
    boolean handled = originalIOExceptionHandler != null
        && originalIOExceptionHandler.handleIOException(request, supportsRetry);

    // TODO(peleyal): figure out what is best practice - call serverErrorCallback only if I/O
    // exception was handled, or call it regardless
    if (handled) {
      try {
        uploader.serverErrorCallback();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "exception thrown while calling server callback", e);
      }
    }
    return handled;
  }

  public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry)
      throws IOException {
    boolean handled = originalUnsuccessfulHandler != null
        && originalUnsuccessfulHandler.handleResponse(request, response, supportsRetry);

    // TODO(peleyal): figure out what is best practice - call serverErrorCallback only if the
    // abnormal response was handled, or call it regardless
    if (handled && supportsRetry && response.getStatusCode() / 100 == 5) {
      try {
        uploader.serverErrorCallback();
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "exception thrown while calling server callback", e);
      }
    }
    return handled;
  }
}
