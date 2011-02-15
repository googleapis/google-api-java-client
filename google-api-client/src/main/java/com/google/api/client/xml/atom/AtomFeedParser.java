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
 * Atom feed parser when the item class is known in advance.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AtomFeedParser<T, I> extends AbstractAtomFeedParser<T> {

  public Class<I> entryClass;

  @SuppressWarnings("unchecked")
  @Override
  public I parseNextEntry() throws IOException, XmlPullParserException {
    return (I) super.parseNextEntry();
  }

  @Override
  protected Object parseEntryInternal() throws IOException, XmlPullParserException {
    I result = ClassInfo.newInstance(this.entryClass);
    Xml.parseElement(parser, result, namespaceDictionary, null);
    return result;
  }

  public static <T, I> AtomFeedParser<T, I> create(HttpResponse response,
      XmlNamespaceDictionary namespaceDictionary, Class<T> feedClass, Class<I> entryClass)
      throws XmlPullParserException, IOException {
    InputStream content = response.getContent();
    try {
      Atom.checkContentType(response.contentType);
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      AtomFeedParser<T, I> result = new AtomFeedParser<T, I>();
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
