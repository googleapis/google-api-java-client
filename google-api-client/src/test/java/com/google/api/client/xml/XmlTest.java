/*
 * Copyright (c) 2011 Google Inc.
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

import com.google.api.client.util.Key;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Tests {@link Xml}.
 *
 * @author Yaniv Inbar
 */
public class XmlTest extends TestCase {

  public static class AnyType {
    @Key("@attr")
    public Object attr;
    @Key
    public Object elem;
    @Key
    public Object rep;
    @Key
    public ValueType value;
  }

  public static class ValueType {
    @Key("text()")
    public Object content;
  }

  private static final String XML =
      "<?xml version=\"1.0\"?><any attr=\"value\" xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<elem>content</elem><rep>rep1</rep><rep>rep2</rep><value>content</value></any>";

  public void testParse_anyType() throws Exception {
    AnyType xml = new AnyType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(XML));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(XML, out.toString());
  }

  public static class ArrayType {
    @Key
    public HashMap<String, String>[] rep;
  }

  private static final String ARRAY_TYPE =
      "<?xml version=\"1.0\"?><any xmlns=\"http://www.w3.org/2005/Atom\">"
          + "<rep>rep1</rep><rep>rep2</rep></any>";

  public void testParse_arrayType() throws Exception {
    ArrayType xml = new ArrayType();
    XmlPullParser parser = Xml.createParser();
    parser.setInput(new StringReader(ARRAY_TYPE));
    XmlNamespaceDictionary namespaceDictionary = new XmlNamespaceDictionary();
    Xml.parseElement(parser, xml, namespaceDictionary, null);
    XmlSerializer serializer = Xml.createSerializer();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    serializer.setOutput(out, "UTF-8");
    namespaceDictionary.serialize(serializer, "any", xml);
    assertEquals(ARRAY_TYPE, out.toString());
  }
}
