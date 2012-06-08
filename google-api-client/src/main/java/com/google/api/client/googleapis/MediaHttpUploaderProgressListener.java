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

package com.google.api.client.googleapis;

import java.io.IOException;

/**
 * An interface for receiving progress notifications for uploads.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static class MyProgressListener implements MediaHttpUploaderProgressListener {

    public void progressChanged(MediaHttpUploader uploader) throws IOException {
      switch (uploader.getUploadState()) {
        case INITIATION_STARTED:
          System.out.println("Initiation Started");
          break;
        case INITIATION_COMPLETE:
          System.out.println("Initiation Completed");
          break;
        case MEDIA_IN_PROGRESS:
          System.out.println("Upload in progress");
          System.out.println("Upload percentage: " + uploader.getProgress());
          break;
        case MEDIA_COMPLETE:
          System.out.println("Upload Completed!");
          break;
      }
    }
  }
 * </pre>
 *
 * @author rmistry@google.com (Ravi Mistry)
 *
 * @deprecated (scheduled to be removed in 1.11) Use
 *             {@link com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener}
 */
@Deprecated
public interface MediaHttpUploaderProgressListener {

  /**
   * Called to notify that progress has been changed.
   *
   * <p>
   * This method is called once before and after the initiation request. For media uploads it is
   * called multiple times depending on how many chunks are uploaded. Once the upload completes it
   * is called one final time.
   * </p>
   *
   * @param uploader Media HTTP uploader
   */
  public void progressChanged(MediaHttpUploader uploader) throws IOException;
}
