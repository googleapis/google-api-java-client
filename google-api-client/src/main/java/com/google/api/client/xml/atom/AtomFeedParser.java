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

package com.google.api.client.xml.atom;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Atom feed pull parser when the Atom entry class is known in advance.
 *
 * @param <T> feed type
 * @param <E> entry type
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AtomFeedParser<T, E> extends AbstractAtomFeedParser<T> {

  /** Atom entry class to parse. */
  public Class<E> entryClass;

  @SuppressWarnings("unchecked")
  @Override
  public E parseNextEntry() throws IOException, XmlPullParserException {
    return (E) super.parseNextEntry();
  }

  @Override
  protected Object parseEntryInternal() throws IOException, XmlPullParserException {
    E result = ClassInfo.newInstance(this.entryClass);
    Xml.parseElement(parser, result, namespaceDictionary, null);
    return result;
  }

  /**
   * Parses the given HTTP response using the given feed class and entry class.
   *
   * @param <T> feed type
   * @param <E> entry type
   * @param response HTTP response
   * @param namespaceDictionary XML namespace dictionary
   * @param feedClass feed class
   * @param entryClass entry class
   * @return Atom feed parser
   * @throws IOException I/O exception
   * @throws XmlPullParserException XML pull parser exception
   */
  public static <T, E> AtomFeedParser<T, E> create(HttpResponse response,
      XmlNamespaceDictionary namespaceDictionary, Class<T> feedClass, Class<E> entryClass)
      throws IOException, XmlPullParserException {
    InputStream content = response.getContent();
    try {
      Atom.checkContentType(response.contentType);
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      AtomFeedParser<T, E> result = new AtomFeedParser<T, E>();
      result.parser = parser;
      result.inputStream = content;
      result.feedClass = feedClass;
      result.entryClass = entryClass;
      result.namespaceDictionary = namespaceDictionary;
      content = null;
      return result;
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }
}
