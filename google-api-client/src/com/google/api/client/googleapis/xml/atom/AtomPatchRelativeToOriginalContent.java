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

import com.google.api.client.http.xml.AbstractXmlHttpContent;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.xml.atom.Atom;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Serializes an optimal Atom XML PATCH HTTP content based on the data key/value mapping object for
 * an Atom entry, by comparing the original value to the patched value.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, XmlNamespaceDictionary namespaceDictionary,
      Object originalEntry, Object patchedEntry) {
    AtomPatchRelativeToOriginalContent content = new AtomPatchRelativeToOriginalContent();
    content.namespaceDictionary = namespaceDictionary;
    content.originalEntry = originalEntry;
    content.patchedEntry = patchedEntry;
    request.content = content;
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AtomPatchRelativeToOriginalContent extends AbstractXmlHttpContent {

  /** Key/value pair data for the updated/patched Atom entry. */
  public Object patchedEntry;

  /** Key/value pair data for the original unmodified Atom entry. */
  public Object originalEntry;

  @Override
  protected void writeTo(XmlSerializer serializer) throws IOException {
    ArrayMap<String, Object> patch = GoogleAtom.computePatch(patchedEntry, originalEntry);
    namespaceDictionary.serialize(serializer, Atom.ATOM_NAMESPACE, "entry", patch);
  }
}
