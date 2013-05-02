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

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Tests {@link AppIdentityCredential}.
 *
 * @author Yaniv Inbar
 */
public class AppIdentityCredentialTest extends TestCase {

  private static final Collection<String> SCOPES =
      Collections.unmodifiableCollection(Arrays.asList("scope1", "scope2"));

  public void testBuilder() {
    String[] scopes = SCOPES.toArray(new String[SCOPES.size()]);
    AppIdentityCredential.Builder builder = new AppIdentityCredential.Builder(SCOPES);
    scopes[1] = "somethingelse";
    assertTrue(Arrays.deepEquals(SCOPES.toArray(), builder.getScopes().toArray()));
    AppIdentityCredential credential = builder.build();
    assertTrue(Arrays.deepEquals(SCOPES.toArray(), credential.getScopes().toArray()));
  }
}
