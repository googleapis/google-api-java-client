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

package com.google.api.client.googleapis.subscriptions;

import com.google.api.client.googleapis.subscriptions.ClientTokenGenerator;

import junit.framework.TestCase;

/**
 * Tests for the {@link ClientTokenGenerator} class.
 *
 * @author Matthias Linder
 * @since 1.11
 */
public class ClientTokenGeneratorTest extends TestCase {

  /** Tests the DEFAULT ClientTokenGenerator. */
  public void testDefaultClientTokenGenerator() throws Exception {
    ClientTokenGenerator def = ClientTokenGenerator.DEFAULT_RANDOM_GENERATOR;
    String firstToken = def.generateToken();
    String secondToken = def.generateToken();

    assertNotNull(firstToken);
    assertNotNull(secondToken);
    assertNotSame(firstToken, secondToken);
    assertTrue(firstToken.length() >= 32);
  }
}
