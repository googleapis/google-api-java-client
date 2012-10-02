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

package com.google.api.client.googleapis.json;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson.JacksonFactory;

import junit.framework.TestCase;

/**
 * Tests {@link GoogleJsonErrorContainer}.
 *
 * @author rmistry@google.com (Ravi Mistry)
 */
public class GoogleJsonErrorContainerTest extends TestCase {

  static final JsonFactory FACTORY = new JacksonFactory();
  static final String ERROR = "{" + "\"error\":{" + "\"code\":403," + "\"errors\":[{"
      + "\"domain\":\"usageLimits\"," + "\"message\":\"Access Not Configured\","
      + "\"reason\":\"accessNotConfigured\"" + "}]," + "\"message\":\"Access Not Configured\"}}";

  public void test_json() throws Exception {
    JsonParser parser = FACTORY.createJsonParser(ERROR);
    GoogleJsonErrorContainer e = parser.parse(GoogleJsonErrorContainer.class, null);
    assertEquals(ERROR, FACTORY.toString(e));
  }
}
