/*
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

package com.google.api.client.googleapis.services;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test for {@link EndToEndChecksumHandler}.
 */
@RunWith(JUnit4.class)
public class EndToEndChecksumHandlerTest {
  private byte[] payloadBytes;
  private String expectedChecksum;

  @Before
  public void setUp() {
    payloadBytes =
        "This is a long tring with numbers 1234, 134.56 boolean value true".getBytes(UTF_8);
    expectedChecksum = EndToEndChecksumHandler.computeChecksum(payloadBytes);
  }

  @Test
  public void validateChecksum_http_api_correctChecksum() {
    String computed = EndToEndChecksumHandler.computeChecksum(payloadBytes);
    assertTrue(EndToEndChecksumHandler.validateChecksumString(computed, payloadBytes));
  }

  @Test
  public void validateChecksum_http_api_negativeTestcases() {
    // negative testcase:  pass incorrect checksum to the call.
    String computed = EndToEndChecksumHandler.computeChecksum("random string".getBytes(UTF_8));
    assertFalse(EndToEndChecksumHandler.validateChecksumString(computed, payloadBytes));
    // negative testcase: pass null checksum but payload is not null.
    assertFalse(EndToEndChecksumHandler.validateChecksumString(null, payloadBytes));
    // negative testcase: pass empty checksum but payload is not null.
    assertFalse(EndToEndChecksumHandler.validateChecksumString("", payloadBytes));
    // negative testcase: payload is null but checksum exists
    assertFalse(EndToEndChecksumHandler.validateChecksumString(computed, null));
    // negative testcase: payload is empty but checksum exists
    assertFalse(EndToEndChecksumHandler.validateChecksumString(computed, new byte[0]));
  }

  @Test
  public void computeChecksum_negativeTestcases() {
    assertNull(EndToEndChecksumHandler.computeChecksum(null));
    assertNull(EndToEndChecksumHandler.computeChecksum(new byte[0]));
  }
}
