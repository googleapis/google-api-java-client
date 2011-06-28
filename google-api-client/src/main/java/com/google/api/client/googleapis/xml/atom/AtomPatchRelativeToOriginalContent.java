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
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.Atom;
import com.google.common.base.Preconditions;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * Serializes an optimal Atom XML PATCH HTTP content based on the data key/value mapping object for
 * an Atom entry, by comparing the original value to the patched value.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
 * <code>
  static void setContent(HttpRequest request, XmlNamespaceDictionary namespaceDictionary,
      Object originalEntry, Object patchedEntry) {
    request.setContent(
        new AtomPatchRelativeToOriginalContent(namespaceDictionary, originalEntry, patchedEntry));
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AtomPatchRelativeToOriginalContent extends AbstractXmlHttpContent {

  /**
   * Key/value pair data for the updated/patched Atom entry.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getPatchedEntry}
   */
  @Deprecated
  public Object patchedEntry;

  /**
   * Key/value pair data for the original unmodified Atom entry.
   *
   * @deprecated (scheduled to be made private final in 1.6) Use {@link #getOriginalEntry}
   */
  @Deprecated
  public Object originalEntry;

  /**
   * @deprecated (scheduled to be removed in 1.6) Use {@link
   *             #AtomPatchRelativeToOriginalContent(XmlNamespaceDictionary, Object, Object)}
   */
  @Deprecated
  public AtomPatchRelativeToOriginalContent() {
  }

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @since 1.5
   */
  public AtomPatchRelativeToOriginalContent(
      XmlNamespaceDictionary namespaceDictionary, Object originalEntry, Object patchedEntry) {
    super(namespaceDictionary);
    this.originalEntry = Preconditions.checkNotNull(originalEntry);
    this.patchedEntry = Preconditions.checkNotNull(patchedEntry);
  }

  @Override
  protected void writeTo(XmlSerializer serializer) throws IOException {
    Map<String, Object> patch = GoogleAtom.computePatch(patchedEntry, originalEntry);
    getNamespaceDictionary().serialize(serializer, Atom.ATOM_NAMESPACE, "entry", patch);
  }

  /**
   * Returns the data key name/value pairs for the updated/patched Atom entry.
   *
   * @since 1.5
   */
  public final Object getPatchedEntry() {
    return patchedEntry;
  }

  /**
   * Returns the data key name/value pairs for the original unmodified Atom entry.
   *
   * @since 1.5
   */
  public final Object getOriginalEntry() {
    return originalEntry;
  }
}
