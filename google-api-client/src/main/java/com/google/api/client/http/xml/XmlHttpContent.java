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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Serializes XML HTTP content based on the data key/value mapping object for an item.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, XmlNamespaceDictionary namespaceDictionary,
      String elementName, Object data) {
    XmlHttpContent content = new XmlHttpContent();
    content.namespaceDictionary = namespaceDictionary;
    content.elementName = elementName;
    content.data = data;
    request.content = content;
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
public class XmlHttpContent extends AbstractXmlHttpContent {

  /**
   * XML element local name, optionally prefixed by its namespace alias, for example {@code
   * "atom:entry"}.
   */
  public String elementName;

  /** Key/value pair data. */
  public Object data;

  @Override
  public final void writeTo(XmlSerializer serializer) throws IOException {
    namespaceDictionary.serialize(serializer, elementName, data);
  }
}
