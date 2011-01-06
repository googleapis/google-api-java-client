/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.testing.json;

import com.google.api.client.json.JsonEncoding;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.util.Key;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Abstract test case for {@link JsonGenerator}.
 *
 * @author Yaniv Inbar
 */
public abstract class AbstractJsonGeneratorTest extends TestCase {

  public AbstractJsonGeneratorTest(String name) {
    super(name);
  }

  protected abstract JsonFactory newFactory();

  private static final String JSON_ENTRY = "{\"title\":\"foo\"}";

  private static final String JSON_FEED =
      "{\"entries\":[" + "{\"title\":\"foo\"}," + "{\"title\":\"bar\"}]}";

  public final void testGenerateEntry() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = newFactory().createJsonGenerator(out, JsonEncoding.UTF8);
    generator.serialize(new Entry("foo"));
    generator.flush();
    assertEquals(JSON_ENTRY, new String(out.toByteArray()));
  }

  public final void testGenerateFeed() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = newFactory().createJsonGenerator(out, JsonEncoding.UTF8);
    Feed feed = new Feed();
    feed.entries.add(new Entry("foo"));
    feed.entries.add(new Entry("bar"));
    generator.serialize(feed);
    generator.flush();
    assertEquals(JSON_FEED, new String(out.toByteArray()));
  }

  public static final class Entry {
    @Key
    public String title;

    Entry(String title) {
      this.title = title;
    }
  }

  public static final class Feed {
    @Key
    public Collection<Entry> entries = new ArrayList<Entry>();
  }
}
