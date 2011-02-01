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

import com.google.api.client.http.xml.XmlHttpParser;

/**
 * Atom XML HTTP parser into an data class of key/value pairs.
 * <p>
 * It overrides the {@link #contentType} to {@link Atom#CONTENT_TYPE}.
 * <p>
 * Sample usage:
 *
 * <pre>
 * <code>
  static void setParser(HttpTransport transport) {
    AtomParser parser = new AtomParser();
    parser.namespaceDictionary = NAMESPACE_DICTIONARY;
    transport.addParser(parser);
  }
 * </code>
 * </pre>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class AtomParser extends XmlHttpParser {

  public AtomParser() {
    contentType = Atom.CONTENT_TYPE;
  }
}
