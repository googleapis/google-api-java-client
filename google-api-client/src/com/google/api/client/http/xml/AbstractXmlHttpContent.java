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

package com.google.api.client.http.xml;

import com.google.api.client.http.HttpContent;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstract serializer for XML HTTP content based on the data key/value mapping object for an item.
 *
 * <p>
 * Upgrade warning: in prior version 1.2 this class was previously in the
 * {@link com.google.api.client.xml} package.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public abstract class AbstractXmlHttpContent implements HttpContent {

  /**
   * Content type. Default value is {@link XmlHttpParser#CONTENT_TYPE}, though subclasses may define
   * a different default value.
   */
  public String contentType = XmlHttpParser.CONTENT_TYPE;

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

  public final String getType() {
    return contentType;
  }

  public final void writeTo(OutputStream out) throws IOException {
    XmlSerializer serializer = Xml.createSerializer();
    serializer.setOutput(out, "UTF-8");
    writeTo(serializer);
  }

  protected abstract void writeTo(XmlSerializer serializer) throws IOException;
}
