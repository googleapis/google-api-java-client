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

package com.google.api.client.xml;

import com.google.api.client.http.HttpContent;

/**
 * Abstract XML HTTP serializer.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in version 1.1) Use
 *             {@link XmlHttpContent}
 */
@Deprecated
public abstract class XmlContent implements HttpContent {

  /** XML namespace dictionary. */
  public XmlNamespaceDictionary namespaceDictionary;

  /** Default implementation returns {@code null}, but subclasses may override. */
  public String getEncoding() {
    return null;
  }

  /** Default implementation returns {@code -1}, but subclasses may override. */
  public long getLength() {
    return -1;
  }
}
