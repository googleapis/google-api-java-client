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

package com.google.api.client.xml;

import com.google.api.client.util.DataUtil;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.FieldInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Thread-safe XML namespace dictionary that provides a one-to-one map of namespace alias to URI.
 *
 * <p>
 * A namespace alias is uniquely mapped to a single namespace URI, and a namespace URI is uniquely
 * mapped to a single namespace alias. In other words, it is not possible to have duplicates.
 * </p>
 *
 * <p>
 * Sample usage:
 *
 * <pre>{@code
  static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary()
      .add("", "http://www.w3.org/2005/Atom")
      .add("activity", "http://activitystrea.ms/spec/1.0/")
      .add("georss", "http://www.georss.org/georss")
      .add("media", "http://search.yahoo.com/mrss/")
      .add("thr", "http://purl.org/syndication/thread/1.0");
 *}</pre>
 *
 * <p>
 * Upgrade warning: in prior version 1.2 there was a field {@code namespaceAliasToUriMap}, which has
 * now been made inaccessible.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public final class XmlNamespaceDictionary {

  /**
   * Map from XML namespace alias (or {@code ""} for the default namespace) to XML namespace URI.
   */
  private final HashMap<String, String> namespaceAliasToUriMap = Maps.newHashMap();

  /**
   * Map from XML namespace URI to XML namespace alias (or {@code ""} for the default namespace).
   */
  private final HashMap<String, String> namespaceUriToAliasMap = Maps.newHashMap();

  /**
   * Returns the namespace alias (or {@code ""} for the default namespace) for the given namespace
   * URI.
   *
   * @param uri namespace URI
   * @since 1.3
   */
  public synchronized String getAliasForUri(String uri) {
    return namespaceUriToAliasMap.get(Preconditions.checkNotNull(uri));
  }

  /**
   * Returns the namespace URI for the given namespace alias (or {@code ""} for the default
   * namespace).
   *
   * @param alias namespace alias (or {@code ""} for the default namespace)
   * @since 1.3
   */
  public synchronized String getUriForAlias(String alias) {
    return namespaceAliasToUriMap.get(Preconditions.checkNotNull(alias));
  }

  /**
   * Returns an unmodified set of map entries for the map from namespace alias (or {@code ""} for
   * the default namespace) to namespace URI.
   *
   * @since 1.3
   */
  public synchronized Map<String, String> getAliasToUriMap() {
    return Collections.unmodifiableMap(namespaceAliasToUriMap);
  }

  /**
   * Returns an unmodified set of map entries for the map from namespace URI to namespace alias (or
   * {@code ""} for the default namespace).
   *
   * @since 1.3
   */
  public synchronized Map<String, String> getUriToAliasMap() {
    return Collections.unmodifiableMap(namespaceUriToAliasMap);
  }

  /**
   * Adds a known namespace of the given alias and URI.
   *
   * @param alias alias
   * @param uri namespace URI
   * @deprecated use {@link #set(String, String)} (scheduled to be removed in 1.4)
   */
  @Deprecated
  public void addNamespace(String alias, String uri) {
    set(alias, uri);
  }

  /**
   * Adds a namespace of the given alias and URI.
   *
   * <p>
   * If the uri is {@code null}, the namespace alias will be removed. Similarly, if the alias is
   * {@code null}, the namespace URI will be removed. Otherwise, if the alias is already mapped to a
   * different URI, it will be remapped to the new URI. Similarly, if a URI is already mapped to a
   * different alias, it will be remapped to the new alias.
   * </p>
   *
   * @param alias alias or {@code null} to remove the namespace URI
   * @param uri namespace URI or {@code null} to remove the namespace alias
   * @return this namespace dictionary
   * @since 1.3
   */
  public synchronized XmlNamespaceDictionary set(String alias, String uri) {
    String previousUri = null;
    String previousAlias = null;
    if (uri == null) {
      if (alias != null) {
        previousUri = namespaceAliasToUriMap.remove(alias);
      }
    } else if (alias == null) {
      previousAlias = namespaceUriToAliasMap.remove(uri);
    } else {
      previousUri = namespaceAliasToUriMap.put(
          Preconditions.checkNotNull(alias), Preconditions.checkNotNull(uri));
      if (!uri.equals(previousUri)) {
        previousAlias = namespaceUriToAliasMap.put(uri, alias);
      } else {
        previousUri = null;
      }
    }
    if (previousUri != null) {
      namespaceUriToAliasMap.remove(previousUri);
    }
    if (previousAlias != null) {
      namespaceAliasToUriMap.remove(previousAlias);
    }
    return this;
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *        fields)
   * @param elementName optional XML element local name prefixed by its namespace alias -- for
   *        example {@code "atom:entry"} -- or {@code null} to make up something
   */
  public String toStringOf(String elementName, Object element) {
    try {
      StringWriter writer = new StringWriter();
      XmlSerializer serializer = Xml.createSerializer();
      serializer.setOutput(writer);
      serialize(serializer, elementName, element, false);
      return writer.toString();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *        fields)
   * @param elementNamespaceUri XML namespace URI or {@code null} for no namespace
   * @param elementLocalName XML local name
   * @throws IOException I/O exception
   */
  public void serialize(
      XmlSerializer serializer, String elementNamespaceUri, String elementLocalName, Object element)
      throws IOException {
    serialize(serializer, elementNamespaceUri, elementLocalName, element, true);
  }

  /**
   * Shows a debug string representation of an element data object of key/value pairs.
   *
   * @param element element data object ({@link GenericXml}, {@link Map}, or any object with public
   *        fields)
   * @param elementName XML element local name prefixed by its namespace alias
   * @throws IOException I/O exception
   */
  public void serialize(XmlSerializer serializer, String elementName, Object element)
      throws IOException {
    serialize(serializer, elementName, element, true);
  }

  private void serialize(XmlSerializer serializer, String elementNamespaceUri,
      String elementLocalName, Object element, boolean errorOnUnknown) throws IOException {
    startDoc(serializer, element, errorOnUnknown, elementNamespaceUri).serialize(
        serializer, elementNamespaceUri, elementLocalName);
    serializer.endDocument();
  }

  private void serialize(
      XmlSerializer serializer, String elementName, Object element, boolean errorOnUnknown)
      throws IOException {
    startDoc(serializer, element, errorOnUnknown, null).serialize(serializer, elementName);
    serializer.endDocument();
  }

  private ElementSerializer startDoc(
      XmlSerializer serializer, Object element, boolean errorOnUnknown, String extraNamespace)
      throws IOException {
    serializer.startDocument(null, null);
    SortedSet<String> aliases = new TreeSet<String>();
    computeAliases(element, aliases);
    boolean foundExtra = extraNamespace == null;
    for (String alias : aliases) {
      String uri = getUriForAlias(alias);
      serializer.setPrefix(alias, uri);
      if (!foundExtra && uri.equals(extraNamespace)) {
        foundExtra = true;
      }
    }
    if (!foundExtra) {
      for (Map.Entry<String, String> entry : getAliasToUriMap().entrySet()) {
        if (extraNamespace.equals(entry.getValue())) {
          serializer.setPrefix(entry.getKey(), extraNamespace);
          break;
        }
      }
    }
    return new ElementSerializer(element, errorOnUnknown);
  }

  private void computeAliases(Object element, SortedSet<String> aliases) {
    for (Map.Entry<String, Object> entry : DataUtil.mapOf(element).entrySet()) {
      Object value = entry.getValue();
      if (value != null) {
        String name = entry.getKey();
        if (!"text()".equals(name)) {
          int colon = name.indexOf(':');
          boolean isAttribute = name.charAt(0) == '@';
          if (colon != -1 || !isAttribute) {
            String alias = colon == -1 ? "" : name.substring(name.charAt(0) == '@' ? 1 : 0, colon);
            aliases.add(alias);
          }
          if (!isAttribute && !FieldInfo.isPrimitive(value)) {
            if (value instanceof Collection<?>) {
              for (Object subValue : (Collection<?>) value) {
                computeAliases(subValue, aliases);
              }
            } else {
              computeAliases(value, aliases);
            }
          }
        }
      }
    }
  }

  class ElementSerializer {
    private final boolean errorOnUnknown;
    Object textValue = null;
    final List<String> attributeNames = new ArrayList<String>();
    final List<Object> attributeValues = new ArrayList<Object>();
    final List<String> subElementNames = new ArrayList<String>();
    final List<Object> subElementValues = new ArrayList<Object>();

    ElementSerializer(Object elementValue, boolean errorOnUnknown) {
      this.errorOnUnknown = errorOnUnknown;
      Class<?> valueClass = elementValue.getClass();
      if (FieldInfo.isPrimitive(valueClass)) {
        textValue = elementValue;
      } else {
        for (Map.Entry<String, Object> entry : DataUtil.mapOf(elementValue).entrySet()) {
          Object fieldValue = entry.getValue();
          if (fieldValue != null) {
            String fieldName = entry.getKey();
            if ("text()".equals(fieldName)) {
              textValue = fieldValue;
            } else if (fieldName.charAt(0) == '@') {
              attributeNames.add(fieldName.substring(1));
              attributeValues.add(fieldValue);
            } else {
              subElementNames.add(fieldName);
              subElementValues.add(fieldValue);
            }
          }
        }
      }
    }

    String getNamespaceUriForAliasHandlingUnknown(String alias) {
      String result = getUriForAlias(alias);
      if (result == null) {
        if (errorOnUnknown) {
          throw new IllegalArgumentException(
              "unrecognized alias: " + (alias.length() == 0 ? "(default)" : alias));
        }
        return "http://unknown/" + alias;
      }
      return result;
    }

    void serialize(XmlSerializer serializer, String elementName) throws IOException {
      String elementLocalName = null;
      String elementNamespaceUri = null;
      if (elementName != null) {
        int colon = elementName.indexOf(':');
        elementLocalName = elementName.substring(colon + 1);
        String alias = colon == -1 ? "" : elementName.substring(0, colon);
        elementNamespaceUri = getNamespaceUriForAliasHandlingUnknown(alias);
        if (elementNamespaceUri == null) {
          elementNamespaceUri = "http://unknown/" + alias;
        }
      }
      serialize(serializer, elementNamespaceUri, elementLocalName);
    }

    void serialize(XmlSerializer serializer, String elementNamespaceUri, String elementLocalName)
        throws IOException {
      boolean errorOnUnknown = this.errorOnUnknown;
      if (elementLocalName == null) {
        if (errorOnUnknown) {
          throw new IllegalArgumentException("XML name not specified");
        }
        elementLocalName = "unknownName";
      }
      serializer.startTag(elementNamespaceUri, elementLocalName);
      // attributes
      List<String> attributeNames = this.attributeNames;
      List<Object> attributeValues = this.attributeValues;
      int num = attributeNames.size();
      for (int i = 0; i < num; i++) {
        String attributeName = attributeNames.get(i);
        int colon = attributeName.indexOf(':');
        String attributeLocalName = attributeName.substring(colon + 1);
        String attributeNamespaceUri = colon == -1 ? null : getNamespaceUriForAliasHandlingUnknown(
            attributeName.substring(0, colon));
        serializer.attribute(
            attributeNamespaceUri, attributeLocalName, toSerializedValue(attributeValues.get(i)));
      }
      // text
      Object textValue = this.textValue;
      if (textValue != null) {
        serializer.text(toSerializedValue(textValue));
      }
      // elements
      List<String> subElementNames = this.subElementNames;
      List<Object> subElementValues = this.subElementValues;
      num = subElementNames.size();
      for (int i = 0; i < num; i++) {
        Object subElementValue = subElementValues.get(i);
        String subElementName = subElementNames.get(i);
        if (subElementValue instanceof Collection<?>) {
          for (Object subElement : (Collection<?>) subElementValue) {
            new ElementSerializer(subElement, errorOnUnknown).serialize(serializer, subElementName);
          }
        } else {
          new ElementSerializer(subElementValue, errorOnUnknown).serialize(
              serializer, subElementName);
        }
      }
      serializer.endTag(elementNamespaceUri, elementLocalName);
    }
  }

  static String toSerializedValue(Object value) {
    if (value instanceof Float) {
      Float f = (Float) value;
      if (f.floatValue() == Float.POSITIVE_INFINITY) {
        return "INF";
      }
      if (f.floatValue() == Float.NEGATIVE_INFINITY) {
        return "-INF";
      }
    }
    if (value instanceof Double) {
      Double d = (Double) value;
      if (d.doubleValue() == Double.POSITIVE_INFINITY) {
        return "INF";
      }
      if (d.doubleValue() == Double.NEGATIVE_INFINITY) {
        return "-INF";
      }
    }
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    if (value instanceof DateTime) {
      return ((DateTime) value).toStringRfc3339();
    }
    throw new IllegalArgumentException("unrecognized value type: " + value.getClass());
  }
}
