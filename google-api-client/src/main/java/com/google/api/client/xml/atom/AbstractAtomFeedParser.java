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

import com.google.api.client.util.ClassInfo;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract base class for an Atom feed parser when the feed type is known in advance.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractAtomFeedParser<T> {

  private boolean feedParsed;
  public XmlPullParser parser;
  public InputStream inputStream;
  public Class<T> feedClass;
  public XmlNamespaceDictionary namespaceDictionary;

  /**
   * Parse the feed and return a new parsed instance of the feed type. This method can be skipped if
   * all you want are the items.
   *
   * @throws XmlPullParserException
   */
  public T parseFeed() throws IOException, XmlPullParserException {
    boolean close = true;
    try {
      this.feedParsed = true;
      T result = ClassInfo.newInstance(this.feedClass);
      Xml.parseElement(
          this.parser, result, this.namespaceDictionary, Atom.StopAtAtomEntry.INSTANCE);
      close = false;
      return result;
    } finally {
      if (close) {
        close();
      }
    }
  }

  /**
   * Parse the next item in the feed and return a new parsed instanceof of the item type. If there
   * is no item to parse, it will return {@code null} and automatically close the parser (in which
   * case there is no need to call {@link #close()}.
   *
   * @throws XmlPullParserException
   */
  public Object parseNextEntry() throws IOException, XmlPullParserException {
    XmlPullParser parser = this.parser;
    if (!this.feedParsed) {
      this.feedParsed = true;
      Xml.parseElement(parser, null, this.namespaceDictionary, Atom.StopAtAtomEntry.INSTANCE);
    }
    boolean close = true;
    try {
      if (parser.getEventType() == XmlPullParser.START_TAG) {
        Object result = parseEntryInternal();
        parser.next();
        close = false;
        return result;
      }
    } finally {
      if (close) {
        close();
      }
    }
    return null;
  }

  /** Closes the underlying parser. */
  public void close() throws IOException {
    this.inputStream.close();
  }

  protected abstract Object parseEntryInternal() throws IOException, XmlPullParserException;
}
