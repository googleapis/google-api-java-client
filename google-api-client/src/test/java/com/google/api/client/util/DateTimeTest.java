/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.util;

import com.google.api.client.util.DateTime;

import junit.framework.TestCase;

import java.util.Date;

/**
 * Tests {@link DateTime}.
 *
 * @author Yaniv Inbar
 */
public class DateTimeTest extends TestCase {

  public DateTimeTest() {
  }

  public DateTimeTest(String testName) {
    super(testName);
  }

  public void testEquals() {
    assertEquals(new DateTime(1234567890L), new DateTime(1234567890L, 120));
    assertTrue("Check equals with two identical tz specified.",
        new DateTime(1234567890L, -240).equals(
            new DateTime(1234567890L, -240)));
    assertTrue("Check equals with two different tz specified.",
        new DateTime(1234567890L, 60).equals(new DateTime(1234567890L, 240)));

    assertFalse("Check not equal.",
        new DateTime(1234567890L).equals(new DateTime(9876543210L)));
    assertFalse("Check not equal with tz.",
        new DateTime(1234567890L, 120).equals(new DateTime(9876543210L, 120)));
    assertFalse("Check not equal with Date.",
        new DateTime(1234567890L).equals(new Date(9876543210L)));
  }

  public void testParseDateTime() {
    expectExceptionForParseRfc3339("");
    expectExceptionForParseRfc3339("abc");
    DateTime value = DateTime.parseRfc3339("2007-06-01");
    assertTrue(value.dateOnly);
    value = DateTime.parseRfc3339("2007-06-01T10:11:30.057");
    assertFalse(value.dateOnly);
  }

  private void expectExceptionForParseRfc3339(String input) {
    try {
      DateTime.parseRfc3339(input);
      fail("expected NumberFormatException");
    } catch (NumberFormatException e) {
      // expected
    }
  }
}
