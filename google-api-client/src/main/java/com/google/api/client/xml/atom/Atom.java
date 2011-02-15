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

import com.google.api.client.xml.Xml;

/**
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class Atom {

  public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
  public static final String CONTENT_TYPE = "application/atom+xml";

  static final class StopAtAtomEntry extends Xml.CustomizeParser {

    static final StopAtAtomEntry INSTANCE = new StopAtAtomEntry();

    @Override
    public boolean stopBeforeStartTag(String namespace, String localName) {
      return "entry".equals(localName) && ATOM_NAMESPACE.equals(namespace);
    }
  }

  private Atom() {
  }

  public static void checkContentType(String contentType) {
    if (contentType == null || !contentType.startsWith(CONTENT_TYPE)) {
      throw new IllegalArgumentException(
          "Wrong content type: expected <" + CONTENT_TYPE + "> but got <" + contentType + ">");
    }
  }
}
