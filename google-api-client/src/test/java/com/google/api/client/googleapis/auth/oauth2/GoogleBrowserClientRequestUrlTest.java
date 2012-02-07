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

package com.google.api.client.googleapis.auth.oauth2;

import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Tests {@link GoogleBrowserClientRequestUrl}.
 *
 * @author Yaniv Inbar
 */
public class GoogleBrowserClientRequestUrlTest extends TestCase {

  private static final String EXPECTED =
      "https://accounts.google.com/o/oauth2/auth?client_id=812741506391.apps.googleusercontent.com&"
      + "redirect_uri=https://oauth2-login-demo.appspot.com/oauthcallback&response_type=token"
      + "&scope=https://www.googleapis.com/auth/userinfo.email%20"
      + "https://www.googleapis.com/auth/userinfo.profile&state=/profile";

  public void testBuild() {
    assertEquals(EXPECTED, new GoogleBrowserClientRequestUrl(
        "812741506391.apps.googleusercontent.com",
        "https://oauth2-login-demo.appspot.com/oauthcallback", Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile")).setState("/profile").build());
  }
}
