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

package com.google.api.client.googleapis.extensions.android.gms.auth;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.util.Beta;

import android.app.Activity;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Wraps a {@link GooglePlayServicesAvailabilityException} into an {@link IOException} so it can be
 * caught directly.
 *
 * <p>
 * Use {@link #getConnectionStatusCode()} to display the error dialog. Alternatively, use
 * {@link #getCause()} to get the wrapped {@link GooglePlayServicesAvailabilityException}. Example
 * usage:
 * </p>
 *
 * <pre>
    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
      myActivity.runOnUiThread(new Runnable() {
        public void run() {
          Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
              availabilityException.getConnectionStatusCode(),
              myActivity,
              MyActivity.REQUEST_GOOGLE_PLAY_SERVICES);
          dialog.show();
        }
      });
 * </pre>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
@Beta
public class GooglePlayServicesAvailabilityIOException extends UserRecoverableAuthIOException {

  private static final long serialVersionUID = 1L;

  /**
   * @since 1.21.0
   */
  public GooglePlayServicesAvailabilityIOException(
      GooglePlayServicesAvailabilityException wrapped) {
    super(wrapped);
  }

  @Override
  public GooglePlayServicesAvailabilityException getCause() {
    return (GooglePlayServicesAvailabilityException) super.getCause();
  }

  /**
   * Returns the error code to use with
   * {@link GooglePlayServicesUtil#getErrorDialog(int, Activity, int)}.
   */
  public final int getConnectionStatusCode() {
    return getCause().getConnectionStatusCode();
  }
}
