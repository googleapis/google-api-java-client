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

/**
 * @author Yaniv Inbar
 */
public final class Version {

  public Client client;

  public String id;

  @Key
  public String rootUrl;

  @Key
  public AtomInfo atom;

  public String getJarName() {
    return "data-" + client.id + "-" + id;
  }

  public String getPathRelativeToSrc() {
    return "com/google/api/data/" + client.id + "/" + id;
  }

  public String getPackageName() {
    return getPathRelativeToSrc().replace('/', '.');
  }

  void validate(String id, Client client) {
    this.id = (client.isOldGDataStyle ? "v" : "") + id;
    this.client = client;
    if (atom != null) {
      atom.validate();
    }
  }
}
