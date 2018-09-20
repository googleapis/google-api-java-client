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

package com.google.api.client.googleapis.auth.oauth2;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleAuthorizationCodeFlow}.
 *
 * @author Yaniv Inbar
 */
public class GoogleAuthorizationCodeFlowTest extends TestCase {

  private static final String CLIENT_ID = "812741506391.apps.googleusercontent.com";
  private static final String CLIENT_SECRET = "{client_secret}";

  public void testBuilder() {
    GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(
        new MockHttpTransport(), new JacksonFactory(), CLIENT_ID, CLIENT_SECRET,
        ImmutableList.of("https://www.googleapis.com/auth/userinfo.email"));
    assertNull(builder.getApprovalPrompt());
    assertNull(builder.getAccessType());
  }
}
