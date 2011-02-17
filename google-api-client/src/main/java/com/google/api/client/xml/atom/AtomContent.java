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

import com.google.api.client.http.xml.AbstractXmlHttpContent;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Serializes Atom XML HTTP content based on the data key/value mapping object for an Atom entry.
 * <p>
 * Default value for {@link #contentType} is {@link Atom#CONTENT_TYPE}.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(
      HttpRequest request, XmlNamespaceDictionary namespaceDictionary, Object entry) {
    AtomContent content = new AtomContent();
    content.namespaceDictionary = namespaceDictionary;
    content.entry = entry;
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class AtomContent extends AbstractXmlHttpContent {

  /** Key/value pair data for the Atom entry. */
  public Object entry;

  public AtomContent() {
    contentType = Atom.CONTENT_TYPE;
  }

  @Override
  public final void writeTo(XmlSerializer serializer) throws IOException {
    namespaceDictionary.serialize(serializer, Atom.ATOM_NAMESPACE, "entry", entry);
  }
}
