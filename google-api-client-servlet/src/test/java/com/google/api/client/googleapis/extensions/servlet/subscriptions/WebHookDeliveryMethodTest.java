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

package com.google.api.client.googleapis.extensions.servlet.subscriptions;

import junit.framework.TestCase;

/**
 * Tests for the {@link WebHookDeliveryMethod} class.
 *
 * @author Matthias Linder (mlinder)
 */
public class WebHookDeliveryMethodTest extends TestCase {

  /** Callback URL used for testing. */
  private static final String CALLBACK = "https://example.com/foo/bar";

  public void testBuild() {
    assertEquals("web_hook?url=" + CALLBACK,
        new WebHookDeliveryMethod(CALLBACK).build());
  }

  public void testBuild_escape() {
    assertEquals("web_hook?url=https://example.com/foo/bar%26foo",
        new WebHookDeliveryMethod(CALLBACK + "&foo").build());
  }

  public void testBuild_host() {
    assertEquals("web_hook?url=" + CALLBACK + "&host=someHost",
        new WebHookDeliveryMethod(CALLBACK).setHost("someHost").build());
    assertEquals("foo", new WebHookDeliveryMethod(CALLBACK).setHost("foo").getHost());
  }

  public void testBuild_noPayload() {
    assertEquals(
        "web_hook?url=" + CALLBACK + "&invalidate=true",
        new WebHookDeliveryMethod(CALLBACK).setPayloadRequested(false)
            .build());

    assertEquals(true, new WebHookDeliveryMethod(CALLBACK).isPayloadRequested());
    assertEquals(false,
        new WebHookDeliveryMethod(CALLBACK).setPayloadRequested(false).isPayloadRequested());
  }
}
