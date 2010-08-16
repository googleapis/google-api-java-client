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

package com.google.api.client.xml;

import com.google.api.client.util.Key;
import com.google.api.client.xml.atom.Atom;

import junit.framework.TestCase;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Tests {@link XmlNamespaceDictionary}.
 *
 * @author Yaniv Inbar
 */
public class XmlNamespaceDictionaryTest extends TestCase {

  public XmlNamespaceDictionaryTest() {
  }

  public XmlNamespaceDictionaryTest(String name) {
    super(name);
  }

  private static final String EXPECTED =
      "<?xml version=\"1.0\"?>" + "<feed xmlns=\"http://www.w3.org/2005/Atom\" "
          + "xmlns:gd=\"http://schemas.google.com/g/2005\">"
          + "<entry gd:etag=\"abc\"><title>One</title></entry>"
          + "<entry gd:etag=\"def\"><title>Two</title></entry></feed>";

  public void testSerialize() throws Exception {
    Feed feed = new Feed();
    feed.entries = new TreeSet<Entry>();
    feed.entries.add(new Entry("One", "abc"));
    feed.entries.add(new Entry("Two", "def"));
    StringWriter writer = new StringWriter();
    XmlSerializer serializer = Xml.createSerializer();
    serializer.setOutput(writer);
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    namespaceDictionary.addNamespace("", "http://www.w3.org/2005/Atom");
    namespaceDictionary.addNamespace("gd", "http://schemas.google.com/g/2005");
    namespaceDictionary.serialize(
        serializer, Atom.ATOM_NAMESPACE, "feed", feed);
    assertEquals(EXPECTED, writer.toString());
  }

  public static class Entry implements Comparable<Entry> {
    @Key
    public String title;

    @Key("@gd:etag")
    public String etag;

    public Entry(String title, String etag) {
      super();
      this.title = title;
      this.etag = etag;
    }

    public int compareTo(Entry other) {
      return title.compareTo(other.title);
    }
  }

  public static class Feed {
    @Key("entry")
    public Collection<Entry> entries;

  }
}
