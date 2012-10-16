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

import com.google.android.gms.auth.UserRecoverableAuthException;

import android.app.Activity;
import android.content.Intent;

import java.io.IOException;

/**
 * Wraps a {@link UserRecoverableAuthException} into an {@link IOException} so it can be caught
 * directly.
 *
 * <p>
 * Use {@link #getCause()} to get the wrapped {@link UserRecoverableAuthException}. Use
 * {@link #getIntent()} to allow user interaction to recover.
 * </p>
 *
 * @since 1.12
 * @author Yaniv Inbar
 */
public class UserRecoverableAuthIOException extends GoogleAuthIOException {

  private static final long serialVersionUID = 1L;

  UserRecoverableAuthIOException(UserRecoverableAuthException wrapped) {
    super(wrapped);
  }

  @Override
  public UserRecoverableAuthException getCause() {
    return (UserRecoverableAuthException) super.getCause();
  }

  /**
   * Returns the {@link Intent} that when supplied to
   * {@link Activity#startActivityForResult(Intent, int)} will allow user intervention.
   */
  public Intent getIntent() {
    return getCause().getIntent();
  }
}
