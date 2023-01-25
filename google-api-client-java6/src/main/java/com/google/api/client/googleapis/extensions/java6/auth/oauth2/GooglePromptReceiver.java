/*
 * Copyright 2012 Google Inc.
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

package com.google.api.client.googleapis.extensions.java6.auth.oauth2;

import com.google.api.client.extensions.java6.auth.oauth2.AbstractPromptReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import java.io.IOException;

/**
 * Google OAuth 2.0 abstract verification code receiver that prompts user to paste the code copied
 * from the browser.
 *
 * <p>This uses deprecated OAuth out-of-band (oob) flow. To migrate to an alternative flow, please
 * refer to <a href="https://developers.googleblog.com/2022/02/making-oauth-flows-safer.html">Making
 * Google OAuth interactions safer by using more secure OAuth flows</a>.
 *
 * <p>Implementation is thread-safe.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@Deprecated
public class GooglePromptReceiver extends AbstractPromptReceiver {

  @Override
  public String getRedirectUri() throws IOException {
    return GoogleOAuthConstants.OOB_REDIRECT_URI;
  }
}
