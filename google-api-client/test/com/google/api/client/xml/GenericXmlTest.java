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

package com.google.api.client.xml;

import com.google.api.client.util.ArrayMap;

import org.xmlpull.v1.XmlPullParser;

import junit.framework.TestCase;

import java.io.StringReader;
import java.util.Collection;

/**
 * Tests {@link GenericXml}.
 *
 * @author Yaniv Inbar
 */
public class GenericXmlTest extends TestCase {

  public GenericXmlTest() {
  }

  public GenericXmlTest(String name) {
    super(name);
  }

  private static final String XML =
      "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\" "
          + "xmlns:gd=\"http://schemas.google.com/g/2005\"><atom:entry "
          + "xmlns=\"http://schemas.google.com/g/2005\" "
          + "xmlns:atom=\"http://www.w3.org/2005/Atom\" "
          + "gd:etag=\"abc\"><atom:title>One</atom:title></atom:entry>"
          + "<entry gd:etag=\"def\"><title>Two</title></entry></feed>";

  @SuppressWarnings("unchecked")
  public void testParse() throws Exception {
    GenericXml xml = new GenericXml();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    ArrayMap<String, String> expected =
        ArrayMap.of("gd", "http://schemas.google.com/g/2005", "", "http://www.w3.org/2005/Atom");
    assertEquals(expected, namespaceDictionary.getAliasToUriMap());
    assertEquals("feed", xml.name);
    Collection<GenericXml> foo = (Collection<GenericXml>) xml.get("entry");
    // TODO(yanivi): check contents of foo
    assertEquals(2, foo.size());
  }
}
