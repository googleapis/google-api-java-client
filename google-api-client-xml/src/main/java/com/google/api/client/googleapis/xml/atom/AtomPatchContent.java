/*
 * Copyright 2010 Google Inc.
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

import com.google.api.client.http.HttpMediaType;
import com.google.api.client.http.xml.atom.AtomContent;
import com.google.api.client.util.Beta;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

/**
 * {@link Beta} <br>
 * Serializes Atom XML PATCH HTTP content based on the data key/value mapping object for an Atom
 * entry.
 *
 * <p>Default value for {@link #getType()} is {@link Xml#MEDIA_TYPE}.
 *
 * <p>Sample usage:
 *
 * <pre>{@code
 * static void setContent(
 *   HttpRequest request, XmlNamespaceDictionary namespaceDictionary, Object patchEntry) {
 *   request.setContent(new AtomPatchContent(namespaceDictionary, patchEntry));
 * }
 * }</pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class AtomPatchContent extends AtomContent {

  /**
   * @param namespaceDictionary XML namespace dictionary
   * @param patchEntry key/value pair data for the Atom PATCH entry
   * @since 1.5
   */
  public AtomPatchContent(XmlNamespaceDictionary namespaceDictionary, Object patchEntry) {
    super(namespaceDictionary, patchEntry, true);
    setMediaType(new HttpMediaType(Xml.MEDIA_TYPE));
  }

  @Override
  public AtomPatchContent setMediaType(HttpMediaType mediaType) {
    super.setMediaType(mediaType);
    return this;
  }
}
