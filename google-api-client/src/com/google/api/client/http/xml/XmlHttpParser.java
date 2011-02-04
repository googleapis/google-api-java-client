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

package com.google.api.client.http.xml;

import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.ClassInfo;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * XML HTTP parser into an data class of key/value pairs.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setParser(HttpTransport transport) {
    XmlHttpParser parser = new XmlHttpParser();
    parser.namespaceDictionary = NAMESPACE_DICTIONARY;
    transport.addParser(parser);
  }
 * </code>
 * </pre>
 *
 * <p>
 * Upgrade warning: in prior version 1.2 this class was previously in the
 * {@link com.google.api.client.xml} package.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class XmlHttpParser implements HttpParser {

  /** {@code "application/xml"} content type. */
  public static final String CONTENT_TYPE = "application/xml";

  /** Content type. Default value is {@link #CONTENT_TYPE}. */
  public String contentType = CONTENT_TYPE;

  /** XML namespace dictionary. */
  public XmlNamespaceDictionary namespaceDictionary;

  public final String getContentType() {
    return contentType;
  }

  /**
   * Default implementation parses the content of the response into the data class of key/value
   * pairs, but subclasses may override.
   */
  public <T> T parse(HttpResponse response, Class<T> dataClass) throws IOException {
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
