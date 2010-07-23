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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * Factory for creating new XML pull parsers and XML serializers.
 * 
 * @since 1.0
 * @author Yaniv Inbar
 */
public interface XmlParserFactory {

  /**
   * Creates a new XML pull parser.
   * 
   * @throws XmlPullParserException if parser could not be created
   */
  XmlPullParser createParser() throws XmlPullParserException;

  /**
   * Creates a new XML serializer.
   * 
   * @throws XmlPullParserException if serializer could not be created
   */
  XmlSerializer createSerializer() throws XmlPullParserException;
}
