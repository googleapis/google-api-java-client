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

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract XML HTTP parser into an data class of key/value pairs.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in version 1.1) Use
 *             {@link XmlHttpParser}
 */
@Deprecated
public abstract class XmlParser implements HttpParser {

  /** XML namespace dictionary. */
  public XmlNamespaceDictionary namespaceDictionary;

  /**
   * Default implementation parses the content of the response into the data
   * class of key/value pairs, but subclasses may override.
   */
  public <T> T parse(HttpResponse response, Class<T> dataClass)
      throws IOException {
    InputStream content = response.getContent();
    try {
      T result = ClassInfo.newInstance(dataClass);
      XmlPullParser parser = Xml.createParser();
      parser.setInput(content, null);
      Xml.parseElement(parser, result, namespaceDictionary, null);
      return result;
    } catch (XmlPullParserException e) {
      IOException exception = new IOException();
      exception.initCause(e);
      throw exception;
    } finally {
      content.close();
    }
  }
}
