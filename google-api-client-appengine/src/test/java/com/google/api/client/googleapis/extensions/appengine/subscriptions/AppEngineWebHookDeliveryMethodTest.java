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

package com.google.api.client.googleapis.extensions.appengine.subscriptions;

import junit.framework.TestCase;

/**
 * Tests for the {@link AppEngineWebHookDeliveryMethod} class.
 *
 * @author Matthias Linder (mlinder)
 */
public class AppEngineWebHookDeliveryMethodTest extends TestCase {

  public void testGetSubscriptionHeader() {
    AppEngineWebHookDeliveryMethod whdm =
        new AppEngineWebHookDeliveryMethod("https://example.com/foo/bar");
    assertEquals("web_hook?url=https://example.com/foo/bar&appEngine=true", whdm.build());
  }
}
