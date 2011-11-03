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

package com.google.api.client.googleapis.extensions.auth.helpers.oauth2.draft10;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleOAuth2ThreeLeggedFlow}.
 *
 * @author Yaniv Inbar
 */
public class GoogleOAuth2ThreeLeggedFlowTest extends TestCase {

  public void testConstructor() {
    GoogleOAuth2ThreeLeggedFlow flow = new GoogleOAuth2ThreeLeggedFlow(
        "userIdValue", "clientIdValue", "clientSecretValue", "scopeValue", "callbackUrlValue");
    assertEquals("https://accounts.google.com/o/oauth2/auth?client_id=clientIdValue&"
        + "redirect_uri=callbackUrlValue&response_type=code&scope=scopeValue&"
        + "access_type=offline&approval_prompt=force", flow.getAuthorizationUrl());
  }
}
