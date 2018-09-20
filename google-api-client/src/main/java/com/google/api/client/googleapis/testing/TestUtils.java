/*
 * Copyright 2014 Google Inc.
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

package com.google.api.client.googleapis.testing;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for test code.
 */
public final class TestUtils {

  private static final String UTF_8 = "UTF-8";

  public static Map<String, String> parseQuery(String query) throws IOException {
    Map<String, String> map = new HashMap<String, String>();
    Iterable<String> entries = Splitter.on('&').split(query);
    for (String entry : entries) {
      List<String> sides = Lists.newArrayList(Splitter.on('=').split(entry));
      if (sides.size() != 2) {
        throw new IOException("Invalid Query String");
      }
      String key = URLDecoder.decode(sides.get(0), UTF_8);
      String value = URLDecoder.decode(sides.get(1), UTF_8);
      map.put(key, value);
    }
    return map;
  }

  private TestUtils() {
  }
}
