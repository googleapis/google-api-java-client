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

package com.google.api.client.googleapis.testing.json;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link GoogleJsonResponseExceptionFactoryTesting}
 *
 * @author Eric Mintz
 */
public class GoogleJsonResponseExceptionFactoryTestingTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new GsonFactory();
  private static final int HTTP_CODE_NOT_FOUND = 404;
  private static final String REASON_PHRASE_NOT_FOUND = "NOT FOUND";

  public void testCreateException() throws IOException {
    GoogleJsonResponseException exception =
        GoogleJsonResponseExceptionFactoryTesting.newMock(
            JSON_FACTORY, HTTP_CODE_NOT_FOUND, REASON_PHRASE_NOT_FOUND);
    assertEquals(HTTP_CODE_NOT_FOUND, exception.getStatusCode());
    assertEquals(REASON_PHRASE_NOT_FOUND, exception.getStatusMessage());
  }
}
