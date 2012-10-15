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

package com.google.api.client.googleapis.xml.atom;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Types;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.AbstractAtomFeedParser;
import com.google.api.client.xml.atom.Atom;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * GData Atom feed pull parser when the entry class can be computed from the kind.
 *
 * @param <T> feed type
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class MultiKindFeedParser<T> extends AbstractAtomFeedParser<T> {

  private final HashMap<String, Class<?>> kindToEntryClassMap = new HashMap<String, Class<?>>();

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param parser XML pull parser to use
   * @param inputStream input stream to read
   * @param feedClass feed class to parse
   */
  MultiKindFeedParser(XmlNamespaceDictionary namespaceDictionary, XmlPullParser parser,
      InputStream inputStream, Class<T> feedClass) {
    super(namespaceDictionary, parser, inputStream, feedClass);
  }

  /** Sets the entry classes to use when parsing. */
  public void setEntryClasses(Class<?>... entryClasses) {
    int numEntries = entryClasses.length;
    HashMap<String, Class<?>> kindToEntryClassMap = this.kindToEntryClassMap;
    for (int i = 0; i < numEntries; i++) {
      Class<?> entryClass = entryClasses[i];
      ClassInfo typeInfo = ClassInfo.of(entryClass);
      Field field = typeInfo.getField("@gd:kind");
      if (field == null) {
        throw new IllegalArgumentException("missing @gd:kind field for " + entryClass.getName());
      }
      Object entry = Types.newInstance(entryClass);
      String kind = (String) FieldInfo.getFieldValue(field, entry);
      if (kind == null) {
        throw new IllegalArgumentException(
            "missing value for @gd:kind field in " + entryClass.getName());
      }
      kindToEntryClassMap.put(kind, entryClass);
    }
  }

  @Override
  protected Object parseEntryInternal() throws IOException, XmlPullParserException {
    XmlPullParser parser = getParser();
    String kind = parser.getAttributeValue(GoogleAtom.GD_NAMESPACE, "kind");
    Class<?> entryClass = this.kindToEntryClassMap.get(kind);
    if (entryClass == null) {
      throw new IllegalArgumentException("unrecognized kind: " + kind);
    }
    Object result = Types.newInstance(entryClass);
    Xml.parseElement(parser, result, getNamespaceDictionary(), null);
    return result;
  }

  /**
   * Parses the given HTTP response using the given feed class and entry classes.
   *
   * @param <T> feed type
   * @param <E> entry type
   * @param response HTTP response
   * @param namespaceDictionary XML namespace dictionary
   * @param feedClass feed class
   * @param entryClasses entry class
   * @return Atom multi-kind feed pull parser
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  public static <T, E> MultiKindFeedParser<T> create(HttpResponse response,
      XmlNamespaceDictionary namespaceDictionary, Class<T> feedClass, Class<E>... entryClasses)
      throws IOException, XmlPullParserException {
    InputStream content = response.getContent();
    try {
      Atom.checkContentType(response.getContentType());
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      MultiKindFeedParser<T> result =
          new MultiKindFeedParser<T>(namespaceDictionary, parser, content, feedClass);
      result.setEntryClasses(entryClasses);
      return result;
    } finally {
      content.close();
    }
  }
}
