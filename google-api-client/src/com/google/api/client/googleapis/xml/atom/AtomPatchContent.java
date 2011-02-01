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

import com.google.api.client.http.xml.XmlHttpParser;
import com.google.api.client.xml.atom.AtomContent;

/**
 * Serializes Atom XML PATCH HTTP content based on the data key/value mapping object for an Atom
 * entry.
 * <p>
 * Default value for {@link #contentType} is {@link XmlHttpParser#CONTENT_TYPE}.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(
      HttpRequest request, XmlNamespaceDictionary namespaceDictionary, Object entry) {
    AtomPatchContent content = new AtomPatchContent();
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
public final class AtomPatchContent extends AtomContent {

  public AtomPatchContent() {
    contentType = XmlHttpParser.CONTENT_TYPE;
  }
}
