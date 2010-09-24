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

package com.google.api.client.generator.model;


/**
 * @author Yaniv Inbar
 */
public final class DependencyModel implements Comparable<DependencyModel> {

  public String artifactId;
  public String groupId;
  public String scope;
  public String version;

  public int compareTo(DependencyModel other) {
    int compare = groupId.compareTo(other.groupId);
    if (compare != 0) {
      return compare;
    }
    return artifactId.compareTo(other.artifactId);
  }

  @Override
  public int hashCode() {
    return artifactId.hashCode() * 31 + groupId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DependencyModel)) {
      return false;
    }
    DependencyModel other = (DependencyModel) obj;
    return artifactId.equals(other.artifactId) && groupId.equals(other.groupId);
  }

  @Override
  public String toString() {
    return "DependencyModel [artifactId=" + artifactId + ", groupId=" + groupId
        + (scope != null ? ", scope=" + scope : "")
        + (version != null ? ", version=" + version : "") + "]";
  }
}
