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

package com.google.api.data.sites.v1.atom;

import com.google.api.client.xml.XmlNamespaceDictionary;

import java.util.Map;

/**
 * Utilities for the Atom XML format of the Google Sites Data API.
 *
 * @deprecated (scheduled to be removed in version 1.1) Copy into your own
 * application
 */
@Deprecated
public final class GoogleSitesAtom {

  /**
   * XML namespace dictionary.
   *
   * @deprecated (scheduled to be removed in version 1.1) Copy into your own
   * application
   */
  @Deprecated
  public static final XmlNamespaceDictionary NAMESPACE_DICTIONARY = new
      XmlNamespaceDictionary();
  static {
    Map<String, String> map = NAMESPACE_DICTIONARY.namespaceAliasToUriMap;
    map.put("", "http://www.w3.org/2005/Atom");
    map.put("app", "http://www.w3.org/2007/app");
    map.put("atom", "http://www.w3.org/2005/Atom");
    map.put("batch", "http://schemas.google.com/gdata/batch");
    map.put("dc", "http://purl.org/dc/terms");
    map.put("gAcl", "http://schemas.google.com/acl/2007");
    map.put("gd", "http://schemas.google.com/g/2005");
    map.put("gs", "http://schemas.google.com/spreadsheets/2006");
    map.put("openSearch", "http://a9.com/-/spec/opensearch/1.1/");
    map.put("sites", "http://schemas.google.com/sites/2008");
    map.put("thr", "http://purl.org/syndication/thread/1.0");
    map.put("xml", "http://www.w3.org/XML/1998/namespace");
  }

  private GoogleSitesAtom() {
  }
}
