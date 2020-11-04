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

package com.google.api.client.googleapis.util;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 * Tests {@link Utils}.
 *
 * @author Yaniv Inbar
 */
public class UtilsTest extends TestCase {

  public void testGetDefaultJsonFactory() {
    JsonFactory jsonFactory = Utils.getDefaultJsonFactory();
    assertNotNull(jsonFactory);
    assertTrue(jsonFactory instanceof JacksonFactory);
    JsonFactory secondCall = Utils.getDefaultJsonFactory();
    assertSame(jsonFactory, secondCall);
  }

  public void testGetDefaultTransport() {
    HttpTransport transport = Utils.getDefaultTransport();
    assertNotNull(transport);
    assertTrue(transport instanceof NetHttpTransport);
    HttpTransport secondCall = Utils.getDefaultTransport();
    assertSame(transport, secondCall);
  }

  public static Map<String, String> parseQuery(String query) throws IOException {
    Map<String, String> map = new HashMap<String, String>();
    String[] entries = query.split("&");
    for (String entry : entries) {
      String[] sides = entry.split("=");
      if (sides.length != 2) {
        throw new IOException("Invalid Query String");
      }
      String key = URLDecoder.decode(sides[0], "UTF-8");
      String value = URLDecoder.decode(sides[1], "UTF-8");
      map.put(key, value);
    }
    return map;
  }

  public void testExtractCertificateProviderCommand() throws IOException {
    String json = "{\"cert_provider_command\":[\"/opt/google/endpoint-verification/bin/apihelper\",\"--print_certificate\"],\"device_resource_ids\":[\"123\"]}";
    ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
    List<String> command = Utils.extractCertificateProviderCommand(stream);
    assertEquals(2, command.size());
    assertEquals("/opt/google/endpoint-verification/bin/apihelper", command.get(0));
    assertEquals("--print_certificate", command.get(1));
  }
}
