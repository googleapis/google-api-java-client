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

package com.google.api.client.generator.model;

import com.google.api.client.util.Key;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author Yaniv Inbar
 */
public final class Client implements Comparable<Client> {

  private static final Set<String> OLD_GDATA_STYLE_IDS =
      new HashSet<String>(Arrays.asList("analytics", "blogger", "books",
          "calendar", "codesearch", "contacts", "docs", "finance", "gbase",
          "health", "maps", "migration", "picasa", "sidewiki", "sites",
          "spreadsheet", "webmastertools", "youtube"));

  public boolean isOldGDataStyle;

  @Key
  public String id;

  @Key
  public String name;

  @Key
  public String className;

  @Key
  public SortedMap<String, Version> versions;

  /** Client Login token type or {@code null}. */
  @Key
  public String authTokenType;

  /** OAuth information or {@code null}. */
  @Key("OAuth")
  public OAuthInfo oauth;

  public int compareTo(Client client) {
    if (client == this) {
      return 0;
    }
    return id.compareTo(client.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Client)) {
      return false;
    }
    Client other = (Client) obj;
    return id.equals(other.id);
  }

  public void validate() {
    if (id == null) {
      throw new IllegalArgumentException("id required");
    }
    if (versions == null || versions.size() < 1) {
      throw new NullPointerException("at least one version required");
    }
    if (className == null) {
      className = Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
    isOldGDataStyle = OLD_GDATA_STYLE_IDS.contains(id);
    for (Map.Entry<String, Version> entry : versions.entrySet()) {
      entry.getValue().validate(entry.getKey(), this);
    }
  }

  public String getXmlFormatId() {
    return "storage".equals(id) ? "xml" : "atom";
  }

  public String getXmlFormatCapitalId() {
    return "storage".equals(id) ? "Xml" : "Atom";
  }

  public String getXmlFormatName() {
    return "storage".equals(id) ? "XML" : "Atom XML";
  }
}
