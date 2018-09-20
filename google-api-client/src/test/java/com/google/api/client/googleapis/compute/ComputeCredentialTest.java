/*
 * Copyright 2013 Google Inc.
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

package com.google.api.client.googleapis.compute;


import com.google.api.client.googleapis.testing.compute.MockMetadataServerTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import junit.framework.TestCase;

/**
 * Tests {@link ComputeCredential}.
 *
 * @author Yaniv Inbar
 */
public class ComputeCredentialTest extends TestCase {

  static final String ACCESS_TOKEN = "ya29.AHES6ZRN3-HlhAPya30GnW_bHSb_QtAS08i85nHq39HE3C2LTrCARA";

  public void testExecuteRefreshToken() throws Exception {

    HttpTransport transport = new MockMetadataServerTransport(ACCESS_TOKEN);

    ComputeCredential credential = new ComputeCredential(transport, new JacksonFactory());

    assertTrue(credential.refreshToken());
    assertEquals(ACCESS_TOKEN, credential.getAccessToken());
  }
}
