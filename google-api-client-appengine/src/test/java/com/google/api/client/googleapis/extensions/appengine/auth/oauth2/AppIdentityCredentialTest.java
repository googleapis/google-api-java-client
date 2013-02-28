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

package com.google.api.client.googleapis.extensions.appengine.auth.oauth2;

import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

/**
 * Tests {@link AppIdentityCredential}.
 *
 * @author Yaniv Inbar
 */
public class AppIdentityCredentialTest extends TestCase {

  private static final ImmutableList<String> SCOPES = ImmutableList.of("scope1", "scope2");

  public void testBuilder() {
    String[] scopes = SCOPES.toArray(new String[SCOPES.size()]);
    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    scopes[1] = "somethingelse";
    assertEquals(SCOPES, builder.getScopes());
    AppIdentityCredential credential = builder.build();
    assertEquals(SCOPES, credential.getScopes());
  }
}
